package models.taobao;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Order;
import com.taobao.api.request.LogisticsDummySendRequest;
import com.taobao.api.request.TradeFullinfoGetRequest;
import com.taobao.api.response.LogisticsDummySendResponse;
import com.taobao.api.response.TradeFullinfoGetResponse;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.journal.MQJournal;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.NotEnoughInventoryException;
import models.resale.ResalerFav;
import org.json.JSONException;
import org.json.JSONObject;
import play.Logger;
import play.Play;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author  likang
 * Date: 12-5-3
 */
@OnApplicationStart(async = true)
public class TaobaoCometConsumer extends RabbitMQConsumer<TaobaoCometMessage>{
    private static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
    private static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "12621657");
    private static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "b0d06603b45a281f783b6ccd72ad8745");


    @Override
    protected void consume(TaobaoCometMessage message) {
        //开启事务管理
        JPAPlugin.startTx(false);
        //保存消息
        new MQJournal(TaobaoCometUtil.QUEUE_NAME, message.getMessage()).save();
        //解析主动通知的消息,目前只处理用户对订单进行付款的消息
        JSONObject tradeMessage = parseMessage(message);
        if(tradeMessage == null){
            JPAPlugin.closeTx(false);
            return;
        }
        String messageStatus = null;
        String taobaoUserId = null;
        Long taobaoOrderId = null;
        try {
            messageStatus = tradeMessage.getString("status");
            taobaoUserId = tradeMessage.getString("user_id");
            taobaoOrderId = tradeMessage.getLong("tid");
        } catch (JSONException e) {
            Logger.error(e, "error while decode taobao comet message:\n" + message.getMessage());
            JPAPlugin.closeTx(false);
            return;
        }
        if(!messageStatus.equals("TradeBuyerPay")){
            Logger.info("the message status is not TradeBuyerPay, ignore it");
            JPAPlugin.closeTx(false);
            return;
        }

        // 查找 oAuthToken
        OAuthToken oAuthToken = OAuthToken.getOAuthToken(taobaoUserId, WebSite.TAOBAO);
        if (oAuthToken == null || oAuthToken.isExpired() || !oAuthToken.accountType.equals(AccountType.RESALER)){
            Logger.error("oauth token invalid");
            JPAPlugin.closeTx(false);
            return;
        }
        // 获取订单的详细信息
        TradeFullinfoGetResponse taobaoOrder = getTaobaoOrder(taobaoOrderId, oAuthToken);
        if(taobaoOrder == null || taobaoOrder.getErrorCode() != null){
            Logger.error("can not get the fullinfo of taobao order:" + taobaoOrderId);
            JPAPlugin.closeTx(false);
            return;
        }
        // 生成本地订单
        models.order.Order resalerOrder = createResalerOrder(taobaoOrder, oAuthToken.userId);
        if(resalerOrder == null){
            Logger.error("can not create resaler order");
            JPAPlugin.closeTx(false);
            return;
        }
        // 自动处理本地订单
        autoProcessResalerOrder(oAuthToken, taobaoOrder, resalerOrder);

        JPAPlugin.closeTx(false);
    }

    /**
     * 解析主动通知的json消息
     *
     * @param message 带有json格式的消息
     * @return 转换后的消息
     */
    private JSONObject parseMessage(TaobaoCometMessage message){
        try {
            JSONObject jsonObject = new JSONObject(message.getMessage());
            return jsonObject.getJSONObject("notify_trade");
        } catch (JSONException e) {
            Logger.error(e, "error while decode taobao comet message:\n" + message.getMessage());
            return null;
        }
    }

    /**
     * 查询淘宝订单明细.
     * 目前只请求:收件人手机,收件人电话,每笔子订单订单数量,子订单的购买者ID
     *
     * @param orderId  订单ID
     * @param oAuthToken oauth token
     * @return 淘宝订单明细
     */
    private TradeFullinfoGetResponse getTaobaoOrder(Long orderId, OAuthToken oAuthToken){
        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);
        TradeFullinfoGetRequest fullinfoGetRequest = new TradeFullinfoGetRequest();
        //请求以下字段:收件人手机,收件人电话,每笔子订单订单数量,子订单的购买者ID
        fullinfoGetRequest.setFields("tid,receiver_mobile,receiver_phone,orders.num,orders.num_iid");
        fullinfoGetRequest.setTid(orderId);
        try {
            return taobaoClient.execute(fullinfoGetRequest , oAuthToken.accessToken);
        } catch (ApiException e) {
            Logger.error(e, "error while request taobao api");
            return null;
        }

    }

    /**
     * 创建优惠啦系统中分销商的订单.
     *
     * @param taobaoOrder 淘宝的订单
     * @param userId 分销商ID
     * @return 分销商订单
     */
    private models.order.Order createResalerOrder(TradeFullinfoGetResponse taobaoOrder, Long userId){
        String mobile =  taobaoOrder.getTrade().getReceiverMobile();

        // 遍历所有的子订单(也就是我们所谓的orderItem),生成我们的订单
        List<Order> taobaoOrders = taobaoOrder.getTrade().getOrders();
        models.order.Order order = new models.order.Order(userId, AccountType.RESALER);
        for(Order tOrder: taobaoOrders){
            ResalerFav resalerFav = ResalerFav.find("byTaobaoItemId", tOrder.getIid()).first();
            if(resalerFav == null){
                continue;
            }
            BigDecimal resalerPrice = resalerFav.goods.getResalePrice(resalerFav.resaler.level);
            try {
                order.addOrderItem(resalerFav.goods,tOrder.getNum(), mobile, resalerPrice, resalerPrice);
            } catch (NotEnoughInventoryException e) {
                Logger.error(e, "auto generate order failed: inventory not enough");
            }
        }
        if(order.orderItems.size() == 0){
            return null;
        }
        order.createAndUpdateInventory();
        return order;

    }

    /**
     * 自动处理已生成的分销商订单.如果淘宝用户在一个订单中购买的所有商品都是我们发布过去的,并且分销商余额足够
     * 则自动设置淘宝订单状态为已发货,并且也在分销系统中发货
     *
     * @param oAuthToken oauth token
     * @param taobaoOrder 淘宝订单
     * @param resalerOrder 分销商订单
     */
    private void autoProcessResalerOrder(OAuthToken oAuthToken, TradeFullinfoGetResponse taobaoOrder,
                                         models.order.Order resalerOrder) {
        // 如果两边子订单数量一致,并且分销商账户中余额足够,则自动设置淘宝订单为已发货,并结算本地账户
        if(resalerOrder.orderItems.size() == taobaoOrder.getTrade().getOrders().size()){
            Account account = AccountUtil.getAccount(oAuthToken.getId(), AccountType.RESALER);
            //余额足够
            if(account.amount.compareTo(resalerOrder.needPay) >=0){
                LogisticsDummySendResponse taobaoOrderSend = setTaobaoOrderSend(taobaoOrder.getTrade().getTid(), oAuthToken);
                //淘宝成功设置已发货
                if (taobaoOrderSend != null && taobaoOrderSend.getErrorCode() == null){
                    resalerOrder.accountPay = resalerOrder.needPay;
                    resalerOrder.discountPay = BigDecimal.ZERO;

                    TradeBill tradeBill =
                            TradeUtil.createOrderTrade(account, resalerOrder.accountPay, resalerOrder.discountPay,
                                    null, resalerOrder.getId());
                    if(tradeBill != null){
                        resalerOrder.payRequestId = tradeBill.getId();
                        resalerOrder.payMethod = null;

                        resalerOrder.paid();
                    }else {
                        Logger.info("auto process resaler order failed: can not create trade bill");
                    }
                }else {
                    Logger.info("auto process resaler order failed: set taobao order send failed");
                }
            }else {
                Logger.info("auto process resaler order failed: balance not enough");
            }

        }else {
            Logger.info("auto process resaler order failed: not all of the orderItems in taobaoOrder is our goods");
        }

    }

    /**
     * 设置淘宝订单为已发货
     *
     * @param taobaoOrderId 淘宝订单ID
     * @param token oauth token
     * @return API返回
     */
    private LogisticsDummySendResponse setTaobaoOrderSend(Long taobaoOrderId, OAuthToken token){
        TaobaoClient client=new DefaultTaobaoClient(URL, APPKEY, APPSECRET);
        LogisticsDummySendRequest req=new LogisticsDummySendRequest();
        req.setTid(taobaoOrderId);
        try {
            return client.execute(req , token.accessToken);
        } catch (ApiException e) {
            Logger.error(e, "set taobao order send failed");
            return null;
        }
    }


    @Override
    protected Class getMessageType() {
        return TaobaoCometMessage.class;
    }

    @Override
    protected String queue() {
        return TaobaoCometUtil.QUEUE_NAME;
    }
/*    {
    "notify_trade":
       {
         "topic":"trade",
         "payment":"0.01",
         "status":"TradeBuyerPay",
         "type":"guarantee_trade",
         "modified":"2012-05-03 15:46:13",
         "buyer_nick":"kisbear",
         "nick":"玲丫月",
         "oid":169412921091179,
         "is_3D":true,
         "user_id":85365940,
         "tid":169412921091179,
         "seller_nick":"玲丫月"
       }
    }*/
}
