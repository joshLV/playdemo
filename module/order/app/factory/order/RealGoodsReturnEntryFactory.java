package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.order.OrderItems;
import models.order.RealGoodsReturnEntry;
import models.order.RealGoodsReturnStatus;

/**
 * User: tanglq
 * Date: 13-4-12
 * Time: 下午5:10
 */
public class RealGoodsReturnEntryFactory extends ModelFactory<RealGoodsReturnEntry> {
    @Override
    public RealGoodsReturnEntry define() {
        RealGoodsReturnEntry entry = new RealGoodsReturnEntry();
        entry.orderItems = FactoryBoy.lastOrCreate(OrderItems.class, "orderItemReal");
        entry.createdBy = "test";
        entry.reason = "测试退货";
        entry.returnedCount = entry.orderItems.buyNumber;
        entry.status = RealGoodsReturnStatus.RETURNING;
        return entry;
    }
}
