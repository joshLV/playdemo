package unit;

import java.util.List;

import models.admin.SupplierNavigation;

import navigation.ContextedMenu;
import navigation.MenuContext;
import navigation.Navigation;
import navigation.NavigationPlugin;

import org.junit.Test;

import play.PlayPlugin;
import play.ant.PlayConfigurationLoadTask;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class NavigationTest extends UnitTest {

    @Test
    public void canLoadNavigationYamlFile() {
        assertNotNull(Navigation.getMenuContext());
        ContextedMenu menu = Navigation.getMenu("main");
        assertNotNull(menu);

        // 加载后，数据库中必须有相关的记录
        List<SupplierNavigation> menus = SupplierNavigation.find("byApplicationName", "traders-admin").fetch();
        assertTrue(menus.size() > 0);

        SupplierNavigation mainNav = SupplierNavigation.find("byApplicationNameAndName", "traders-admin", "main").first();
        assertNotNull(mainNav);
        SupplierNavigation homeNav = SupplierNavigation.find("byApplicationNameAndName", "traders-admin", "home").first();
        assertNotNull(homeNav);
        assertNotNull(homeNav.parent);
        assertEquals(mainNav.name, homeNav.parent.name);
    }

}

