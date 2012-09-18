package controllers;

import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.dangdang.DDOrder;
import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.ErrorInfo;
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
import play.db.jpa.JPA;
import play.mvc.Controller;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午3:59
 */
public class DDOrderAPI extends Controller {
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
        ErrorInfo errorInfo = new ErrorInfo();
        //检查参数
        if (StringUtils.isBlank(params.get("user_mobile")) || StringUtils.isBlank(user_id)) {
            Logger.error("invalid userInfo: %s", user_id);
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }
        String kx_order_id = params.get("kx_order_id");
        if (StringUtils.isBlank(kx_order_id)) {
            Logger.error("invalid kx_order_id: %s", kx_order_id);
            errorInfo.errorCode = ErrorCode.ORDER_NOT_EXITED;
            errorInfo.errorDes = "订单不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);

        }
        if (StringUtils.isBlank(sign)) {
            Logger.error("invalid sign: %s", sign);
            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign验证失败！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        //校验参数
        SortedMap<String, String> veryParams = new TreeMap<>();
        veryParams.put("kx_order_id", kx_order_id);
        if (!DDAPIUtil.validSign(veryParams, sign)) {
            Logger.error("wrong sign: ", sign);
            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign验证失败！";
            render("/DDOrderAPI/error.xml", errorInfo);
            Logger.info(">>>>>>>>>>>>>>>.");
        }
        Order order = null;
        //如果已经存在订单，则不处理，直接返回xml
        DDOrder ddOrder = DDOrder.find("orderId=?", Long.valueOf(kx_order_id)).first();
        if (ddOrder != null) {
            order = Order.find("ddOrder=?", ddOrder).first();
            if (order != null) {
                render(order, id, kx_order_id);
            }
        }

        //定位请求者
        Resaler resaler = Resaler.findOneByLoginName("dangdang");
        if (resaler == null || resaler.status != ResalerStatus.APPROVED) {
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
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
                    OrderItems ybqOrderItem = order.addOrderItem(goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, resalerPrice, resalerPrice);
                    //创建当当的订单Items
                    ddOrder.addOrderItem(goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, resalerPrice, ybqOrderItem);
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
        ddOrder.createAndUpdateInventory();
        Logger.info(">>>>>>>>>>>>>>>.");
        render(order, id, kx_order_id);
    }
}
