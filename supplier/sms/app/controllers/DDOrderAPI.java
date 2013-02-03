package controllers;

import models.dangdang.groupbuy.DDGroupBuyUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.util.SortedMap;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午3:59
 */
public class DDOrderAPI extends Controller {
    public static final String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    /**
     * 基本相应参数
     */
    @Before
    public static void baseResponse() {
        renderArgs.put("version", "1.0");
        renderArgs.put("zip", "false");
    }

    public static void order(String id, String ddgid, String user_mobile, String options, String express_memo,
                             String user_id, String kx_order_id, BigDecimal amount) {
        Logger.info("[DDOrderAPI] begin ");
        //取得参数信息 必填信息
        SortedMap<String, String> params = DDGroupBuyUtil.filterPlayParams(request.params.allSimple());

        String sign = StringUtils.trimToEmpty(params.get("sign")).toLowerCase();
        /*
        ErrorInfo errorInfo = new ErrorInfo();
        //检查参数
        if (StringUtils.isBlank(user_mobile) || StringUtils.isBlank(user_id)) {
            errorInfo.errorCode = DDErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户或手机不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        if (StringUtils.isBlank(kx_order_id)) {
            errorInfo.errorCode = DDErrorCode.ORDER_NOT_EXITED;
            errorInfo.errorDes = "订单不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }
        //校验参数
        if (StringUtils.isBlank(sign) || !sign.equals(DDGroupBuyUtil.signParams(params))) {
            errorInfo.errorCode = DDErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign验证失败！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        //定位请求者
        Resaler resaler = Resaler.find("loginName=? and status=?", Resaler.DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
        Long resalerId = null;
        if (resaler == null) {
            errorInfo.errorCode = DDErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "当当分销商用户不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
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
                Goods goods = GoodsDeployRelation.getGoods(OuterOrderPartner.DD, Long.parseLong(arrGoodsItem[0]));
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
                    errorInfo.errorCode = DDErrorCode.INVENTORY_NOT_ENOUGH;
                    errorInfo.errorDes = "库存不足！";
                    render(errorInfo);
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
        for (OrderItems ybqItem : order.orderItems) {
            Integer number = ybqItem.buyNumber.intValue();
            if (number > 0 && ybqItem.goods != null) {
//                new DDOrderItem(Long.valueOf(kx_order_id), ddgid, ybqItem.goods, ybqItem.buyNumber, ybqItem).save();
            }
        }
        outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
        outerOrder.save();
        List<ECoupon> eCouponList = ECoupon.findByOrder(order);
        for (ECoupon coupon : eCouponList) {
            coupon.partner = ECouponPartner.DD;
            coupon.save();
        }
        Logger.info("\n [DDOrderAPI] end ");
        render(order, id, kx_order_id, eCouponList);
    }

    public static void sendSms() {
        /*
        Logger.info("[DDAPIUtil.sendSMS] sendMsg begin]" + data);
        Response response = new Response();
        Request request = new Request();
        response.ver = VER;
        response.spid = SPID;
        try {
            request.parse(data);
        } catch (Exception e) {
            response = new Response();
            response.spid = SPID;
            response.ver = VER;
            response.errorCode = DDErrorCode.PARSE_XML_FAILED;
            response.desc = "xml解析失败！";
            return response;
        }
        //取得data节点中的数据信息
        Map<String, String> dataMap = request.params;
        String orderId = dataMap.get("order_id");
        Long spgid = Long.parseLong(dataMap.get("spgid"));
        String receiveMobile = dataMap.get("receiver_mobile_tel");
        String consumeId = dataMap.get("consume_id");

        //根据当当订单编号，查询订单是否存在
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId",
                OuterOrderPartner.DD, Long.valueOf(orderId)).first();
        if (outerOrder == null || outerOrder.ybqOrder == null) {
            response.errorCode = DDErrorCode.ORDER_NOT_EXITED;
            response.desc = "没找到对应的当当订单!";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        Resaler resaler = Resaler.find("loginName=? and status=?", Resaler.DD_LOGIN_NAME, ResalerStatus.APPROVED).first();

        if (resaler == null) {
            response.errorCode = DDErrorCode.USER_NOT_EXITED;
            response.desc = "当当用户不存在！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }


        Order ybqOrder = Order.find("orderNumber= ? and userId=? and userType=?", outerOrder.ybqOrder.orderNumber, resaler.id, AccountType.RESALER).first();
        if (ybqOrder == null) {
            response.errorCode = DDErrorCode.ORDER_NOT_EXITED;
            response.desc = "没找到对应的订单!";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }

        //从对应商品关系表中取得商品
        Goods goods = GoodsDeployRelation.getGoods(OuterOrderPartner.DD, spgid);
        if (goods == null) {
            goods = Goods.findById(spgid);
        }
        ECoupon coupon = ECoupon.find("order=? and eCouponSn=? and goods=?", ybqOrder, consumeId, goods).first();
        if (coupon == null) {
            response.errorCode = DDErrorCode.COUPON_SN_NOT_EXISTED;
            response.desc = "没找到对应的券号!";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //券已消费
        if (coupon.status == ECouponStatus.CONSUMED) {
            response.errorCode = DDErrorCode.COUPON_CONSUMED;
            response.desc = "对不起该券已消费，不能重发短信！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //券已退款
        if (coupon.status == ECouponStatus.REFUND) {
            response.errorCode = DDErrorCode.COUPON_REFUND;
            response.desc = "对不起该券已退款，不能重发短信！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //券已过期
        if (coupon.expireAt.before(new Date())) {
            response.errorCode = DDErrorCode.COUPON_EXPIRED;
            response.desc = "对不起该券已过期，不能重发短信！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }
        //最多发送三次短信
        if (coupon.smsSentCount >= 3) {
            response.errorCode = DDErrorCode.MESSAGE_SEND_FAILED;
            response.desc = "重发短信超过三次！";
            Logger.info("[DDAPIUtil.sendSMS]" + response.desc);
            return response;
        }

        //发送短信并返回成功
        coupon.sendOrderSMS(receiveMobile, "当当重发短信");

        response.errorCode = DDErrorCode.SUCCESS;
        response.desc = "success";
        response.addAttribute("consumeId", coupon.eCouponSn);
        response.addAttribute("ddOrderId", orderId);
        response.addAttribute("ybqOrderId", coupon.order.id);

        return response;
        */

    }
}

