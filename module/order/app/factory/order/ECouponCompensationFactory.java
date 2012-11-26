package factory.order;

import models.order.ECoupon;
import models.order.ECouponCompensation;
import factory.FactoryBoy;
import factory.ModelFactory;

public class ECouponCompensationFactory extends ModelFactory<ECouponCompensation> {

    @Override
    public ECouponCompensation define() {
        ECouponCompensation ec = new ECouponCompensation();
        ec.ecpuon = FactoryBoy.lastOrCreate(ECoupon.class);
        ec.compensationType = ECouponCompensation.CONSUMED;
        return ec;
    }

}
