package factory.order;

import factory.ModelFactory;
import factory.annotation.Factory;
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
        PromoteRebate rebate = new PromoteRebate(null, null, null, BigDecimal.TEN, false);
        rebate.partAmount = BigDecimal.ONE;
        rebate.createdAt = new Date();
        rebate.status = RebateStatus.ALREADY_REBATE;
        return rebate;
    }

    @Factory(name = "UN_CONSUMED")
    public PromoteRebate defineWithUnConsumed(PromoteRebate promoteRebate) {
        promoteRebate.status = RebateStatus.UN_CONSUMED;
        promoteRebate.rebateAmount = new BigDecimal(2.5);

        return promoteRebate;

    }

    @Factory(name = "PART_REBATE")
    public PromoteRebate defineWithPartRebate(PromoteRebate promoteRebate) {
        promoteRebate.status = RebateStatus.PART_REBATE;
        promoteRebate.rebateAmount = new BigDecimal(3.5);

        return promoteRebate;

    }

}
