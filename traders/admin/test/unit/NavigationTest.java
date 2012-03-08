package unit;

import java.util.List;

import models.admin.SupplierNavigation;

import navigation.ContextedMenu;
import navigation.MenuContext;
import navigation.Navigation;

import org.junit.Test;

import play.test.UnitTest;

public class NavigationTest extends UnitTest {

    @Test
    public void canLoadNavigationYamlFile() {
        assertNotNull(Navigation.getMenuContext());
        ContextedMenu menu = Navigation.getMenu("main");
        assertNotNull(menu);

        // 加载后，数据库中必须有相关的记录
        List<SupplierNavigation> menus = SupplierNavigation.find("byApplicationName", "traders-admin").fetch();
        assertTrue(menus.size() > 0);
    }

}

