package controllers;

import models.accounts.Account;
import models.accounts.TradeBill;
import models.accounts.TradeType;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.kangou.KangouCardStatus;
import models.kangou.KangouUtil;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.taobao.TaobaoCouponUtil;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 处理已消费券的退款。
 *
 * @author tanglq
 */
@With(OperateRbac.class)
@ActiveNavigation("verified_ecoupon_refund")
public class VerifiedECouponRefunds extends Controller {

    public static void index(String eCouponSn) {
        if (!StringUtils.isBlank(eCouponSn)) {
            ECoupon eCoupon = ECoupon.find("byECouponSn", eCouponSn).first();
            renderArgs.put("coupon", eCoupon);
        }
        render(eCouponSn);
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

            if (KangouUtil.SUPPLIER_DOMAIN_NAME.equals(ecoupon.goods.getSupplier().domainName)) {
                // 看购网
                KangouCardStatus kangouCardStatus = KangouUtil.setCardUseless(ecoupon);
                if (kangouCardStatus == null) {
                    message = "看购网退款失败:" + eCouponSn;
                    render(message);
                }
            }
            message = applyRefund(ecoupon, refundComment);
        } else if ("UNCONSUME".equals(choice)) {
            Logger.info("取消验证.");
            Long consumedAt = ecoupon.consumedAt.getTime();
            Long currentTime = new Date().getTime();
            Long diffTime = (currentTime - consumedAt) / (24 * 60 * 60 * 1000);
            if (diffTime > 1) {
                message = "消费时间距今超过24小时，券状态不能扭转:" + eCouponSn;
                render(message);
            }
            if (KangouUtil.SUPPLIER_DOMAIN_NAME.equals(ecoupon.goods.getSupplier().domainName)) {
                Logger.info("处理看购网券:" + ecoupon.id);
                // 看购网
                message = "看购网不支持取消验证状态:" + eCouponSn;
                render(message);
            }
            message = applyUnConsumed(ecoupon, refundComment);
        } else {
            message = "请输入REFUND或UNCONSUME。";
        }

