package unit;

import java.util.List;

import models.admin.SupplierNavigation;

import navigation.ContextedMenu;
import navigation.MenuContext;
import navigation.Navigation;
import navigation.NavigationPlugin;

import org.junit.Before;
import org.junit.Test;

import play.PlayPlugin;
import play.ant.PlayConfigurationLoadTask;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;


public class NavigationLoadCheckTest extends UnitTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(SupplierNavigation.class);
        Fixtures.loadModels("fixture/navigation.yml");

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/navigation.yml");
        Navigation.init(file);
    }

    @Test
    public void theNoDefinedNavigationWillBeDeleted() {
        // 加载后，数据库中没有在yml定义的导航记录必须被删除
        SupplierNavigation toDeleteNav = SupplierNavigation.find("byApplicationNameAndName", "traders-admin", "to_delete").first();
        assertNull(toDeleteNav);
    }
}

