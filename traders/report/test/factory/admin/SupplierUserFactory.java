package factory.admin;


import com.uhuila.common.constants.DeletedStatus;
import models.admin.SupplierRole;
import models.admin.SupplierUser;

import java.util.ArrayList;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.supplier.Supplier;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-20
 * Time: 下午4:47
 * To change this template use File | Settings | File Templates.
 */
public class SupplierUserFactory extends ModelFactory<SupplierUser> {

    @Override
    public SupplierUser define() {
     Supplier supplier=   FactoryBoy.create(Supplier.class);
        SupplierUser user = new SupplierUser();
        user.loginName = "tom";
        user.deleted = DeletedStatus.UN_DELETED;
        user.mobile = "13900118888";
        user.userName = "tom";

        user.supplier=supplier;
        user.lastLoginIP="127.0.0.1";
        // 定义角色
        user.roles = new ArrayList<SupplierRole>();
        user.roles.add(role("sales"));
        user.roles.add(role("account"));
        user.roles.add(role("manager"));
        user.roles.add(role("editor"));
        user.roles.add(role("admin"));
        user.roles.add(role("clerk"));
            user.save();
        return user;
    }

    public static SupplierRole role(String roleName) {
        SupplierRole role = SupplierRole.find("byKey", roleName).first();
        System.out.println("role=" + role + ", name=" + roleName);
        return role;
    }
}
