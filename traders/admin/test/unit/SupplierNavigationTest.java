package unit;

import java.util.List;

import models.admin.SupplierNavigation;
import navigation.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class SupplierNavigationTest extends UnitTest {
    public String applicationName = Play.configuration.get("application.name").toString();

    @Before
    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        Fixtures.delete(SupplierNavigation.class);
        Fixtures.loadModels("fixture/navigation_unit.yml");
    }

    @After
    public void  initPluginAgain() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void testGetTopNavigations() {
        List<SupplierNavigation> topMenus = SupplierNavigation.getTopNavigations();
        assertEquals(2, topMenus.size());
        // display_order test
        assertEquals("main", topMenus.get(0).name);
        assertEquals("order", topMenus.get(1).name);
    }

    @Test
    public void testGetNavigationParentStack() {
        List<SupplierNavigation> navigationStack = SupplierNavigation.getNavigationParentStack(applicationName,"user_add");
        assertEquals(3, navigationStack.size());
        // navigation stack order test
        assertEquals("main", navigationStack.get(0).name);
        assertEquals("user", navigationStack.get(1).name);
        assertEquals("user_add", navigationStack.get(2).name);
    }

    @Test
    public void testGetSecondLevelNavigations() {
        List<SupplierNavigation> secondLevelNavigations = SupplierNavigation.getSecondLevelNavigations(applicationName,"user_edit");
        assertEquals(2, secondLevelNavigations.size());
        // second level menu order test
        assertEquals("user", secondLevelNavigations.get(0).name);
        assertEquals("role", secondLevelNavigations.get(1).name);
    }
}
