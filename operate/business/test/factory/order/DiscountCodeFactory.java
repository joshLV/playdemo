package factory.order;

import static util.DateHelper.*;
import java.util.Date;
import models.order.DiscountCode;
import factory.ModelFactory;
import factory.annotation.Factory;

public class DiscountCodeFactory extends ModelFactory<DiscountCode> {

    /**
     * 在有效期内的测试数据.
     */
    @Override
    public DiscountCode define() {
        DiscountCode discountCode = new DiscountCode();
        discountCode.discountSn = "DIST001";
        discountCode.beginAt = beforeDays(new Date(), 1);
        discountCode.endAt = afterDays(new Date(), 1);
        return discountCode;
    }
    
    @Factory(name="Unavaiable")
    public DiscountCode defineUnavaiableDiscount() {
        DiscountCode discountCode = new DiscountCode();
        discountCode.discountSn = "DIST002";
        discountCode.beginAt = beforeDays(new Date(), 3);
        discountCode.endAt = beforeDays(new Date(), 1);
        return discountCode;
    }
}
