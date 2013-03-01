package factory.operator;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.operator.OperateRole;
import models.operator.OperateUser;

import java.util.ArrayList;

public class OperateUserFactory extends ModelFactory<OperateUser> {

    @Override
    public OperateUser define() {
        OperateUser user = new OperateUser();
        user.loginName = "tom";
        user.deleted = DeletedStatus.UN_DELETED;
        user.mobile = "13900118888";
        user.userName = "tom";
        user.email = "test@uhuila.com";
        user.jobNumber = "123456";
        // 定义角色
        user.roles = new ArrayList<>();
        user.roles.add(role("sales"));
        user.roles.add(role("account"));
        user.roles.add(role("customservice"));
        user.roles.add(role("editor"));
        user.roles.add(role("admin"));
        user.roles.add(role("webop"));
        user.roles.add(role("manager"));
        user.roles.add(role("virtual_verify"));
        user.roles.add(role("test"));
        return user;
    }

    @Factory(name = "random")
    public void defineRandomUser(OperateUser user) {
        user.loginName = "test" + FactoryBoy.sequence(OperateUser.class);
        user.userName = "TestName" + FactoryBoy.sequence(OperateUser.class);
    }

    @Factory(name = "role")
    public void defineRole(OperateUser user) {
        user.mobile = "13211111111";
        user.roles = new ArrayList<>();
        user.roles.add(role("sales"));
        user.roles.add(role("customservice"));
        user.roles.add(role("editor"));
        user.roles.add(role("account"));
    }


    public static OperateRole role(String roleName) {
        return OperateRole.find("byKey", roleName).first();
    }

}
