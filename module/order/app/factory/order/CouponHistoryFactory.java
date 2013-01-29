package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponStatus;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-11
 * Time: 下午3:40
 */
public class CouponHistoryFactory extends ModelFactory<CouponHistory> {

    /**
     */
    @Override
    public CouponHistory define() {
        ECoupon ecoupon = FactoryBoy.lastOrCreate(ECoupon.class);
        return ECouponHistoryMessage.with(ecoupon).remark("产生券号")
                .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.UNCONSUMED).toModel();
    }
}
