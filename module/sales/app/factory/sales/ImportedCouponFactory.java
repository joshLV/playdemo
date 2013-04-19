package factory.sales;

import com.uhuila.common.util.RandomNumberUtil;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Goods;
import models.sales.ImportedCoupon;
import models.sales.ImportedCouponStatus;
import util.DateHelper;

/**
 * User: tanglq
 * Date: 13-1-6
 * Time: 下午2:25
 */
public class ImportedCouponFactory extends ModelFactory<ImportedCoupon> {
    @Override
    public ImportedCoupon define() {
        ImportedCoupon importedCoupon = new ImportedCoupon();
        importedCoupon.goods = FactoryBoy.lastOrCreate(Goods.class);
        importedCoupon.coupon = "00111222" + FactoryBoy.sequence(ImportedCoupon.class);
        importedCoupon.password = RandomNumberUtil.generateRandomNumber(6);
        importedCoupon.importedAt = DateHelper.beforeDays(1);
        importedCoupon.status = ImportedCouponStatus.UNUSED;
        return importedCoupon;
    }
}
