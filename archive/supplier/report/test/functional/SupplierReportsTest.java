package functional;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

public class SupplierReportsTest extends FunctionalTest {

	@Before
	public void setUp() {

		FactoryBoy.deleteAll();

		// 重新加载配置文件
		VirtualFile file = VirtualFile.open("conf/rbac.xml");
		RbacLoader.init(file);

		SupplierUser user = FactoryBoy.create(SupplierUser.class);
		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);

	}

	@Test
	public void testShowShopReport() {

		Http.Response response = GET("/reports/shop");

		assertStatus(200, response);
		assertContentMatch("门店报表", response);

	}

}