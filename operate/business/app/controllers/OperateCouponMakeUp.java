package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.TradeType;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import util.extension.ExtensionResult;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-12-21
 */

@With(OperateRbac.class)
public class OperateCouponMakeUp extends Controller {
    public static void index(String partner, String coupon) {
        if (StringUtils.isBlank(partner) || StringUtils.isBlank(coupon)) {
            renderText("请输入partner和coupon,多个coupon请用半角逗号分割");
            return;
        }

        String[] couponList = coupon.trim().split(",");
        StringBuilder successMessage = new StringBuilder("成功：");
        StringBuilder failMessage = new StringBuilder("失败：\n");
        for (String c : couponList) {
            ECoupon eCoupon = ECoupon.find("byECouponSn", c).first();
            if (eCoupon == null) {
                failMessage.append(c).append(" 没有找到\n");
                continue;
            }
            if (eCoupon.status != ECouponStatus.CONSUMED) {
                failMessage.append(c).append(" 状态不是已消费\n");
                continue;
            }

            ExtensionResult result = eCoupon.verifyAndCheckOnPartnerResaler();

            if (result.isOk()) {
                successMessage.append(c).append(",");
            } else {
                failMessage.append(c).append(" 在第三方消费失败-").append(result.toString()).append("\n");
            }
        }

        renderText("输入：" + coupon + "\n"
                + successMessage + "\n"
                + failMessage);
    }

    public static void pay(String orderIds) {
        if (StringUtils.isBlank(orderIds)) {
            renderText("请输入orderIds,多个orderIds请用半角逗号分割");
            return;
        }
        StringBuilder successMessage = new StringBuilder("成功");
        StringBuilder failMessage = new StringBuilder("失败：\n");

        String[] orderIdList = orderIds.trim().split(",");

        List<Long> orderIdsResult = new ArrayList<>();
        for (String orderId : orderIdList) {
            Object objOrderId = Cache.get("order_" + orderId);
            String cacheOrderId = objOrderId == null ? "" : objOrderId.toString();
            if (cacheOrderId != null && cacheOrderId.equals(orderId)) {
                failMessage.append(orderId).append(" 已经处理过\n");
                continue;
            }
            OrderItems orderItems = OrderItems.find("order.id = ?", Long.valueOf(orderId)).first();
            Account supplierAccount = Account.find("accountType = ? and uid = ?", AccountType.SUPPLIER,
                    orderItems.goods.supplierId).first();
            AccountSequence sequence = AccountSequence.find("orderId = ? and tradeType = ? and account = ?",
                    Long.valueOf(orderId), TradeType.PURCHASE_COSTING, supplierAccount).first();
            if (sequence != null) {
                failMessage.append(orderId).append(" 已经处理过\n");
                continue;
            }
            Cache.set("order_" + orderId, orderId, "1h");
            successMessage.append(orderId).append(",");
            orderIdsResult.add(Long.valueOf(orderId));
        }

        if (orderIdsResult.size() > 0) {
            EntityManager entityManager = JPA.em();
            Query q = entityManager.createQuery("SELECT o FROM OrderItems o  WHERE " +
                    " o.order.id in (:orderIds)");
            q.setParameter("orderIds", orderIdsResult);
            List<OrderItems> orderItemsList = q.getResultList();
            for (OrderItems item : orderItemsList) {
                item.realGoodsPayCommission();
            }
        }

        renderText(successMessage.append("\n").append(failMessage));
    }

