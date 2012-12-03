package factory.supplier;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.supplier.SupplierCategory;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-3
 * Time: 上午11:53
 * To change this template use File | Settings | File Templates.
 */
public class SupplierCategoryFactory extends ModelFactory<SupplierCategory> {
    @Override
    public SupplierCategory define() {
        SupplierCategory supplierCategory = new SupplierCategory();
        supplierCategory.code = "01";
        supplierCategory.name = "test_name" + FactoryBoy.sequence(SupplierCategory.class);
        return supplierCategory;
    }
}
