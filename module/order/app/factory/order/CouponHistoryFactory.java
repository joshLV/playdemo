package factory.order;

import factory.ModelFactory;
import models.order.CouponHistory;
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

        //记录券历史信息
        CouponHistory history = new CouponHistory(null, null, "产生券号", ECouponStatus.UNCONSUMED, ECouponStatus.UNCONSUMED, null);
        return history;
    }
}
