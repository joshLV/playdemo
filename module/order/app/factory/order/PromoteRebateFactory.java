package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.consumer.User;
import models.order.Order;
import models.order.PromoteRebate;
import models.order.RebateStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-25
 * Time: 下午4:41
 */
public class PromoteRebateFactory extends ModelFactory<PromoteRebate> {
    @Override
    public PromoteRebate define() {
        User user = FactoryBoy.create(User.class);
        User invitedUser = FactoryBoy.create(User.class);
        Order order = FactoryBoy.create(Order.class);
        PromoteRebate rebate = new PromoteRebate(user, invitedUser, order, BigDecimal.TEN, false);
        rebate.partAmount = BigDecimal.ONE;
        rebate.createdAt = new Date();
        rebate.status= RebateStatus.ALREADY_REBATE;
        return rebate;
    }
}
