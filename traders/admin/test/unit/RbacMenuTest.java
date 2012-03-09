package unit;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import models.admin.SupplierNavigation;
import navigation.Application;
import navigation.ContextedMenu;
import navigation.Menu;
import navigation.Navigation;

import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class RbacMenuTest extends UnitTest {

    @SuppressWarnings("unchecked")
    public void setupDatabase() {
        Fixtures.delete(SupplierNavigation.class);
        Fixtures.loadModels("fixture/navigation.yml");

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        Navigation.init(file);
    }

    @Test
    public void theNoDefinedNavigationWillBeDeleted() {
        // 加载后，数据库中没有在yml定义的导航记录必须被删除
        SupplierNavigation toDeleteNav = SupplierNavigation.find("byApplicationNameAndName", "traders-admin", "to_delete").first();
        assertNull(toDeleteNav);
    }
    
    @Test
    public void canLoadNavigationYamlFile() {
        assertNotNull(Navigation.getMenuContext());
        ContextedMenu menu = Navigation.getMenu("main");
        assertNotNull(menu);

        // 加载后，数据库中必须有相关的记录
        List<SupplierNavigation> menus = SupplierNavigation.find("byApplicationName", "traders-admin").fetch();
        assertTrue(menus.size() > 0);

        SupplierNavigation mainNav = SupplierNavigation.find("byApplicationNameAndName", "traders-admin", "main")
                .first();
        assertNotNull(mainNav);
        SupplierNavigation homeNav = SupplierNavigation.find("byApplicationNameAndName", "traders-admin", "home")
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
        System.out.println(xmlString);

        StringReader sr = new StringReader(xmlString);
        Application umApp = (Application)um.unmarshal(sr);
        assertNotNull(umApp);
        assertEquals("Hello", umApp.text);
        assertEquals(2, umApp.menus.size());
    }
}
