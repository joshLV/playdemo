package factory.supplier;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.supplier.Supplier;
import models.supplier.SupplierProperty;
import models.supplier.SupplierStatus;
import util.DateHelper;

import java.util.Date;

/**
 * User: yan
 * Date: 13-4-12
 * Time: 下午4:20
 */
public class SupplierPropertyFactory extends ModelFactory<SupplierProperty> {

    @Override
    public SupplierProperty define() {
        SupplierProperty property = new SupplierProperty(FactoryBoy.lastOrCreate(Supplier.class), Supplier.CAN_SALE_REAL, "1");
        return property;
    }
}
