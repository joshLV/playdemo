package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.admin.SupplierNavigation;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.util.List;

public class SupplierNavigationUnitTest extends UnitTest {
    public String applicationName = Play.configuration.get("application.name").toString();

    @Before
    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        final SupplierNavigation navigation = FactoryBoy.create(SupplierNavigation.class);
        final SupplierNavigation userNavigation = FactoryBoy.create(SupplierNavigation.class, new BuildCallback<SupplierNavigation>() {
            @Override
            public void build(SupplierNavigation subNavigation) {
                subNavigation.name = "user";
                subNavigation.text = "用户管理";
                subNavigation.parent = navigation;
            }
        });
        final SupplierNavigation userAddNavigation = FactoryBoy.create(SupplierNavigation.class, new BuildCallback<SupplierNavigation>() {
            @Override
            public void build(SupplierNavigation subNavigation) {
                subNavigation.name = "user_add";
                subNavigation.text = "添加用户";
                subNavigation.parent = userNavigation;
            }
        });
        final SupplierNavigation orderNavigation = FactoryBoy.create(SupplierNavigation.class, new BuildCallback<SupplierNavigation>() {
            @Override
            public void build(SupplierNavigation subNavigation) {
                subNavigation.name = "order";
                subNavigation.text = "订单管理";
                subNavigation.parent = navigation;
            }
        });
        final SupplierNavigation userEditNavigation = FactoryBoy.create(SupplierNavigation.class, new BuildCallback<SupplierNavigation>() {
            @Override
            public void build(SupplierNavigation subNavigation) {
                subNavigation.name = "user_edit";
                subNavigation.text = "修改用户";
                subNavigation.parent = userNavigation;
            }
        });
        navigation.refresh();
    }

    @Test
    public void testGetTopNavigations() {
        List<SupplierNavigation> topMenus = SupplierNavigation.getTopNavigations();
        assertEquals(1, topMenus.size());
        // display_order test
        assertEquals("main", topMenus.get(0).name);
    }

    @Test
    public void testGetNavigationParentStack() {
        List<SupplierNavigation> navigationStack = SupplierNavigation.getNavigationParentStack(applicationName, "user_add");
        assertEquals(3, navigationStack.size());
        // navigation stack order test
        assertEquals("main", navigationStack.get(0).name);
        assertEquals("user", navigationStack.get(1).name);
        assertEquals("user_add", navigationStack.get(2).name);
    }

    @Test
    public void testGetSecondLevelNavigations() {
        System.out.println("applicationName:" + applicationName);
        List<SupplierNavigation> secondLevelNavigations = SupplierNavigation.getSecondLevelNavigations(applicationName, "user_edit");
        assertEquals(2, secondLevelNavigations.size());
        // second level menu order test
        assertEquals("user", secondLevelNavigations.get(0).name);
        assertEquals("order", secondLevelNavigations.get(1).name);
    }
}
