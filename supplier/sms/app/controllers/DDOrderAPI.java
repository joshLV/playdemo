package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.dangdang.*;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Controller;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午3:59
 */
public class DDOrderAPI extends Controller {
    public static String DD_LOGIN_NAME = Play.configuration.getProperty("dangdang.resaler_login_name", "dangdang");

    public static void order(String sign) {
        //取得参数信息 必填信息
        Map<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
        String id = params.get("id");
        String all_amount = params.get("all_amount");
        String amount = params.get("amount");
        String user_mobile = params.get("user_mobile");
        String options = params.get("options");
        String express_memo = params.get("express_memo");
        String express_fee = params.get("express_fee");
        String user_id = params.get("user_id");
        String kx_order_id = params.get("kx_order_id");

        ErrorInfo errorInfo = new ErrorInfo();
        //检查参数
        if (StringUtils.isBlank(params.get("user_mobile")) || StringUtils.isBlank(user_id)) {
            Logger.error("invalid userInfo: %s", user_id);
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户不存在！";
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
        if (StringUtils.isBlank(sign)) {
            Logger.error("invalid sign: %s", sign);
            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign不存在！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        //校验参数
        if (!DDAPIUtil.validSign(params, sign)) {
            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign验证失败！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        //定位请求者
        Resaler resaler = Resaler.find("loginName=? and status=?", DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
        if (resaler == null || resaler.status != ResalerStatus.APPROVED) {
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户不存在！";
            Logger.error("errorInfo.errorDes: " + errorInfo.errorDes);
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        Order order = null;
        //如果已经存在订单，则不处理，直接返回xml
        DDOrder ddOrder = DDOrder.find("orderId=?", Long.valueOf(kx_order_id)).first();
        if (ddOrder != null && ddOrder.ybqOrder != null) {
            order = Order.findOneByUser(ddOrder.ybqOrder.orderNumber, resaler.id, AccountType.RESALER);
            if (order != null) {
                render(order, id, kx_order_id);
            }
        }
        //产生DD订单
        ddOrder = new DDOrder(Long.parseLong(kx_order_id), new BigDecimal(all_amount), new BigDecimal(amount), new BigDecimal(express_fee), resaler.id).save();
        try {
            JPA.em().flush();
        } catch (Exception e) {
            errorInfo.errorCode = ErrorCode.ORDER_EXITED;
            errorInfo.errorDes = "订单已存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        JPA.em().refresh(ddOrder, LockModeType.PESSIMISTIC_WRITE);

        order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);

        //分解有几个商品，每个商品购买的数量
        String[] arrGoods = options.split(",");

        String[] arrGoodsItem = null;
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
        ddOrder.status = DDOrderStatus.ORDER_SEND;
        order.save();
        //设置当当订单中的一百券订单
        ddOrder.ybqOrder = order;
        for (OrderItems ybqItem : order.orderItems) {
            //创建当当的订单Items
            try {
                ddOrder.addOrderItem(ybqItem.goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, ybqItem.resalerPrice, ybqItem);
                ddOrder.save();
            } catch (NotEnoughInventoryException e) {
                Logger.info("inventory not enough");
                errorInfo.errorCode = ErrorCode.INVENTORY_NOT_ENOUGH;
                errorInfo.errorDes = "库存不足！";
                render(errorInfo);
            }
        }

        ddOrder.createAndUpdateInventory();

        render(order, id, kx_order_id);
    }
}
