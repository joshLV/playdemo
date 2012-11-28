package factory.order;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.consumer.User;
import models.order.PointGoodsOrder;
import models.order.PointGoodsOrderStatus;
import models.sales.PointGoods;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-27
 * Time: 上午10:48
 */
public class PointGoodsOrderFactory extends ModelFactory<PointGoodsOrder> {
    @Override
    public PointGoodsOrder define() {
        PointGoodsOrder order = new PointGoodsOrder();
        order.pointGoods = FactoryBoy.lastOrCreate(PointGoods.class);
        order.userId = FactoryBoy.lastOrCreate(User.class).id;
        order.orderNumber = "100" + FactoryBoy.sequence(PointGoodsOrder.class);
        order.amount = 10;
        order.buyerMobile = "13800000000";
        order.buyerPhone = "02132342134";
        order.deleted = DeletedStatus.UN_DELETED;
        order.status = PointGoodsOrderStatus.ACCEPT;
        order.totalPoint = 20000l;
        order.buyNumber = 1l;
        return order;
    }
}