    public static void payCommissionToSupplierOfReal(String orderItemIds) {
        StringBuilder successMessage = new StringBuilder("成功");
        if (StringUtils.isBlank(orderItemIds)) {
            renderText("orderItemIds,多个orderItemIds请用半角逗号分割");
            return;
        }
        String[] orderItemIdList = orderItemIds.trim().split(",");

        for (String itemId : orderItemIdList) {
            OrderItems orderItems =OrderItems.findById(Long.valueOf(itemId));
            Account supplierAccount = orderItems.getSupplierAccount();


            BigDecimal paidToSupplierPrice = orderItems.originalPrice;

            for (int i = 0; i < orderItems.buyNumber; i++) {
                // 给商户打钱
                TradeBill consumeTrade = TradeUtil.consumeTrade(orderItems.order.operator)
                        .toAccount(supplierAccount)
                        .balancePaymentAmount(paidToSupplierPrice)
                        .orderId(orderItems.order.getId())
                        .make();
                TradeUtil.success(consumeTrade, "11月5日实物未发货(补退款)" + orderItems.goods.shortName);

                BigDecimal platformCommission = BigDecimal.ZERO;

                if (orderItems.salePrice.compareTo(orderItems.resalerPrice) < 0) {
                    // 如果成交价小于分销商成本价（这种情况只有在一百券网站上才会发生），
                    // 那么一百券就没有佣金，平台的佣金也变为成交价减成本价
                    platformCommission = orderItems.salePrice.subtract(orderItems.originalPrice);
                } else {
                    // 平台的佣金等于分销商成本价减成本价
                    platformCommission = orderItems.resalerPrice.subtract(orderItems.originalPrice);
                }

                if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
                    // 给优惠券平台佣金
                    TradeBill platformCommissionTrade = TradeUtil.commissionTrade(orderItems.order.operator)
                            .toAccount(AccountUtil.getPlatformCommissionAccount(orderItems.order.operator))
                            .balancePaymentAmount(platformCommission)
                            .orderId(orderItems.order.getId())
                            .make();
                    TradeUtil.success(platformCommissionTrade, orderItems.order.description);
                }
            }
        }
        renderText(successMessage);
    }
    public static void payCommissionToSupplier(String couponIds) {
        StringBuilder successMessage = new StringBuilder("成功");
        StringBuilder failMessage = new StringBuilder("失败：\n");
        if (StringUtils.isBlank(couponIds)) {
            renderText("请输入couponIds,多个couponIds请用半角逗号分割");
            return;
        }
        String[] couponIdsList = couponIds.trim().split(",");
        List<Long> couponIdsResult = new ArrayList<>();

        for (String couponId : couponIdsList) {
            ECoupon coupon = ECoupon.findById(Long.valueOf(couponId));
            Account supplierAccount = Account.find("accountType = ? and uid = ?", AccountType.SUPPLIER,
                    coupon.goods.supplierId).first();
            AccountSequence sequence = AccountSequence.find("orderId = ? and tradeType = ? and account = ?",
                    Long.valueOf(coupon.order.id), TradeType.COMMISSION, supplierAccount).first();
            if (sequence != null) {
                failMessage.append(couponId).append(" 已经处理过\n");
                continue;
            }
            successMessage.append(couponId).append(",");
            couponIdsResult.add(Long.valueOf(couponId));
        }

        for (Long couponId : couponIdsResult) {
            ECoupon coupon = ECoupon.findById(couponId);
            BigDecimal salePrice = coupon.salePrice;
            BigDecimal originalPrice = coupon.originalPrice;
            Order order = coupon.order;
            String eCouponSn = coupon.eCouponSn;
            // 如果成交价小于分销商成本价（这种情况只有在一百券网站上才会发生），
            // 那么一百券就没有佣金，平台的佣金也变为成交价减成本价
            BigDecimal platformCommission = salePrice.subtract(originalPrice);
            Account supplierAccount = Account.find("accountType = ? and uid = ?", AccountType.SUPPLIER,
                    coupon.goods.supplierId).first();
            if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
                // 佣金平台打款给商户
                TradeBill platformCommissionTrade = TradeUtil.commissionTrade(AccountUtil.getPlatformCommissionAccount(order.operator))
                        .toAccount(supplierAccount)
                        .balancePaymentAmount(platformCommission)
                        .coupon(eCouponSn)
                        .orderId(order.getId())
                        .make();
                TradeUtil.success(platformCommissionTrade, order.description);
            }
        }
        renderText(successMessage.append("\n").append(failMessage));
    }
}
