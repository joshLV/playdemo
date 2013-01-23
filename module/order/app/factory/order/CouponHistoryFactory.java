package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponHistoryData;
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
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        return ECouponHistoryData.newInstance(ecoupon).remark("产生券号")
                .fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.UNCONSUMED).toModel();
    }
}
