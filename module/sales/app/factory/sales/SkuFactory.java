package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Brand;
import models.sales.Sku;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-27
 * Time: 下午5:46
 */
public class SkuFactory extends ModelFactory<Sku> {
    @Override
    public Sku define() {
        Sku sku = new Sku();
        sku.code = "S01000101";
        sku.name = "test";
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        sku.sequenceCode = "01";
        sku.supplier = supplier;
        sku.brand = FactoryBoy.lastOrCreate(Brand.class);
        sku.supplierCategory = FactoryBoy.lastOrCreate(SupplierCategory.class);
        sku.deleted = DeletedStatus.UN_DELETED;
        return sku;
    }
}