        render(ecoupon, message);
    }

    /**
     * 直接退款，券状态为『已退款』。
     *
     * @param eCoupon
     * @param refundComment
     * @return
     */
    private static String applyRefund(ECoupon eCoupon, String refundComment) {
        if (eCoupon.order.refundedAmount == null) {
            eCoupon.order.refundedAmount = BigDecimal.ZERO;
        }
        if (eCoupon.order.promotionBalancePay == null) {
            eCoupon.order.promotionBalancePay = BigDecimal.ZERO;
        }

        // 查找原订单信息 可能是分销账户，也可能是消费者账户

        Account userAccount = eCoupon.order.getBuyerAccount();

//        System.out.println("===coupon.salePrice" + eCoupon.salePrice);
//        System.out.println("===coupon.rebate" + eCoupon.rebateValue);
//        System.out.println("===order.account" + eCoupon.order.accountPay);
//        System.out.println("===order.disaccount" + eCoupon.order.discountPay);
//        System.out.println("===order.promotion" + eCoupon.order.promotionBalancePay);

        //先计算已消费的金额(此处应去除本券)
        BigDecimal consumedAmount = BigDecimal.ZERO;
        List<ECoupon> eCoupons = ECoupon.find("byOrderAndStatus",
                eCoupon.order, ECouponStatus.CONSUMED).fetch();
        for (ECoupon c : eCoupons) {
            if (!c.getId().equals(eCoupon.getId()))
                consumedAmount = consumedAmount.add(ECoupon.getLintRefundPrice(c));
        }

//        System.out.println("===consumedAmount" + consumedAmount);
        //已消费的金额加上已退款的金额作为垫底
        BigDecimal onTheBottom = consumedAmount.add(eCoupon.order.refundedAmount);

//        System.out.println("===onTheBottom" + onTheBottom);
        //再来看看去掉垫底的资金后，此订单还能退多少活动金和可提现余额
        BigDecimal refundOrderTotalCashAmount = eCoupon.order.accountPay.add(eCoupon.order.discountPay);
        BigDecimal refundOrderTotalPromotionAmount = eCoupon.order.promotionBalancePay;
        if (refundOrderTotalPromotionAmount.compareTo(onTheBottom) > 0) {
            //如果该订单的活动金大于垫底资金
            refundOrderTotalPromotionAmount = refundOrderTotalPromotionAmount.subtract(onTheBottom);
        } else {
            refundOrderTotalCashAmount = refundOrderTotalCashAmount
                    .add(refundOrderTotalPromotionAmount)
                    .subtract(onTheBottom);
            refundOrderTotalPromotionAmount = BigDecimal.ZERO;
            if (refundOrderTotalCashAmount.compareTo(BigDecimal.ZERO) < 0) {
                refundOrderTotalCashAmount = BigDecimal.ZERO;
            }
        }

//        System.out.println("===refundOrderTotalCashAmount" + refundOrderTotalCashAmount);
//        System.out.println("===refundOrderTotalPromotionAmount" + refundOrderTotalPromotionAmount);
        //用户为此券实际支付的金额,也就是从用户为该券付的钱来看，最多能退多少
        BigDecimal refundAtMostCouponAmount = ECoupon.getLintRefundPrice(eCoupon);
//        System.out.println("===refundAtMostCouponAmount" + refundAtMostCouponAmount);

        //最后我们来看看最终能退多少
        BigDecimal refundPromotionAmount = BigDecimal.ZERO;
        BigDecimal refundCashAmount = BigDecimal.ZERO;

        if (refundOrderTotalPromotionAmount.compareTo(refundAtMostCouponAmount) > 0) {
            refundPromotionAmount = refundOrderTotalPromotionAmount.subtract(refundAtMostCouponAmount);
        } else {
            refundPromotionAmount = refundOrderTotalPromotionAmount;
            refundCashAmount = refundAtMostCouponAmount.subtract(refundPromotionAmount);
            refundCashAmount = refundCashAmount.min(refundOrderTotalCashAmount);
        }
//        System.out.println("===refundCashAmount" + refundCashAmount);
//        System.out.println("===refundPromotionAmount" + refundPromotionAmount);

        TradeBill tradeBill = new TradeBill();
        tradeBill.fromAccount = eCoupon.getSupplierAccount(); //付款方为商户账户
        tradeBill.toAccount = AccountUtil.getPlatformCommissionAccount(eCoupon.order.operator);                                  //收款方为指定账户
        tradeBill.balancePaymentAmount = eCoupon.originalPrice;                                   //使用可提现余额来支付退款的金额
        tradeBill.ebankPaymentAmount = BigDecimal.ZERO;                          //不使用网银支付
        tradeBill.uncashPaymentAmount = BigDecimal.ZERO;                          //不使用不可提现余额支付
        tradeBill.promotionPaymentAmount = refundPromotionAmount;                          //使用活动金余额来支付退款的金额
        tradeBill.tradeType = TradeType.REFUND;                         //交易类型为退款
        tradeBill.orderId = eCoupon.order.id;                                  //冗余订单ID
        tradeBill.eCouponSn = eCoupon.eCouponSn;                                //冗余券编号
        tradeBill.amount = tradeBill.balancePaymentAmount
                .add(tradeBill.ebankPaymentAmount)
                .add(tradeBill.uncashPaymentAmount)
                .add(refundPromotionAmount);

        tradeBill.save();

        if (!TradeUtil.success(tradeBill,
                "券" + eCoupon.getMaskedEcouponSn() + "因" + refundComment + "被" + OperateRbac.currentUser().userName + "操作退款")) {
            throw new RuntimeException("商户退款失败:" + eCoupon.eCouponSn);
        }

        TradeBill rebateTrade = TradeUtil.transferTrade()
                .fromAccount(AccountUtil.getPlatformCommissionAccount(eCoupon.order.operator))
                .toAccount(userAccount)
                .balancePaymentAmount(refundCashAmount)
                .make();
        rebateTrade.orderId = eCoupon.order.id;

        if (!TradeUtil.success(rebateTrade, "券" + eCoupon.getMaskedEcouponSn() + "因" + refundComment + "被" + OperateRbac.currentUser().userName + "操作退款")) {
            throw new RuntimeException("退款失败:" + eCoupon.eCouponSn);
        }

        // 更新已退款的活动金金额
        eCoupon.order.refundedAmount = eCoupon.order.refundedAmount.add(refundCashAmount).add(refundPromotionAmount);
        eCoupon.order.save();

        String userName = OperateRbac.currentUser().userName;
        //记录券历史信息
        ECouponHistoryMessage.with(eCoupon).operator(userName).remark("已消费券退款:" + refundComment)
                .toStatus(ECouponStatus.REFUND).sendToMQ();

        // 更改订单状态
        eCoupon.status = ECouponStatus.REFUND;
        eCoupon.refundAt = new Date();
        eCoupon.refundPrice = refundCashAmount.add(refundPromotionAmount);
        eCoupon.save();

        return "退款成功，状态为已退款:" + eCoupon.eCouponSn + ", status:" + eCoupon.status;
    }

    /**
     * 取消验证，状态返回为『未消费』.
     *
     * @param eCoupon
     * @param refundComment
     * @return
     */
    private static String applyUnConsumed(ECoupon eCoupon, String refundComment) {
        if (eCoupon.status != ECouponStatus.CONSUMED) {
            return "必须是已经消费的券";
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

        // 给商户打钱
        TradeBill consumeTrade = TradeUtil.consumeTrade(eCoupon.order.operator)
                .toAccount(eCoupon.getSupplierAccount())
                .balancePaymentAmount(eCoupon.originalPrice)
                .orderId(eCoupon.order.getId())
                .coupon(eCoupon.eCouponSn)
                .reverseFromAndTo()
                .make();
        consumeTrade.tradeType = TradeType.REFUND;
        consumeTrade.save();
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
            if (eCoupon.order.isWebsiteOrder()) {
                TradeBill uhuilaCommissionTrade = TradeUtil.commissionTrade(eCoupon.order.operator)
                        .toAccount(AccountUtil.getUhuilaAccount(eCoupon.order.operator))
                        .balancePaymentAmount(eCoupon.salePrice.subtract(eCoupon.resalerPrice))
                        .coupon(eCoupon.eCouponSn)
                        .orderId(eCoupon.order.getId())
                        .reverseFromAndTo()
                        .make();

                TradeUtil.success(uhuilaCommissionTrade, "已消费退款：" + refundComment + "。" + eCoupon.order.description);
            }
        }

        if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
            // 给优惠券平台佣金
            TradeBill platformCommissionTrade = TradeUtil.commissionTrade(eCoupon.order.operator)
                    .toAccount(AccountUtil.getPlatformCommissionAccount(eCoupon.order.operator))
                    .balancePaymentAmount(platformCommission)
                    .coupon(eCoupon.eCouponSn)
                    .orderId(eCoupon.order.getId())
                    .reverseFromAndTo()
                    .make();
            TradeUtil.success(platformCommissionTrade, "已消费退款：" + refundComment + "。" + eCoupon.order.description);
        }

        if (eCoupon.rebateValue != null && eCoupon.rebateValue.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill rebateTrade = TradeUtil.transferTrade()
                    .fromAccount(AccountUtil.getPlatformIncomingAccount(eCoupon.order.operator))
                    .toAccount(AccountUtil.getUhuilaAccount(eCoupon.order.operator))
                    .balancePaymentAmount(eCoupon.rebateValue)
                    .orderId(eCoupon.order.id)
                    .make();
            TradeUtil.success(rebateTrade, "已消费退款：" + refundComment + "。活动折扣费" + eCoupon.rebateValue);
        } else if (eCoupon.salePrice.compareTo(eCoupon.originalPrice) < 0) {
            BigDecimal detaPrice = eCoupon.originalPrice.subtract(eCoupon.salePrice);
            // 如果售价低于进价，从活动金账户出
            TradeBill rebateTrade = TradeUtil.transferTrade()
                    .fromAccount(AccountUtil.getPlatformIncomingAccount(eCoupon.order.operator))
                    .toAccount(AccountUtil.getPromotionAccount(eCoupon.order.operator))
                    .balancePaymentAmount(detaPrice)
                    .orderId(eCoupon.order.id)
                    .make();
            TradeUtil.success(rebateTrade, "已消费退款：" + refundComment + "。低价销售补贴" + detaPrice);
        }
        /**
         * FIXME: 后面还有推荐返利的暂时不弄
         */


        String userName = OperateRbac.currentUser().userName;

        //记录券历史信息
        ECouponHistoryMessage.with(eCoupon).operator(userName).remark("已消费券取消验证:" + refundComment)
                .fromStatus(eCoupon.status).toStatus(ECouponStatus.UNCONSUMED).sendToMQ();

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
