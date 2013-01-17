package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.BatchCoupons;
import models.order.ECoupon;
import models.resale.Resaler;
import models.sales.Goods;

import java.util.Date;
import java.util.LinkedList;

/**
 * User: wangjia
 * Date: 12-11-23
 * Time: 下午3:23
 */
public class BatchCouponsFactory extends ModelFactory<BatchCoupons> {
    @Override
    public BatchCoupons define() {
        BatchCoupons batchCoupons = new BatchCoupons();
        batchCoupons.name = "Batch_Coupons_Name " + FactoryBoy.sequence(BatchCoupons.class);
        batchCoupons.goodsName = FactoryBoy.lastOrCreate(Goods.class).name;
        batchCoupons.prefix = "12";
        batchCoupons.createdAt = new Date();
        batchCoupons.count = 1;
        batchCoupons.lockVersion = 0;
        batchCoupons.operatorId = FactoryBoy.lastOrCreate(Resaler.class).id;
        batchCoupons.coupons = new LinkedList<>();
        batchCoupons.coupons.add(FactoryBoy.lastOrCreate(ECoupon.class));
        return batchCoupons;
    }
}
