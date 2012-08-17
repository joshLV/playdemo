package factory.admin;

import java.util.ArrayList;
import models.admin.OperateRole;
import models.admin.OperateUser;
import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;

public class OperateUserFactory extends ModelFactory<OperateUser> {

    @Override
    public OperateUser define() {
        OperateUser user = new OperateUser();
        user.loginName = "tom";
        user.deleted = DeletedStatus.UN_DELETED;
        user.mobile = "13900118888";
        user.userName = "tom";
        user.roles = new ArrayList<OperateRole>();
        user.roles.add(role("sales"));
        user.roles.add(role("account"));
        user.roles.add(role("customservice"));
        user.roles.add(role("editor"));
        user.roles.add(role("admin"));
        return user;
    }

    @Factory(name="random")
    public void defineRandomUser(OperateUser user) {
        user.loginName = "test" + FactoryBoy.sequence(OperateUser.class);
        user.userName = "TestName" + FactoryBoy.sequence(OperateUser.class);
    }
    
    public static OperateRole role(String roleName) {
        OperateRole role = OperateRole.find("byKey", roleName).first();
        System.out.println("role=" + role + ", name=" + roleName);
        return role;
    }
}