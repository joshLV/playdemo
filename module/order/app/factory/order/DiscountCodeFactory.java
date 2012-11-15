package factory.order;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.order.DiscountCode;
import models.sales.Goods;

import java.math.BigDecimal;
import java.util.Date;

import static util.DateHelper.afterDays;
import static util.DateHelper.beforeDays;

public class DiscountCodeFactory extends ModelFactory<DiscountCode> {

    /**
     * 在有效期内的测试数据.
     */
    @Override
    public DiscountCode define() {
        DiscountCode discountCode = new DiscountCode();
        discountCode.title = "Test" + FactoryBoy.sequence(DiscountCode.class);
        discountCode.discountSn = "DIST" + FactoryBoy.sequence(DiscountCode.class);
        discountCode.discountAmount = BigDecimal.TEN;
        discountCode.beginAt = beforeDays(new Date(), 1);
        discountCode.endAt = afterDays(new Date(), 1);
        discountCode.deleted = DeletedStatus.UN_DELETED;
        return discountCode;
    }

    @Factory(name = "Unavailable")
    public void defineUnavailableDiscount(DiscountCode discountCode) {
        discountCode.discountSn = "DIST2";
        discountCode.beginAt = beforeDays(new Date(), 3);
        discountCode.endAt = beforeDays(new Date(), 1);
    }

}
