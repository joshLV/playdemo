package unit;

import factory.FactoryBoy;
import models.admin.SupplierNavigation;
import navigation.Application;
import navigation.ContextedMenu;
import navigation.Menu;
import navigation.NavigationHandler;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;


public class RbacMenuTest extends UnitTest {

    @Before
    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        FactoryBoy.deleteAll();
        NavigationHandler.clearMenuContext();
        FactoryBoy.create(SupplierNavigation.class);
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);
        NavigationHandler.initNamedMenus();
    }

    @After
    public void initPluginAgain() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
    }

    @Test
    public void theNoDefinedNavigationWillBeDeleted() {
        SupplierNavigation mainNav = SupplierNavigation.find("byApplicationNameAndName", "traders-home", "main").first();
        assertNotNull(mainNav);
        assertNotNull(mainNav.permissions);
        assertEquals(4, mainNav.permissions.size());

        SupplierNavigation userNav = SupplierNavigation.find("byApplicationNameAndName", "traders-home", "user_add").first();
        assertNotNull(userNav);
        assertNotNull(userNav.permissions);
        assertEquals(2, userNav.permissions.size());

        // 加载后，数据库中没有在yml定义的导航记录必须被删除
        SupplierNavigation toDeleteNav = SupplierNavigation.find("byApplicationNameAndName", "traders-home", "to_delete").first();
        assertNull(toDeleteNav);
    }

    @Test
    public void canLoadNavigationYamlFile() {
        assertNotNull(NavigationHandler.getMenuContext());
        ContextedMenu menu = NavigationHandler.getMenu("main");
        assertNotNull(menu);

        // 加载后，数据库中必须有相关的记录
        List<SupplierNavigation> menus = SupplierNavigation.find("byApplicationName", "traders-home").fetch();
        assertTrue(menus.size() > 0);

        SupplierNavigation mainNav = SupplierNavigation.find("byApplicationNameAndName", "traders-home", "main")
                .first();
        assertNotNull(mainNav);
        assertEquals("系统管理", mainNav.text);
        assertEquals(new Integer(99), mainNav.displayOrder);
        SupplierNavigation homeNav = SupplierNavigation.find("byApplicationNameAndName", "traders-home", "home")
                .first();
        assertNotNull(homeNav);
        assertEquals(new Integer(0), homeNav.displayOrder);
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
//        System.out.println(xmlString);

        StringReader sr = new StringReader(xmlString);
        Application umApp = (Application) um.unmarshal(sr);
        assertNotNull(umApp);
        assertEquals("Hello", umApp.text);
        assertEquals(2, umApp.menus.size());
    }
}
