package factory.order;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.accounts.SettlementStatus;
import models.order.Prepayment;
import models.supplier.Supplier;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;

public class PrepaymentFactory extends ModelFactory<Prepayment> {

    @Override
    public Prepayment define() {
        Prepayment prepayment = new Prepayment();
        prepayment.amount = BigDecimal.TEN;
        prepayment.createdAt = new Date();
        prepayment.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        prepayment.effectiveAt = DateHelper.beforeDays(3);
        prepayment.expireAt = DateHelper.afterDays(1);
        prepayment.deleted = DeletedStatus.UN_DELETED;
        prepayment.createdBy = "admin";
        prepayment.warning = false;
        prepayment.settlementStatus = SettlementStatus.UNCLEARED;
        prepayment.remark = "测试备注";
        return prepayment;
    }

}
