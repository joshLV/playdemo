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
import java.util.HashSet;

/**
 * SupplierUser的测试对象.
 * User: hejun
 * Date: 12-8-23
 * Time: 上午11:42
 */
public class SupplierUserFactory extends ModelFactory<SupplierUser> {
    @Override
    public SupplierUser define() {
        SupplierUser supplierUser = new SupplierUser();

        supplierUser.supplier = FactoryBoy.lastOrCreate(Supplier.class);
        supplierUser.shop = FactoryBoy.lastOrCreate(Shop.class);
        supplierUser.userName = "abc";
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        supplierUser.mobile = "13700001111";
        supplierUser.loginName = "02188888888";
        supplierUser.jobNumber = "001";
        supplierUser.supplierUserType = SupplierUserType.ANDROID;

        // 定义角色
        supplierUser.roles = new ArrayList<>();
        supplierUser.roles.add(role("sales"));
        supplierUser.roles.add(role("clerk"));
        supplierUser.roles.add(role("admin"));
        supplierUser.roles.add(role("account"));
        supplierUser.permissions = new HashSet<>();
        return supplierUser;
    }


    public static SupplierRole role(String roleName) {
        return SupplierRole.find("byKey", roleName).first();
    }


    @Factory(name = "Id")
    public SupplierUser defineWithId(SupplierUser supplierUser) {
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        supplierUser.mobile = "13700001111";
        supplierUser.loginName = "02188888888";
        supplierUser.jobNumber = "001";
        return supplierUser;
    }

    @Factory(name = "SalesAdmin")
    public SupplierUser defineWithSalesAdmin(SupplierUser supplierUser) {
        // 定义角色
        supplierUser.roles = new ArrayList<>();
        supplierUser.roles.add(role("sales"));
        supplierUser.roles.add(role("admin"));
        return supplierUser;
    }

    @Factory(name = "Test")
    public SupplierUser defineWithTest(SupplierUser su) {
        // 定义角色
        su.loginName = "test";
        su.roles = new ArrayList<>();
        su.roles.add(role("test"));
        return su;
    }


}
