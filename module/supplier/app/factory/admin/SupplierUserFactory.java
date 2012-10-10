package factory.admin;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.admin.SupplierUser;
import models.sales.Shop;
import models.supplier.Supplier;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-23
 * Time: 上午11:42
 * To change this template use File | Settings | File Templates.
 */
public class SupplierUserFactory extends ModelFactory<SupplierUser> {

    @Override
    public SupplierUser define(){
        SupplierUser supplierUser = new SupplierUser();
        Shop shop = FactoryBoy.create(Shop.class);
        Supplier supplier = FactoryBoy.lastOrCreate(Supplier.class);
        supplierUser.shop = shop;
        supplierUser.supplier = supplier;
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        supplierUser.mobile = "13700001111";
        supplierUser.loginName = "02188888888";
        supplierUser.jobNumber = "001";
        return supplierUser;
    }

    @Factory(name="Id")
    public SupplierUser defineWithId(SupplierUser supplierUser){

        supplierUser.deleted = DeletedStatus.UN_DELETED;
        supplierUser.mobile = "13700001111";
        supplierUser.loginName = "02188888888";
        supplierUser.jobNumber = "001";
        return supplierUser;

    }
}
