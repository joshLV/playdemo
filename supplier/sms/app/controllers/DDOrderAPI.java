package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.dangdang.groupbuy.DDErrorCode;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.order.*;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.XPath;
import play.mvc.Before;
import play.mvc.Controller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
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
public class DDOrderAPI extends Controller {
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
     * @param id
     * @param ddgid
     * @param user_mobile
     * @param options
     * @param express_memo
     * @param user_id
     * @param kx_order_id
     * @param amount
     * @param sign
     */
    public static void order(String id, String ddgid, String user_mobile, String options, String express_memo,
                             String user_id, String kx_order_id, BigDecimal amount, String sign) {
        Logger.info("[DDOrderAPI] begin ");
        //取得参数信息 必填信息
        SortedMap<String, String> params = DDGroupBuyUtil.filterPlayParams(request.params.allSimple());

        //检查参数
        if (StringUtils.isBlank(user_mobile) || StringUtils.isBlank(user_id)) {
            renderError(DDErrorCode.USER_NOT_EXITED, "用户或者手机不存在!");
        }
        if (StringUtils.isBlank(kx_order_id)) {
            renderError(DDErrorCode.ORDER_NOT_EXITED, "订单不存在！");
        }
        //校验参数
        if (StringUtils.isBlank(sign) || !sign.equals(DDGroupBuyUtil.signParams(params))) {
            renderError(DDErrorCode.VERIFY_FAILED, "sign验证失败！");
        }
        //定位请求者
        Resaler resaler = Resaler.find("loginName=? and status=?", Resaler.DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
        Long resalerId = null;
        if (resaler == null) {
            renderError(DDErrorCode.USER_NOT_EXITED, "当当分销商用户不存在！");
        } else {
            resalerId = resaler.id;
        }

        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.DD, Long.valueOf(kx_order_id)).first();
        //outerOrder是否存在的标志
        Boolean isExisted = true;
        Order order;

        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            isExisted = false;
            outerOrder = new OuterOrder();
            outerOrder.orderId = Long.parseLong(kx_order_id);
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
            order = Order.findOneByUser(outerOrder.ybqOrder.orderNumber, resalerId, AccountType.RESALER);
            if (order != null) {
                Logger.info("[DDOrderAPI] order has existed,and render xml");
                List<ECoupon> eCouponList = ECoupon.findByOrder(order);
                render(order, id, kx_order_id, eCouponList);
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
                Goods goods = ResalerProduct.getGoods(Long.parseLong(arrGoodsItem[0]));
                BigDecimal resalerPrice = goods.getResalePrice();
                BigDecimal number = new BigDecimal(arrGoodsItem[1]);
                ybqPrice = ybqPrice.add(resalerPrice.multiply(number));
                if (ybqPrice.compareTo(amount) != 0) {
                    resalerPrice = amount.divide(number);
                    Logger.error("当当订单金额不一致！当当amount=" + amount + ",ybqPrice=" + ybqPrice);
                }
                try {
                    //创建一百券订单Items
                    order.addOrderItem(goods, number.intValue(), user_mobile, resalerPrice, resalerPrice);
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
        render(order, id, kx_order_id, eCouponList);
    }

    public static void sendSms(String sign, String data, String time, String order_id, Long spgid,
                               String receiver_mobile_tel, String consume_id) {
        Logger.info("[DDSendMessageAPI] begin ");
        //取得参数信息
        String verifySign = DDGroupBuyUtil.sign("send_msg", data, time);
        if (StringUtils.isBlank(sign) || !sign.equals(verifySign)) {
            renderError(DDErrorCode.VERIFY_FAILED, "sign验证失败！");
        }


        //取得data节点中的数据信息

        //根据当当订单编号，查询订单是否存在
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.DD, Long.parseLong(order_id)).first();
        if (outerOrder == null || outerOrder.ybqOrder == null) {
            renderError(DDErrorCode.ORDER_NOT_EXITED, "没找到对应的当当订单!");
        }
        Resaler resaler = Resaler.find("loginName=? and status=?", Resaler.DD_LOGIN_NAME, ResalerStatus.APPROVED).first();

        if (resaler == null) {
            renderError(DDErrorCode.USER_NOT_EXITED, "当当用户不存在！");
        }

        Order ybqOrder = Order.find("orderNumber= ? and userId=? and userType=?", outerOrder.ybqOrder.orderNumber, resaler.id, AccountType.RESALER).first();
        if (ybqOrder == null) {
            renderError(DDErrorCode.ORDER_NOT_EXITED, "没找到对应的当当订单!");
        }

        //从对应商品关系表中取得商品
        Goods goods = ResalerProduct.getGoods(spgid);
        if (goods == null) {
            goods = Goods.findById(spgid);
        }
        ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and goods=?", ybqOrder, consume_id, goods).first();
        if (coupon == null) {
            renderError(DDErrorCode.COUPON_SN_NOT_EXISTED, "没找到对应的券号!");
        }
        //券已消费
        if (coupon.status == ECouponStatus.CONSUMED) {
            renderError(DDErrorCode.COUPON_CONSUMED, "对不起该券已消费，不能重发短信！");
        }
        //券已退款
        if (coupon.status == ECouponStatus.REFUND) {
            renderError(DDErrorCode.COUPON_REFUND, "对不起该券已退款，不能重发短信！");
        }
        //券已过期
        if (coupon.expireAt.before(new Date())) {
            renderError(DDErrorCode.COUPON_EXPIRED, "对不起该券已过期，不能重发短信！");
        }
        //最多发送三次短信
        if (coupon.smsSentCount >= 3) {
            renderError(DDErrorCode.MESSAGE_SEND_FAILED, "重发短信超过三次！");
        }

        //发送短信并返回成功
        coupon.sendOrderSMS(receiver_mobile_tel, "当当重发短信");

        /*
        response.desc = "success";
        response.addAttribute("consumeId", coupon.eCouponSn);
        response.addAttribute("ddOrderId", orderId);
        response.addAttribute("ybqOrderId", coupon.order.id);

        return response;
        */
    }

    private static void renderError(DDErrorCode errorCode, String errorDesc) {
        Logger.info("process dangdang's request error: code: %s, desc: %s", errorCode, errorDesc);
        render("dangdang/groupbuy/response/error.xml", errorCode, errorDesc);
    }
}

