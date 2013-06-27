package facade.order;

import facade.order.vo.OuterECouponVO;
import facade.order.vo.OuterOrderItemVO;
import facade.order.vo.OuterOrderResult;
import facade.order.constant.OuterOrderResultCode;
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
import play.Logger;
import play.db.jpa.JPA;
import util.transaction.RemoteRecallCheck;
import util.transaction.TransactionCallback;
import util.transaction.TransactionRetry;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 外部订单生成功能Facade.
 */
public class OuterOrderFacade {

    public static String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    public static String PHONE_REGEX = "^1\\d{10}$";


    public static OuterOrderResult createOuterOrder(final OuterOrderVO outerOrderVO) {

        RemoteRecallCheck.setId("OuterOrderFacade." + outerOrderVO.outerOrderId);
        // 事务重试.
        return TransactionRetry.run(new TransactionCallback<OuterOrderResult>() {
            @Override
            public OuterOrderResult doInTransaction() {
                return doCreateOuterOrder(outerOrderVO);
            }
        });
    }


    private static OuterOrderResult doCreateOuterOrder(OuterOrderVO outerOrderVO) {
        //检查订单总额是否匹配
        if (!checkTotalAmount(outerOrderVO)) {
            return OuterOrderResult.build(OuterOrderResultCode.UNBALANCE_TOTAL_AMOUNT,
                    "OuterOrder(orderId:" + outerOrderVO.outerOrderId + "订单总金额与OrderItem不一致");
        }

        //检查手机号
        if (!checkPhone(outerOrderVO.mobile)) {
            return OuterOrderResult.build(OuterOrderResultCode.INVALID_MOBILE, "无效的手机号：" + outerOrderVO.mobile);
        }

        OuterOrderPartner outerOrderPartner = getOuterOrderPartner(outerOrderVO.resaler.partner);
        if (outerOrderPartner == null) {
            return OuterOrderResult.build(OuterOrderResultCode.INVALID_PARTNER,
                    "Resaler(id:" + outerOrderVO.resaler.id + ")的partner值(" + outerOrderVO.resaler.partner + ")找不到对应的渠道代码");
        }

        // 检查传入的券列表size是否与订购数量一致.
        if (!checkCouponSize(outerOrderVO)) {
            //recordResultMessage(211, "coupon size not matched, ybq size: " + coupons.size() + " jd size:" + jdCoupons.size());
        }


        // 检查产品是否有映射.
        Map<Long, Goods> venderTeamGoodsMap = new HashMap<>();
        for (OuterOrderItemVO itemVO : outerOrderVO.orderItems) {
            if (itemVO.count <= 0) {
                return OuterOrderResult.build(OuterOrderResultCode.INVALID_BUY_COUNT,
                        "TeamId(" + itemVO.venderTeamId + ")的购买数量(" + itemVO.count
                                + ")必须大于0");
            }
            Goods goods = ResalerProduct.getGoods(outerOrderVO.resaler, itemVO.venderTeamId, outerOrderPartner);
            if (goods == null) {
                return OuterOrderResult.build(OuterOrderResultCode.NOT_FOUND_GOODS,
                        "找不到teamId(" + itemVO.venderTeamId + ")对应的商品");
            }

            if (goods.originalPrice.compareTo(itemVO.price) > 0) {
                // 售价不能低于进价
                return OuterOrderResult.build(OuterOrderResultCode.INVALID_PRICE,
                        "TeamId(" + itemVO.venderTeamId + ")的售价(" + itemVO.price
                                + ")不能低于商品(id:" + goods.id + ")的进价" + goods.originalPrice);
            }

            //检查商品库存，目前只会检查导入券的库存
            if (goods.hasEnoughInventory(itemVO.count)) {
                // JPA.em().getTransaction().rollback(); //TODO: 考虑引入goods销售锁定
                Logger.info("inventory not enough,goods.id=%s", goods.id.toString());
                return OuterOrderResult.build(OuterOrderResultCode.INVENTORY_NOT_ENOUGH,
                        "商品(id:" + goods.id + ")库存不足");
            }

            venderTeamGoodsMap.put(itemVO.venderTeamId, goods);
        }

        // --------------------- 分隔线： 前面只是数据检查(不会修改数据库），之后的代码会修改数据库 --------------------------

        OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", outerOrderPartner, outerOrderVO.outerOrderId).first();
        //如果找不到该orderCode的订单，说明还没有新建，则新建一个
        if (outerOrder == null) {
            outerOrder = new OuterOrder();
            outerOrder.partner = outerOrderPartner;
            outerOrder.resaler = outerOrderVO.resaler;
            outerOrder.status = OuterOrderStatus.ORDER_COPY;
            outerOrder.orderId = outerOrderVO.outerOrderId;
            outerOrder.message = outerOrderVO.message;
            outerOrder.save();
            try { // 将订单写入数据库
                JPA.em().flush();
            } catch (Exception e) { // 如果写入失败，说明 已经存在一个相同的orderId 的订单，则放弃
                e.printStackTrace();
                return OuterOrderResult.build(OuterOrderResultCode.CONCURRENCY_REQUEST, "并发请求:" + outerOrderVO.outerOrderId);
            }
        }
        // TODO: 考虑申请OuterOrder行锁后再处理订单
        //生成一百券订单
        if (outerOrder.status == OuterOrderStatus.ORDER_COPY) {
            Order ybqOrder = outerOrder.ybqOrder;
            if (ybqOrder == null) {
                ybqOrder = createYbqOrder(outerOrderVO, venderTeamGoodsMap);
            }
            outerOrder.status = OuterOrderStatus.ORDER_DONE;
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.save();
        }

        OuterOrderResult outerOrderResult = OuterOrderResult.build(OuterOrderResultCode.SUCCESS, "调用成功:" + outerOrderVO.outerOrderId).outerOrder(outerOrder);
        //保存合作方的券号密码
        if (outerOrder.status == OuterOrderStatus.ORDER_DONE || outerOrder.status == OuterOrderStatus.ORDER_SYNCED) {
            for (OuterOrderItemVO itemVO : outerOrderVO.orderItems) {
                Goods goods = venderTeamGoodsMap.get(itemVO.venderTeamId);
                List<ECoupon> coupons = ECoupon.find("order = ? and goods = ?", outerOrder.ybqOrder, goods).fetch();
                // 加入所有生成的券.
                for (ECoupon eCoupon : coupons) {
                    outerOrderResult.addECoupon(eCoupon);
                }

                if (itemVO.eCoupons != null && itemVO.eCoupons.size() > 0) {
                    for (int i = 0; i < itemVO.eCoupons.size(); i++) {
                        Logger.info("coupos.size=%d, i=%d", coupons.size(), i);
                        ECoupon coupon = coupons.get(i);
                        OuterECouponVO outerECouponVO = itemVO.eCoupons.get(i);

                        coupon.partner = getECouponPartner(outerOrderVO.resaler.partner);
                        coupon.partnerCouponId = outerECouponVO.eCouponSN;
                        coupon.partnerCouponPwd = outerECouponVO.eCouponPassword;
                        coupon.save();

                    }
                }
            }
            outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
            outerOrder.save();
        }

        return outerOrderResult;
    }

