package factory.admin;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.sales.Shop;
import models.supplier.Supplier;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-23
 * Time: 上午11:42
 * To change this template use File | Settings | File Templates.
 */
public class SupplierUserFactory extends ModelFactory<SupplierUser> {

    @Override
    public SupplierUser define() {
        SupplierUser supplierUser = new SupplierUser();

        supplierUser.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        supplierUser.shop = FactoryBoy.lastOrCreate(Shop.class);
        
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        supplierUser.mobile = "13700001111";
        supplierUser.loginName = "02188888888";
        supplierUser.jobNumber = "001";
        supplierUser.supplierUserType = SupplierUserType.ANDROID;

        // 定义角色
        supplierUser.roles = new ArrayList<SupplierRole>();
        supplierUser.roles.add(role("sales"));
        supplierUser.roles.add(role("clerk"));
        return supplierUser;
    }


    public static SupplierRole role(String roleName) {
        SupplierRole role = SupplierRole.find("byKey", roleName).first();
        return role;
    }


    @Factory(name = "Id")
    public SupplierUser defineWithId(SupplierUser supplierUser) {
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        supplierUser.mobile = "13700001111";
        supplierUser.loginName = "02188888888";
        supplierUser.jobNumber = "001";
        return supplierUser;
    }
    
}
