package function;

import java.util.List;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.ContextedMenu;
import navigation.NavigationHandler;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;

public class MenuCheckTest extends FunctionalTest {

    @Before
    public void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
    }

    @Before
    public void setUp() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/cusers.yml");
		
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);		        
    }
    
	@After
	public void tearDown() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
	}
	
	@Test
	public void testMenuBaseUrl() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
		SupplierUser user = SupplierUser.findById(id);
		
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
		assertIsOk(GET("/singlefoo/user"));
		

        List<ContextedMenu> list = (List<ContextedMenu>) renderArgs("topMenus");

        for (ContextedMenu contextedMenu : list) {
            System.out.println("menu:" + contextedMenu.getLink() + ", xx:" + contextedMenu.getText());
        }
        
        assertTrue(list.size() > 0);
        assertTrue(list.get(0).getLink().indexOf("localhost") > 0);
	}
}
