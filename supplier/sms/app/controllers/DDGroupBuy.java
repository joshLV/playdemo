package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.dangdang.groupbuy.DDErrorCode;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Before;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午3:59
 */
public class DDGroupBuy extends Controller {
    public static final String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    private static final String VER = Play.configuration.getProperty("dangdang.version");
    private static final String SPID = Play.configuration.getProperty("dangdang.spid", "3000003");

    /**
     * 基本相应参数
     */
    @Before
    public static void baseResponse() {
        renderArgs.put("spid", SPID);
        renderArgs.put("ver", VER);
    }

    /**
     * 当当新订单请求.
     *
     * @param id            当当团购唯一标识符
     * @param ddgid         当当商品ID
     * @param user_mobile   用户手机号
     * @param options       商品及数量信息
     * @param express_memo
     * @param user_id
     * @param kx_order_id
     * @param amount
     * @param sign
     */
    public static void order(String id, String ddgid, String user_mobile, String options, String express_memo,
                             String user_id, String kx_order_id, BigDecimal amount, String sign) {
        Map<String, String > allParams = params.allSimple();
        Logger.info("dangdang new order request:\n%s", new Gson().toJson(allParams));
        //取得参数信息 必填信息
        SortedMap<String, String> params = DDGroupBuyUtil.filterPlayParams(allParams);

        //检查参数
        if (StringUtils.isBlank(user_mobile) || StringUtils.isBlank(user_id)) {
            renderError(DDErrorCode.USER_NOT_EXITED, "用户或者手机不存在!");
        }
        if (StringUtils.isBlank(kx_order_id)) {
            renderError(DDErrorCode.ORDER_NOT_EXITED, "订单不存在！");
        }
        //校验参数
        if (StringUtils.isBlank(sign) || !sign.toLowerCase().equals(DDGroupBuyUtil.signParams(params))) {
            renderError(DDErrorCode.VERIFY_FAILED, "sign验证失败！");
        }
        //定位请求者
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.DD_LOGIN_NAME);
        Long resalerId = null;
        if (resaler == null) {
            renderError(DDErrorCode.USER_NOT_EXITED, "当当分销商用户不存在！");
        } else {
            resalerId = resaler.id;
        }

        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", OuterOrderPartner.DD, kx_order_id).first();
        //outerOrder是否存在的标志
        Boolean isExisted = true;
        Order order;

        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            isExisted = false;
            outerOrder = new OuterOrder();
            outerOrder.orderId = kx_order_id;
            outerOrder.partner = OuterOrderPartner.DD;
            outerOrder.message = gson.toJson(params);
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) {
                // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，一百券订单产生了则，直接返回，否则，继续创建一百券订单
                isExisted = (outerOrder.ybqOrder != null);
            }
        }

        //如果已经存在订单，则不处理，直接返回xml
        if (isExisted) {
            order = Order.findOneByResaler(outerOrder.ybqOrder.orderNumber, resaler);
            if (order != null) {
                List<ECoupon> eCouponList = ECoupon.findByOrder(order);
                render("dangdang/groupbuy/response/order.xml", order, id, kx_order_id, eCouponList);
            }
        }


        order = Order.createConsumeOrder(resalerId, AccountType.RESALER);
        //分解有几个商品，每个商品购买的数量
        String[] arrGoods = options.split(",");

        BigDecimal ybqPrice = BigDecimal.ZERO;
        String[] arrGoodsItem;
        for (String goodsItem : arrGoods) {
            arrGoodsItem = goodsItem.split(":");
            if (arrGoodsItem != null) {
                if(StringUtils.isEmpty(arrGoodsItem[0])){
                    renderError(DDErrorCode.ORDER_EXCEPTION, "传参数据信息格式不对！请确认检查无误！");
                }
                Goods goods = ResalerProduct.getGoods(resaler, Long.parseLong(arrGoodsItem[0]), OuterOrderPartner.DD);
                if (goods==null){
                    renderError(DDErrorCode.ORDER_EXCEPTION, "没有对应的商品信息！");
                }
                BigDecimal resalerPrice = goods.getResalePrice();
                BigDecimal number = new BigDecimal(arrGoodsItem[1]);
                ybqPrice = ybqPrice.add(resalerPrice.multiply(number));
                if (ybqPrice.compareTo(amount) != 0) {
                    resalerPrice = amount.divide(number);
                    Logger.error("当当订单金额不一致！当当amount=" + amount + ",ybqPrice=" + ybqPrice);
                }
                try {
                    //创建一百券订单Items
                    order.addOrderItem(goods, number.longValue(), user_mobile, resalerPrice, resalerPrice);
                } catch (NotEnoughInventoryException e) {
                    renderError(DDErrorCode.INVENTORY_NOT_ENOUGH, "库存不足！");
                }
            }
        }

        order.deliveryType = DeliveryType.SMS;
        order.remark = express_memo;
        order.createAndUpdateInventory();
        order.accountPay = order.needPay;
        order.discountPay = BigDecimal.ZERO;
        order.payMethod = PaymentSource.getBalanceSource().code;
        order.payAndSendECoupon();
        //设置当当订单中的一百券订单
        outerOrder.ybqOrder = order;
        outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
        outerOrder.save();
        List<ECoupon> eCouponList = ECoupon.findByOrder(order);
        for (ECoupon coupon : eCouponList) {
            coupon.partner = ECouponPartner.DD;
            coupon.save();
        }
        render("dangdang/groupbuy/response/order.xml", order, id, kx_order_id, eCouponList);
    }

    /**
     * 当当请我们发送短信.
     *
     */
    public static void sendSms(String sign, String data, String call_time) {
        Logger.info("dangdang sendSms request:\n%s", new Gson().toJson(params.allSimple()));
        data = StringUtils.trimToEmpty(data);
        call_time = StringUtils.trimToEmpty(call_time);
        sign = StringUtils.trimToEmpty(sign).toLowerCase();

        String verifySign = DDGroupBuyUtil.sign("send_msg", data, call_time);
        if (StringUtils.isBlank(sign) || !sign.equals(verifySign)) {
            renderError(DDErrorCode.VERIFY_FAILED, "sign验证失败！");
        }

        Document dataXml = XML.getDocument(data);
        String orderId = XPath.selectText("/data/order/order_id", dataXml).trim();
        Long spgid = Long.parseLong(XPath.selectText("/data/order/spgid", dataXml).trim());
        String receiverMobileTel = XPath.selectText("/data/order/receiver_mobile_tel", dataXml).trim();
        String consumeId = XPath.selectText("/data/order/consume_id", dataXml).trim();

        //取得data节点中的数据信息

        //根据当当订单编号，查询订单是否存在
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", OuterOrderPartner.DD, orderId).first();
        if (outerOrder == null || outerOrder.ybqOrder == null) {
            renderError(DDErrorCode.ORDER_NOT_EXITED, "没找到对应的当当订单!");
        }
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.DD_LOGIN_NAME);

        if (resaler == null) {
            renderError(DDErrorCode.USER_NOT_EXITED, "当当用户不存在！");
        }

        Order ybqOrder = Order.find("orderNumber= ? and userId=?", outerOrder.ybqOrder.orderNumber, resaler.id).first();
        if (ybqOrder == null) {
            renderError(DDErrorCode.ORDER_NOT_EXITED, "没找到对应的当当订单!");
        }

        //从对应商品关系表中取得商品
        Goods goods = ResalerProduct.getGoods(resaler, spgid, OuterOrderPartner.DD);
        if (goods == null) {
            goods = Goods.findById(spgid);
        }
        ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and goods=?", ybqOrder, consumeId, goods).first();
        if (coupon == null) {
            renderError(DDErrorCode.COUPON_SN_NOT_EXISTED, "没找到对应的券号!");
        }
        //券已消费
        Logger.info("处理当当券(id: %s)重发, tel: %s", coupon.id, receiverMobileTel);
        if (coupon.status == ECouponStatus.CONSUMED) {
            renserSuccessInfo(coupon, orderId, "对不起该券已消费!");
        }
        //券已退款
        if (coupon.status == ECouponStatus.REFUND) {
            renserSuccessInfo(coupon, orderId, "对不起该券已退款!");
        }
        //券已过期
        if (coupon.expireAt.before(new Date())) {
            renserSuccessInfo(coupon, orderId, "对不起该券已过期!");
        }
        //最多发送三次短信
        if (coupon.smsSentCount >= 3) {
            renserSuccessInfo(coupon, orderId, "重发短信超过三次!");
        }

        //发送短信并返回成功
        coupon.sendOrderSMS(receiverMobileTel, "当当重发短信");
        String desc = "success";
        render("dangdang/groupbuy/response/sendMessage.xml", desc, coupon, orderId);
    }

    private static void renderError(DDErrorCode errorCode, String errorDesc) {
        Logger.info("process dangdang's request error: code: %s, desc: %s", errorCode, errorDesc);
        render("dangdang/groupbuy/response/error.xml", errorCode, errorDesc);
    }


    private static void renserSuccessInfo(ECoupon coupon, String orderId, String desc) {
        Logger.info("process dangdang's request error: coupon.id: %d, desc: %s, 但返回成功消息", coupon.id, desc);
        render("dangdang/groupbuy/response/sendMessage.xml", desc, coupon, orderId);
    }
}

