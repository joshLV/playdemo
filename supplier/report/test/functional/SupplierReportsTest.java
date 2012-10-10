package functional;

import controllers.supplier.cas.Security;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import navigation.RbacLoader;
import models.sales.Shop;

import java.math.BigDecimal;
import models.report.ShopDailyReport;
import factory.callback.SequenceCallback;
import factory.callback.BuildCallback;
import factory.FactoryBoy;

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