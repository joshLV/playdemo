package unit;

import factory.FactoryBoy;
import models.operator.OperateNavigation;
import operate.rbac.Application;
import operate.rbac.ContextedMenu;
import operate.rbac.Menu;
import operate.rbac.NavigationHandler;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;


public class OperateRbacMenuTest extends UnitTest {

    private String applicationName = Play.configuration.getProperty("application.name");

    @Before
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(OperateNavigation.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
    }

    @After
    public void initPluginAgain() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void theNoDefinedNavigationWillBeDeleted() {
        OperateNavigation mainNav = OperateNavigation.find("byApplicationNameAndName", applicationName, "main").first();
        assertNotNull(mainNav);
        assertNotNull(mainNav.permissions);
        assertEquals(4, mainNav.permissions.size());

        OperateNavigation userNav = OperateNavigation.find("byApplicationNameAndName", applicationName, "user_add").first();
        assertNotNull(userNav);
        assertNotNull(userNav.permissions);
        assertEquals(2, userNav.permissions.size());

        // 加载后，数据库中没有在yml定义的导航记录必须被删除
        OperateNavigation toDeleteNav = OperateNavigation.find("byApplicationNameAndName", applicationName, "to_delete").first();
        assertNull(toDeleteNav);
    }

    @Test
    public void canLoadNavigationYamlFile() {
        assertNotNull(NavigationHandler.getMenuContext());
        ContextedMenu menu = NavigationHandler.getMenu("main");
        assertNotNull(menu);

        // 加载后，数据库中必须有相关的记录
        List<OperateNavigation> menus = OperateNavigation.find("byApplicationName", applicationName).fetch();
        assertTrue(menus.size() > 0);

        OperateNavigation mainNav = OperateNavigation.find("byApplicationNameAndName", applicationName, "main")
                .first();
        assertNotNull(mainNav);
        OperateNavigation homeNav = OperateNavigation.find("byApplicationNameAndName", applicationName, "home")
                .first();
        assertNotNull(homeNav);
        assertNotNull(homeNav.parent);
        assertEquals(mainNav.name, homeNav.parent.name);
    }

    @Test
    public void testMarshallerApplication() throws Exception {
        Application app = new Application();
        app.text = "Hello";
        Menu m1 = new Menu();
        m1.name = "main";
        m1.text = "Main";
        Menu mc = new Menu();
        mc.name = "home";
        mc.params.put("note", "removed");
        mc.labelValue = "red blue";
        m1.children.add(mc);

        Menu m2 = new Menu();
        m2.name = "main2";

        app.menus.add(m1);
        app.menus.add(m2);

        JAXBContext jc = JAXBContext.newInstance(Application.class, Menu.class);
        Unmarshaller um = jc.createUnmarshaller();
        Marshaller m = jc.createMarshaller();
        StringWriter writer = new StringWriter();
        m.marshal(app, writer);
        String xmlString = writer.toString();

        StringReader sr = new StringReader(xmlString);
        Application umApp = (Application) um.unmarshal(sr);
        assertNotNull(umApp);
        assertEquals("Hello", umApp.text);
        assertEquals(2, umApp.menus.size());
    }
}
