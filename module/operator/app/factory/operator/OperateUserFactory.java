package factory.operator;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import factory.callback.BuildCallback;
import models.operator.OperateRole;
import models.operator.OperateUser;

import java.util.ArrayList;

public class OperateUserFactory extends ModelFactory<OperateUser> {

    @Override
    public OperateUser define() {
        //创建角色
        OperateRole roleSales = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "销售人员";
                role.key = "sales";
            }
        });


        OperateRole roleAdmin = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "系统管理员";
                role.key = "admin";
            }
        });

        OperateRole roleTest = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "测试角色";
                role.key = "test";
            }
        });

        OperateRole roleEditor = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "编辑";
                role.key = "editor";
            }
        });

        OperateRole roleCustomservice = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "客服";
                role.key = "customservice";
            }
        });

        OperateRole roleWebop = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "网站运营";
                role.key = "webop";
            }
        });

        OperateRole roleDevelop = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "开发人员";
                role.key = "developer";
            }
        });

        OperateRole roleManager = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "经理";
                role.key = "manager";
            }
        });

        OperateRole roleAccount = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "财务";
                role.key = "account";
            }
        });
        OperateRole virtualVerify = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "虚拟验证";
                role.key = "virtual_verify";
            }
        });

        OperateRole inventory_manager = FactoryBoy.create(OperateRole.class, new BuildCallback<OperateRole>() {
            @Override
            public void build(OperateRole role) {
                role.text = "库存管理员";
                role.key = "inventory_manager";
            }
        });

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
        user.roles.add(role("inventory_manager"));
        user.roles.add(role("editor"));
        user.roles.add(role("admin"));
        user.roles.add(role("webop"));
        user.roles.add(role("manager"));
        user.roles.add(role("virtual_verify"));
        user.roles.add(role("test"));
        user.roles.add(role("developer"));
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
