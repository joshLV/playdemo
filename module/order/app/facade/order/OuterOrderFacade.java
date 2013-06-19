package facade.order;

import facade.order.vo.OuterOrderItemVO;
import facade.order.vo.OuterOrderResult;
import facade.order.vo.OuterOrderResultCode;
import facade.order.vo.OuterOrderVO;
import models.accounts.PaymentSource;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.Order;
import models.order.OrderItems;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import org.w3c.dom.Node;
import play.Logger;
import play.db.jpa.JPA;
import play.libs.XPath;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 外部订单生成功能Facade.
 */
public class OuterOrderFacade {

    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1\\d{10}$";

    public static OuterOrderResult createOuterOrder(OuterOrderVO outerOrderVO) {

        //检查订单总额是否匹配
        if (checkTotalAmount(outerOrderVO)) {
            recordResultMessage(203, "the total amount does not match the team price and count");
        }

        //检查手机号
        if (!checkPhone(outerOrderVO.mobile)) {
            recordResultMessage(204, "invalid mobile: " + outerOrderVO.mobile);
        }

        OuterOrderPartner outerOrderPartner = getOuterOrderPartner(outerOrderVO.resaler.partner);
        if (outerOrderPartner == null) {
            return buildOuterOrderResult(OuterOrderResultCode.INVALID_PARTNER);
        }


        List<Node> jdCoupons = message.selectNodes("./Coupons/Coupon");

        //检查并保存此新请求
        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", outerOrderPartner, outerOrderVO.outerOrderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = outerOrderPartner;
            outerOrder.resaler = Resaler.findApprovedByLoginName(Resaler.JD_LOGIN_NAME);
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.orderId = outerOrderVO.outerOrderId;
            outerOrder.message = outerOrderVO.message;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderId 的订单，则放弃
                recordResultMessage(205, "there is another parallel request");
            }
        }
        // TODO: 考虑申请OuterOrder行锁后再处理订单

        //生成一百券订单
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            Order ybqOrder = outerOrder.ybqOrder;
            if (ybqOrder == null) {

                Goods goods = ResalerProduct.getGoods(outerOrderVO.resaler, teamId, OuterOrderPartner.JD);
                if (goods == null) {
//            finish(208, "can not find goods: " + venderTeamId);
                    return null;
                }
                if (goods.originalPrice.compareTo(teamPrice) > 0) {
//            finish(209, "invalid product price: " + teamPrice);
                }

                //检查导入券订单库存
                if (goods.hasEnoughInventory(count)) {
                    JPA.em().getTransaction().rollback();
                    Logger.info("inventory not enough,goods.id=%s", goods.id.toString());
//            finish(210, "inventory not enough");
                }

                ybqOrder = createYbqOrder(goods, teamPrice, count, mobile);
            }
            outerOrder.status = OuterOrderStatus.ORDER_DONE;
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.save();
        }
        //保存京东的券号密码
        List<ECoupon> coupons = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        if (outerOrder.status == OuterOrderStatus.ORDER_DONE || outerOrder.status == OuterOrderStatus.ORDER_SYNCED) {
            if (coupons.size() != jdCoupons.size()) {
                recordResultMessage(211, "coupon size not matched, ybq size: " + coupons.size() + " jd size:" + jdCoupons.size());
            }
            // 保存京东的券号密码
            for (int i = 0; i < coupons.size(); i++) {
                ECoupon coupon = coupons.get(i);
                Node jdCoupon = jdCoupons.get(i);

                coupon.partner = ECouponPartner.JD;
                coupon.partnerCouponId = XPath.selectText("./CouponId", jdCoupon).trim();
                coupon.partnerCouponPwd = XPath.selectText("./CouponPwd", jdCoupon).trim();
                coupon.save();
            }
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
        }

        return null;
    }

    /**
     * 检查总价是否合法.
     * @param outerOrderVO
     * @return
     */
    private static boolean checkTotalAmount(OuterOrderVO outerOrderVO) {
        BigDecimal itemTotalAmount = BigDecimal.ZERO;

        for (OuterOrderItemVO itemVO : outerOrderVO.orderItems) {
            itemTotalAmount = itemTotalAmount.add(itemVO.price.multiply(new BigDecimal(itemVO.count)));
        }

        return outerOrderVO.totalAmount.compareTo(itemTotalAmount) == 0;
    }

    private static OuterOrderResult buildOuterOrderResult(OuterOrderResultCode resultCode) {
        OuterOrderResult result = new OuterOrderResult();
        result.resultCode = resultCode;
        result.successed = (resultCode == OuterOrderResultCode.SUCCESS);
        return result;
    }

    private static void recordResultMessage(int resultCode, String resultMessage) {
    }

    private static boolean checkPhone(String phone) {
        if (phone == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    private static OuterOrderPartner getOuterOrderPartner(String partner) {
        if ("JD".equals(partner)) {
            return OuterOrderPartner.JD;
        }
        return null;
    }

    // 创建一百券订单
    private static Order createYbqOrder(Goods goods, BigDecimal teamPrice, long count, String mobile) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);
        Logger.info("create ybq order");
        if (resaler == null) {
            //finish(207, "can not find the jingdong resaler");
        }
        Order ybqOrder = Order.createResaleOrder(resaler);
        ybqOrder.save();

        OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, count, mobile, teamPrice, teamPrice);
        uhuilaOrderItem.save();
        if (goods.materialType.equals(MaterialType.REAL)) {
            ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
            ybqOrder.deliveryType = DeliveryType.SMS;
        }

        ybqOrder.createAndUpdateInventory();
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        ybqOrder.payAndSendECoupon();
        ybqOrder.save();

        return ybqOrder;
    }
}
