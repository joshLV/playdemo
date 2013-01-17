package factory.accounts;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.accounts.Voucher;
import util.DateHelper;
import util.DateHelper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * User: wangjia
 * Date: 13-1-5
 * Time: 上午9:45
 */
public class VoucherFactory extends ModelFactory<Voucher> {
    @Override
    public Voucher define() {
        Voucher voucher = new Voucher();
        voucher.name = "test" + FactoryBoy.sequence(Voucher.class);
        DecimalFormat myFormatter = new DecimalFormat("00000");
        voucher.serialNo = "12" + myFormatter.format(22);
        voucher.chargeCode = "123456789123456";
        voucher.value = BigDecimal.valueOf(100);
        voucher.createdAt = new Date();
        voucher.expiredAt = DateHelper.afterDays(5);
        voucher.prefix = "a";
        return voucher;
    }
}
