package function;

import models.admin.SupplierPermission;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.mvc.Http.Response;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;

public class PermissionCheckTest extends FunctionalTest {

    @BeforeClass
    public static void setUpRouter() {
        Router.addRoute("GET", "/foo/bar", "Foo.bar");
        Router.addRoute("GET", "/singlefoo/bar", "SingleFoo.bar");
        Router.addRoute("GET", "/singlefoo/user", "SingleFoo.user");
        Router.addRoute("GET", "/singlefoo/google", "SingleFoo.google");
    }

    @Before
    public void setUp() {
		Fixtures.delete(SupplierUser.class);
		Fixtures.delete(SupplierRole.class);
		Fixtures.delete(Supplier.class);
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/supplier_users.yml");
		
        // 加载test/rbac.xml配置文件
        VirtualFile file = VirtualFile.open("test/rbac.xml");
        RbacLoader.init(file);		        
    }
    
	@After
	public void tearDown() {
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        
        // 清除登录Mock
        Security.cleanLoginUserForTest();
	}

	@Test
	public void testAdminUserHasNotPermission() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user3");
		SupplierUser user = SupplierUser.findById(id);
		printUserRoles(user);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
		
		Response resp1 = GET("/foo/bar");
        assertStatus(200, resp1);
        assertContentMatch("没有权限", resp1);
		Response resp2 = GET("/singlefoo/bar");
        assertStatus(200, resp2);
        assertContentMatch("没有权限", resp2);
		Response resp3 = GET("/singlefoo/google");
        assertStatus(200, resp3);
        assertContentMatch("没有权限", resp3);
		assertIsOk(GET("/singlefoo/user"));
	}
	
	
	@Test
	public void testTestUserHasPermission() {
		Long id = (Long) Fixtures.idCache.get("models.admin.SupplierUser-test");
		SupplierUser user = SupplierUser.findById(id);
		printUserRoles(user);
		
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
		
		assertIsOk(GET("/foo/bar"));
		assertIsOk(GET("/singlefoo/bar"));
		assertIsOk(GET("/singlefoo/google"));
		Response resp1 = GET("/singlefoo/user");
        assertStatus(200, resp1);
        assertContentMatch("没有权限", resp1);
	}
	
	
	private void printUserRoles(SupplierUser user) {
	    System.out.println("User:" + user.loginName + " roles=" + user.roles);
	    for (SupplierRole role : user.roles) {
            System.out.println("  role:" + role.key);
            for (SupplierPermission perm  : role.permissions) {
                System.out.println("       perm:" + perm.key);
            }
        }
	    

	    
	}

}
