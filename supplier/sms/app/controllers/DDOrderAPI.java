package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.dangdang.DDAPIUtil;
import models.dangdang.DDOrderItem;
import models.dangdang.ErrorCode;
import models.dangdang.ErrorInfo;
import models.order.*;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedMap;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午3:59
 */
public class DDOrderAPI extends Controller {
    public static String DD_LOGIN_NAME = Play.configuration.getProperty("dangdang.resaler_login_name", "dangdang");
    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static void order() {
        Logger.info("[DDOrderAPI] begin ");
        //取得参数信息 必填信息
        SortedMap<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
        String ddgid = StringUtils.trimToEmpty(params.get("id"));
        String user_mobile = StringUtils.trimToEmpty(params.get("user_mobile"));
        String options = StringUtils.trimToEmpty(params.get("options"));
        String express_memo = StringUtils.trimToEmpty(params.get("express_memo"));
        String user_id = StringUtils.trimToEmpty(params.get("user_id"));
        String kx_order_id = StringUtils.trimToEmpty(params.get("kx_order_id"));
        String sign = StringUtils.trimToEmpty(params.get("sign")).toLowerCase();
        ErrorInfo errorInfo = new ErrorInfo();
        //检查参数
        if (StringUtils.isBlank(user_mobile) || StringUtils.isBlank(user_id)) {
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户或手机不存在！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        if (StringUtils.isBlank(kx_order_id)) {
            Logger.error("invalid kx_order_id: %s", kx_order_id);
            errorInfo.errorCode = ErrorCode.ORDER_NOT_EXITED;
            errorInfo.errorDes = "订单不存在！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        }
        //校验参数
        if (StringUtils.isBlank(sign) || !DDAPIUtil.validSign(params, sign)) {
            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign验证失败！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        //定位请求者
        Resaler resaler = Resaler.find("loginName=? and status=?", DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
        Long resalerId = null;
        if (resaler == null) {
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "当当分销商用户不存在！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        } else {
            resalerId = resaler.id;
        }

        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderNumber",
                OuterOrderPartner.DD, kx_order_id).first();
        //outerOrder是否存在的标志
        Boolean isExited = true;
        Order order;

        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            isExited = false;
            outerOrder = new OuterOrder();
            outerOrder.orderNumber = kx_order_id;
            outerOrder.partner = OuterOrderPartner.DD;
            outerOrder.message = gson.toJson(params);
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) {
                // 如果写入失败，说明 已经存在一个相同的orderCode 的订单，一百券订单产生了则，直接返回，否则，继续创建一百券订单
                if (outerOrder.ybqOrder == null) isExited = false;
            }
        }


        //如果已经存在订单，则不处理，直接返回xml
        if (isExited) {
            order = Order.findOneByUser(outerOrder.ybqOrder.orderNumber, resalerId, AccountType.RESALER);
            if (order != null) {
                Logger.info("[DDOrderAPI] order has existed,and render xml");
                List<ECoupon> eCouponList = ECoupon.findByOrder(order);
                render(order, ddgid, kx_order_id, eCouponList);
            }
        }


        order = Order.createConsumeOrder(resalerId, AccountType.RESALER);
        //分解有几个商品，每个商品购买的数量
        String[] arrGoods = options.split(",");

        String[] arrGoodsItem;
        for (String goodsItem : arrGoods) {
            arrGoodsItem = goodsItem.split(":");
            if (arrGoodsItem != null) {
                Goods goods = Goods.findById(Long.parseLong(arrGoodsItem[0]));
                BigDecimal resalerPrice = goods.getResalePrice(ResalerLevel.NORMAL);
                try {
                    //创建一百券订单Items
                    order.addOrderItem(goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, resalerPrice, resalerPrice);
                } catch (NotEnoughInventoryException e) {
                    Logger.info("inventory not enough");
                    errorInfo.errorCode = ErrorCode.INVENTORY_NOT_ENOUGH;
                    errorInfo.errorDes = "库存不足！";
                    render(errorInfo);
                }
            }
        }

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
                new DDOrderItem(Long.valueOf(kx_order_id), ddgid, ybqItem.goods, ybqItem).save();
            }
        }
        outerOrder.save();
        List<ECoupon> eCouponList = ECoupon.findByOrder(order);
        Logger.info("\n [DDOrderAPI] end ");
        render(order, ddgid, kx_order_id, eCouponList);
    }
}

