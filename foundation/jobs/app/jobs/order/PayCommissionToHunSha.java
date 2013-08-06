package jobs.order;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.ECoupon;
import models.order.Order;
import play.jobs.OnApplicationStart;

import java.math.BigDecimal;

/**
 * User: wangjia
 * Date: 13-8-6
 * Time: 上午11:03
 */
@JobDefine(title = "商户刷单给巴黎国际婚纱户打佣金", description = "商户刷单给巴黎国际婚纱户打佣金")
//@On("0 0 3 * * ?")
@OnApplicationStart
public class PayCommissionToHunSha extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        ECoupon coupon = ECoupon.findById(138699l);
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

}
