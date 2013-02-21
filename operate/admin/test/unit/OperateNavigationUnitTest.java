package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateNavigation;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.List;

public class OperateNavigationUnitTest extends UnitTest {
    public String applicationName = Play.configuration.get("application.name").toString();

    @Before
    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        final OperateNavigation navigation = FactoryBoy.create(OperateNavigation.class);
        final OperateNavigation userNavigation = FactoryBoy.create(OperateNavigation.class, new BuildCallback<OperateNavigation>() {
            @Override
            public void build(OperateNavigation subNavigation) {
                subNavigation.name = "user";
                subNavigation.text = "用户管理";
                subNavigation.parent = navigation;
            }
        });
        final OperateNavigation userAddNavigation = FactoryBoy.create(OperateNavigation.class, new BuildCallback<OperateNavigation>() {
            @Override
            public void build(OperateNavigation subNavigation) {
                subNavigation.name = "user_add";
                subNavigation.text = "添加用户";
                subNavigation.parent = userNavigation;
            }
        });
        final OperateNavigation orderNavigation = FactoryBoy.create(OperateNavigation.class, new BuildCallback<OperateNavigation>() {
            @Override
            public void build(OperateNavigation subNavigation) {
                subNavigation.name = "order";
                subNavigation.text = "订单管理";
                subNavigation.parent = navigation;
            }
        });
        final OperateNavigation userEditNavigation = FactoryBoy.create(OperateNavigation.class, new BuildCallback<OperateNavigation>() {
            @Override
            public void build(OperateNavigation subNavigation) {
                subNavigation.name = "user_edit";
                subNavigation.text = "修改用户";
                subNavigation.parent = userNavigation;
            }
        });
        navigation.refresh();
    }

    @After
    public void  initPluginAgain() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void testGetTopNavigations() {
        List<OperateNavigation> topMenus = OperateNavigation.getTopNavigations();
        assertEquals(1, topMenus.size());
        // display_order test
        assertEquals("main", topMenus.get(0).name);
    }

    @Test
    public void testGetNavigationParentStack() {
        System.out.println("applicationName:" + applicationName);
        List<OperateNavigation> navigationStack = OperateNavigation.getNavigationParentStack(applicationName, "user_add");
        assertEquals(3, navigationStack.size());
        // navigation stack order test
        assertEquals("main", navigationStack.get(0).name);
        assertEquals("user", navigationStack.get(1).name);
        assertEquals("user_add", navigationStack.get(2).name);
    }

    @Test
    public void testGetSecondLevelNavigations() {
        System.out.println("applicationName:" + applicationName);
        List<OperateNavigation> secondLevelNavigations = OperateNavigation.getSecondLevelNavigations(applicationName, "user_edit");
        assertEquals(2, secondLevelNavigations.size());
        // second level menu order test
        assertEquals("user", secondLevelNavigations.get(0).name);
        assertEquals("order", secondLevelNavigations.get(1).name);
    }
}
