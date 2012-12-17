package controllers;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.TradeType;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.taobao.TaobaoCouponUtil;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 处理已消费券的退款。
 * 
 * @author tanglq
 *
 */
@With(OperateRbac.class)
@ActiveNavigation("verified_ecoupon_refund")
public class VerifiedECouponRefunds extends Controller {

    public static void index() {
        render();
    }
    
    public static void refund(String eCouponSn, String refundComment, String choice) {
        String message = null;
        if (StringUtils.isBlank(eCouponSn)) {
            message = "券号不能为空";
            render(message);
        }
        if (StringUtils.isBlank(refundComment)) {
            message = "备注不能为空";
            render(message);
        }
        
        ECoupon ecoupon = ECoupon.find("eCouponSn=?", eCouponSn).first();
        
        if (ecoupon == null || ecoupon.status != ECouponStatus.CONSUMED) {
            message = "不存在的券号或券号未验证:" + eCouponSn;
            render(message);
        }

        if ("REFUND".equals(choice)) {
            message = applyRefund(ecoupon, refundComment);
        } else if ("UNCONSUME".equals(choice)) {
            message = applyUnConsumed(ecoupon, refundComment);
        } else {
            message = "请输入REFUND或UNCONSUME。";
        }
        
        render(ecoupon, message);
    }

    /**
     * 直接退款，券状态为『已退款』。
     * @param eCoupon
     * @param refundComment
     * @return
     */
    private static String applyRefund(ECoupon eCoupon, String refundComment) {
        AccountType accountType = eCoupon.order.userType;
        Long userId = eCoupon.order.userId;

        if (accountType != AccountType.CONSUMER && accountType != AccountType.RESALER) {
            return "不支持的券类别，请检查";
        }

        // 查找原订单信息 可能是分销账户，也可能是消费者账户
        Account userAccount = AccountUtil.getAccount(userId, accountType);

        // 计算需要退款的活动金金额
        // 计算方法：本订单中，抛开已消费的和已经退款过的活动金，先退活动金
        // 例如，订单金额100，用活动金支付40，用余额支付60，若此时退款，则首先退到活动金中。
        // 如果已经消费了20， 那仍然首先退到活动金，但是最多退40-20=20元，也就是说，视用户消费时首先消费的是活动金

        BigDecimal promotionAmount = BigDecimal.ZERO;
        BigDecimal consumedAmount = BigDecimal.ZERO;

        //退款金额为该券金额减去折扣金额
        BigDecimal cashAmount = ECoupon.getLintRefundPrice(eCoupon);

        if (eCoupon.order.refundedPromotionAmount == null) {
            eCoupon.order.refundedPromotionAmount = BigDecimal.ZERO;
        }
        List<ECoupon> eCoupons = ECoupon.find("byOrderAndStatus",
                eCoupon.order, ECouponStatus.CONSUMED).fetch();
        for (ECoupon c : eCoupons) {
            consumedAmount = consumedAmount.add(c.salePrice);
        }
        BigDecimal usedPromotionAmount = eCoupon.order.refundedPromotionAmount
                .add(consumedAmount);
        if (eCoupon.order.promotionBalancePay != null
                && eCoupon.order.promotionBalancePay
                .compareTo(usedPromotionAmount) > 0) {
            promotionAmount = cashAmount.min(eCoupon.order.promotionBalancePay
                    .subtract(usedPromotionAmount));
            cashAmount = cashAmount.subtract(promotionAmount);
        }

        Account supplierAccount = AccountUtil.getSupplierAccount(eCoupon.goods.supplierId);

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount           = supplierAccount; //付款方为商户账户
        tradeBill.toAccount             = AccountUtil.getPlatformCommissionAccount();                                  //收款方为指定账户
        tradeBill.balancePaymentAmount  = eCoupon.originalPrice;                                   //使用可提现余额来支付退款的金额
        tradeBill.ebankPaymentAmount    = BigDecimal.ZERO;                          //不使用网银支付
        tradeBill.uncashPaymentAmount   = BigDecimal.ZERO;                          //不使用不可提现余额支付
        tradeBill.promotionPaymentAmount= promotionAmount;                          //使用活动金余额来支付退款的金额
        tradeBill.tradeType             = TradeType.REFUND;                         //交易类型为退款
        tradeBill.orderId               = eCoupon.order.id;                                  //冗余订单ID
        tradeBill.eCouponSn             = eCoupon.eCouponSn;                                //冗余券编号
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount)
                .add(promotionAmount);

        tradeBill.save();

        if (!TradeUtil.success(tradeBill,
                "券" + eCoupon.getMaskedEcouponSn() + "因" + refundComment + "被" + OperateRbac.currentUser().userName + "操作退款")) {
            throw new RuntimeException("商户退款失败:" + eCoupon.eCouponSn);
        }

        TradeBill rabateTrade = TradeUtil.createTransferTrade(
                AccountUtil.getPlatformCommissionAccount(), userAccount,
                cashAmount, BigDecimal.ZERO);
        rabateTrade.orderId = eCoupon.order.id;

        if (!TradeUtil.success(rabateTrade, "券" + eCoupon.getMaskedEcouponSn() + "因" + refundComment + "被" + OperateRbac.currentUser().userName + "操作退款")) {
            throw new RuntimeException("退款失败:" + eCoupon.eCouponSn);
        }

        // 更新已退款的活动金金额
        if (promotionAmount.compareTo(BigDecimal.ZERO) > 0) {
            eCoupon.order.refundedPromotionAmount = eCoupon.order.refundedPromotionAmount
                    .add(promotionAmount);
            eCoupon.order.save();
        }

