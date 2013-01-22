package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.dangdang.DDAPIUtil;
import models.dangdang.DDOrderItem;
import models.dangdang.ErrorCode;
import models.dangdang.ErrorInfo;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
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
    public static final String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static void order() {
        Logger.info("[DDOrderAPI] begin ");
        //取得参数信息 必填信息
        SortedMap<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
        String id = StringUtils.trimToEmpty(params.get("id")); // 一百券的商品ID
        String ddgid = StringUtils.trimToEmpty(params.get("team_id"));//当当商品编号（对应一百券的商品ID）
        String user_mobile = StringUtils.trimToEmpty(params.get("user_mobile"));
        String options = StringUtils.trimToEmpty(params.get("options"));
        String express_memo = StringUtils.trimToEmpty(params.get("express_memo"));
        String user_id = StringUtils.trimToEmpty(params.get("user_id"));
        String kx_order_id = StringUtils.trimToEmpty(params.get("kx_order_id"));
        BigDecimal amount = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(params.get("amount"))) {
            amount = new BigDecimal(StringUtils.trimToEmpty(params.get("amount")));
        }
        String sign = StringUtils.trimToEmpty(params.get("sign")).toLowerCase();
        ErrorInfo errorInfo = new ErrorInfo();
        //检查参数
        if (StringUtils.isBlank(user_mobile) || StringUtils.isBlank(user_id)) {
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
            errorInfo.errorDes = "用户或手机不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        if (StringUtils.isBlank(kx_order_id)) {
            errorInfo.errorCode = ErrorCode.ORDER_NOT_EXITED;
            errorInfo.errorDes = "订单不存在！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }
        //校验参数
        if (StringUtils.isBlank(sign) || !DDAPIUtil.validSign(params, sign)) {
            errorInfo.errorCode = ErrorCode.VERIFY_FAILED;
            errorInfo.errorDes = "sign验证失败！";
            render("/DDOrderAPI/error.xml", errorInfo);
        }

        //定位请求者
        Resaler resaler = Resaler.find("loginName=? and status=?", Resaler.DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
        Long resalerId = null;
        if (resaler == null) {
            errorInfo.errorCode = ErrorCode.USER_NOT_EXITED;
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
                    errorInfo.errorCode = ErrorCode.INVENTORY_NOT_ENOUGH;
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
                new DDOrderItem(Long.valueOf(kx_order_id), ddgid, ybqItem.goods, ybqItem.buyNumber, ybqItem).save();
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
}