    /**
     * 检查
     * @param outerOrderVO
     * @return
     */
    private static boolean checkCouponSize(OuterOrderVO outerOrderVO) {
        for (OuterOrderItemVO itemVO : outerOrderVO.orderItems) {
            if (itemVO.eCoupons.size() != itemVO.count) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查总价是否合法.
     *
     * @param outerOrderVO
     * @return
     */
    private static boolean checkTotalAmount(OuterOrderVO outerOrderVO) {
        BigDecimal itemTotalAmount = BigDecimal.ZERO;

        for (OuterOrderItemVO itemVO : outerOrderVO.orderItems) {
            itemTotalAmount = itemTotalAmount.add(itemVO.price.multiply(new BigDecimal(itemVO.count)));
        }

        Logger.info("outerOrderVO.totalAmount: %s  itemTotalAmount: %s", outerOrderVO.totalAmount, itemTotalAmount);
        return outerOrderVO.totalAmount.compareTo(itemTotalAmount) == 0;
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

    private static ECouponPartner getECouponPartner(String partner) {
        if ("JD".equals(partner)) {
            return ECouponPartner.JD;
        }
        return null;
    }

    // 创建一百券订单
    private static Order createYbqOrder(OuterOrderVO outerOrderVO, Map<Long, Goods> goodsMap) {
        Resaler resaler = Resaler.findOneByLoginName(Resaler.JD_LOGIN_NAME);

        Order ybqOrder = Order.createResaleOrder(resaler);
        ybqOrder.save();

        for (OuterOrderItemVO itemVO : outerOrderVO.orderItems) {
            Goods goods = goodsMap.get(itemVO.venderTeamId);
            OrderItems uhuilaOrderItem = ybqOrder.addOrderItem(goods, itemVO.count, outerOrderVO.mobile, itemVO.price, itemVO.price);
            uhuilaOrderItem.save();
            if (goods.materialType.equals(MaterialType.REAL)) {
                ybqOrder.deliveryType = DeliveryType.LOGISTICS;
            } else if (goods.materialType.equals(MaterialType.ELECTRONIC)) {
                ybqOrder.deliveryType = DeliveryType.SMS;
            }
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
