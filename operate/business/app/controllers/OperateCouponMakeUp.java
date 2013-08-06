package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.TradeType;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import util.extension.ExtensionResult;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
        System.out.println("orderIds = " + orderIds);

        if (StringUtils.isBlank(orderIds)) {
            renderText("请输入orderIds,多个orderIds请用半角逗号分割");
            return;
        }
        StringBuilder successMessage = new StringBuilder("成功");
        StringBuilder failMessage = new StringBuilder("失败：\n");

        String[] orderIdList = orderIds.trim().split(",");
        System.out.println("orderIdList = " + orderIdList);

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

}