        String userName = OperateRbac.currentUser().userName;

        //记录券历史信息
        new CouponHistory(eCoupon, userName, "已消费券退款:" + refundComment, eCoupon.status, ECouponStatus.REFUND, null).save();

        // 更改订单状态
        eCoupon.status = ECouponStatus.REFUND;
        eCoupon.refundAt = new Date();
        eCoupon.refundPrice = cashAmount;
        eCoupon.save();

        return "退款成功，状态为已退款:" + eCoupon.eCouponSn + ", status:" + eCoupon.status;
    }

    /**
     * 取消验证，状态返回为『未消费』.
     * @param eCoupon
     * @param refundComment
     * @return
     */
    private static String applyUnConsumed(ECoupon eCoupon, String refundComment) {
        AccountType accountType = eCoupon.order.userType;
        if (eCoupon.status != ECouponStatus.CONSUMED) {
            return "必须是已经消费的券";
        }

        if (accountType != AccountType.CONSUMER && accountType != AccountType.RESALER) {
            return "不支持的券类别，请检查";
        }

        if (eCoupon.partner == ECouponPartner.TB) {
            if (!TaobaoCouponUtil.reverseOnTaobao(eCoupon)) {
                return "在淘宝上撤销失败！";
            }
        }

        /**
         * 以下过程完全于ECoupon 的 付款后发佣金的过程相反(金额都用.negate()方法取负值)，需要完全相反
         */
        boolean reverse = true;

        Account supplierAccount = AccountUtil
                .getSupplierAccount(eCoupon.orderItems.goods.supplierId);
        // 给商户打钱
        TradeBill consumeTrade = TradeUtil.createConsumeTrade(eCoupon.eCouponSn,
                supplierAccount, eCoupon.originalPrice, eCoupon.order.getId(), reverse);
        TradeUtil.success(consumeTrade, "已消费退款：" + refundComment + "。" + eCoupon.order.description);

        BigDecimal platformCommission = BigDecimal.ZERO;

        if (eCoupon.salePrice.compareTo(eCoupon.resalerPrice) < 0) {
            // 如果成交价小于分销商成本价（这种情况只有在一百券网站上才会发生），
            // 那么一百券就没有佣金，平台的佣金也变为成交价减成本价
            platformCommission = eCoupon.salePrice.subtract(eCoupon.originalPrice);
        } else {
            // 平台的佣金等于分销商成本价减成本价
            platformCommission = eCoupon.resalerPrice.subtract(eCoupon.originalPrice);
            // 如果是在一百券网站下的单，还要给一百券佣金
            if (eCoupon.order.userType == AccountType.CONSUMER) {
                TradeBill uhuilaCommissionTrade = TradeUtil
                        .createCommissionTrade(
                                AccountUtil.getUhuilaAccount(),
                                eCoupon.salePrice.subtract(eCoupon.resalerPrice),
                                eCoupon.eCouponSn,
                                eCoupon.order.getId(),reverse);

                TradeUtil.success(uhuilaCommissionTrade, "已消费退款：" + refundComment + "。" + eCoupon.order.description);
            }
        }

        if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
            // 给优惠券平台佣金
            TradeBill platformCommissionTrade = TradeUtil
                    .createCommissionTrade(
                            AccountUtil.getPlatformCommissionAccount(),
                            platformCommission,
                            eCoupon.eCouponSn,
                            eCoupon.order.getId(),reverse);
            TradeUtil.success(platformCommissionTrade,"已消费退款：" + refundComment + "。" + eCoupon.order.description);
        }

        if (eCoupon.rebateValue != null && eCoupon.rebateValue.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill rabateTrade = TradeUtil.createTransferTrade(
                    AccountUtil.getPlatformIncomingAccount(),
                    AccountUtil.getUhuilaAccount(),
                    eCoupon.rebateValue, BigDecimal.ZERO);
            rabateTrade.orderId = eCoupon.order.id;
            TradeUtil.success(rabateTrade, "已消费退款：" + refundComment + "。活动折扣费" + eCoupon.rebateValue);
        } else if (eCoupon.salePrice.compareTo(eCoupon.originalPrice) < 0) {
            BigDecimal detaPrice = eCoupon.originalPrice.subtract(eCoupon.salePrice);
            // 如果售价低于进价，从活动金账户出
            TradeBill rabateTrade = TradeUtil.createTransferTrade(
                    AccountUtil.getPlatformIncomingAccount(),
                    AccountUtil.getPromotionAccount(),
                    detaPrice, BigDecimal.ZERO);
            rabateTrade.orderId = eCoupon.order.id;
            TradeUtil.success(rabateTrade, "已消费退款：" + refundComment +  "。低价销售补贴" + detaPrice);
        }
        /**
         * 后面还有推荐返利的暂时不弄
         */


        String userName = OperateRbac.currentUser().userName;

        //记录券历史信息
        new CouponHistory(eCoupon, userName, "已消费券退款:" + refundComment, eCoupon.status, ECouponStatus.REFUND, null).save();

        // 更改券状态
        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.consumedAt = null;
        eCoupon.shop = null;
        eCoupon.verifyType = null;
        eCoupon.operateUserId = null;
        eCoupon.triggerCouponSn = null;
        eCoupon.save();

        return "退款成功，状态为未消费:" + eCoupon.eCouponSn + ", status:" + eCoupon.status;
    }

}