//
//    public static void order1() {
//        Logger.info("[DDOrderAPI] begin ");
//        //取得参数信息 必填信息
//        SortedMap<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
//        Long ddgid = Long.valueOf(params.get("id"));
//        String all_amount = StringUtils.isBlank(params.get("all_amount")) ? "0" : StringUtils.trimToEmpty(params.get("all_amount"));
//        String amount = StringUtils.isBlank(params.get("amount")) ? "0" : StringUtils.trimToEmpty(params.get("amount"));
//        String user_mobile = StringUtils.trimToEmpty(params.get("user_mobile"));
//        String options = StringUtils.trimToEmpty(params.get("options"));
//        String express_memo = StringUtils.trimToEmpty(params.get("express_memo"));
//        String express_fee = StringUtils.trimToEmpty(params.get("express_fee"));
//        String user_id = StringUtils.trimToEmpty(params.get("user_id"));
//        String kx_order_id = StringUtils.trimToEmpty(params.get("kx_order_id"));
//        String sign = StringUtils.trimToEmpty(params.get("sign")).toLowerCase();
//        ErrorInfo errorInfo = new ErrorInfo();
//        //检查参数
//        if (StringUtils.isBlank(user_mobile) || StringUtils.isBlank(user_id)) {
//            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
//            errorInfo.errorDes = "用户或手机不存在！";
//            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
//            render("/DDOrderAPI/error.xml", errorInfo);
//        }
//
//        if (StringUtils.isBlank(kx_order_id)) {
//            Logger.error("invalid kx_order_id: %s", kx_order_id);
//            errorInfo.errorCode = ErrorCode.ORDER_NOT_EXITED;
//            errorInfo.errorDes = "订单不存在！";
//            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
//            render("/DDOrderAPI/error.xml", errorInfo);
//        }
//
//        //校验参数
//        if (StringUtils.isBlank(sign) || !DDAPIUtil.validSign(params, sign)) {
//            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
//            errorInfo.errorDes = "sign验证失败！";
//            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
//            render("/DDOrderAPI/error.xml", errorInfo);
//        }
//
//        //定位请求者
//        Resaler resaler = Resaler.find("loginName=? and status=?", DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
//        if (resaler == null) {
//            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
//            errorInfo.errorDes = "当当分销商用户不存在！";
//            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
//            render("/DDOrderAPI/error.xml", errorInfo);
//        }
//
//        Order order = null;
//        //如果已经存在订单，则不处理，直接返回xml
//        DDOrder ddOrder = DDOrder.find("orderId=?", Long.valueOf(kx_order_id)).first();
//        if (ddOrder != null && ddOrder.ybqOrder != null) {
//            order = Order.findOneByUser(ddOrder.ybqOrder.orderNumber, resaler.id, AccountType.RESALER);
//            if (order != null) {
//                Logger.info("[DDOrderAPI] order has existed,and render xml");
//                List<ECoupon> eCouponList = order.eCoupons;
//                render(order, ddgid, kx_order_id, eCouponList);
//            }
//        }
//        //产生DD订单
//        ddOrder = new DDOrder(Long.parseLong(kx_order_id), new BigDecimal(all_amount), new BigDecimal(amount), new BigDecimal(express_fee), resaler.id).save();
//        try {
//            JPA.em().flush();
//        } catch (Exception e) {
//            errorInfo.errorCode = ErrorCode.ORDER_EXCEPTION;
//            errorInfo.errorDes = "订单处理异常！";
//            render("/DDOrderAPI/error.xml", errorInfo);
//        }
//
//        JPA.em().refresh(ddOrder, LockModeType.PESSIMISTIC_WRITE);
//        order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);
//
//        //分解有几个商品，每个商品购买的数量
//        String[] arrGoods = options.split(",");
//
//        String[] arrGoodsItem = null;
//        for (String goodsItem : arrGoods) {
//            arrGoodsItem = goodsItem.split(":");
//            if (arrGoodsItem != null) {
//                Goods goods = Goods.findById(Long.parseLong(arrGoodsItem[0]));
//                BigDecimal resalerPrice = goods.getResalePrice(ResalerLevel.NORMAL);
//                try {
//                    //创建一百券订单Items
//                    order.addOrderItem(goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, resalerPrice, resalerPrice);
//                } catch (NotEnoughInventoryException e) {
//                    Logger.info("inventory not enough");
//                    errorInfo.errorCode = ErrorCode.INVENTORY_NOT_ENOUGH;
//                    errorInfo.errorDes = "库存不足！";
//                    render(errorInfo);
//                }
//            }
//        }
//
//        order.remark = express_memo;
//        order.createAndUpdateInventory();
//        order.accountPay = order.needPay;
//        order.discountPay = BigDecimal.ZERO;
//        order.payMethod = PaymentSource.getBalanceSource().code;
//        order.payAndSendECoupon();
//        //设置当当订单中的一百券订单
//        ddOrder.ybqOrder = order;
//        for (OrderItems ybqItem : order.orderItems) {
//            //创建当当的订单Items
//            ddOrder.addOrderItem(ybqItem.goods, ddgid, ybqItem.buyNumber.intValue(), user_mobile, ybqItem.resalerPrice, ybqItem).save();
//        }
//        ddOrder.status = DDOrderStatus.ORDER_FINISH;
//        ddOrder.save();
//        Logger.info("/n [DDOrderAPI] begin ");
//        render(order, ddgid, kx_order_id);
//    }
//}