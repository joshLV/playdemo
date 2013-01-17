package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.accounts.CashCoupon;
import models.consumer.User;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * User: wangjia
 * Date: 12-12-10
 * Time: 上午9:55
 */
public class CashCouponFactory extends ModelFactory<CashCoupon> {

    @Override
    public CashCoupon define() {
        CashCoupon cashCoupon = new CashCoupon();
        cashCoupon.name = "test" + FactoryBoy.sequence(CashCoupon.class);
        DecimalFormat myFormatter = new DecimalFormat("00000");
        cashCoupon.serialNo = "12" + myFormatter.format(22);
        cashCoupon.chargeCode = "123456789123456";
        cashCoupon.faceValue = BigDecimal.valueOf(100);
        cashCoupon.createdAt = new Date();
        return cashCoupon;
    }
}
