package factory.supplier;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.supplier.SupplierCategory;

/**
 * 商户的商品分类的测试对象.
 * .
 * User: wangjia
 * Date: 12-12-3
 * Time: 上午11:53
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
