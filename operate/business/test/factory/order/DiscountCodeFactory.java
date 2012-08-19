package factory.order;

import static util.DateHelper.afterDays;
import static util.DateHelper.beforeDays;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import models.order.DiscountCode;
import models.order.OrderDiscount;

import com.uhuila.common.constants.DeletedStatus;

import factory.ModelFactory;
import factory.annotation.Factory;

public class DiscountCodeFactory extends ModelFactory<DiscountCode> {

    /**
     * 在有效期内的测试数据.
     */
    @Override
    public DiscountCode define() {
        DiscountCode discountCode = new DiscountCode();
        discountCode.discountSn = "DIST1";
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

    @Override
    public void delete(DiscountCode discountCode) {
        discountCode.goods = null;
        List<OrderDiscount> list = OrderDiscount.find("byDiscountCode",
                        discountCode).fetch();

        for (OrderDiscount orderDiscount : list) {
            orderDiscount.order = null;
            orderDiscount.orderItem = null;
            orderDiscount.discountCode = null;
            orderDiscount.save();
            orderDiscount.delete();
        }
        discountCode.save();
        discountCode.delete();
    }
}
