package function;

import models.admin.SupplierPermission;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.ContextedPermission;
import navigation.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;

public class PermissionCheckTest extends FunctionalTest {

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
        //ContextedPermission.clean();
	}

	@Test
	public void testAdminUserHasNotPermission() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
		SupplierUser user = SupplierUser.findById(id);
		
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
		
		assertStatus(403, GET("/foo/bar"));
		assertStatus(403, GET("/singlefoo/bar"));
		assertIsOk(GET("/singlefoo/user"));
	}
	
	
	@Test
	public void testTestUserHasPermission() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-test");
		SupplierUser user = SupplierUser.findById(id);
		
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
		
		assertIsOk(GET("/foo/bar"));
		assertIsOk(GET("/singlefoo/bar"));
		assertStatus(403, GET("/singlefoo/user"));
	}
}
