package function;

import models.admin.OperateRole;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;

import org.junit.After;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;

public class OperateOrdersTest extends FunctionalTest {

    @org.junit.Before
    public void setup() {
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.delete(GoodsHistory.class);
        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/supplierusers.yml");
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/orderItems.yml");
        Fixtures.loadModels("fixture/goodsHistory.yml");
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
        OperateUser user = OperateUser.findById(id);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

	@Test
	public void testIndex() {
		Response response = GET("/orders");
		assertStatus(200, response);
	}


    @Test
    public void testCoupons() {
        Response response = GET("/coupons");
        assertStatus(200, response);
    }

    @Test
    public void testDetails() {
        Long id = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Response response = GET("/orders/" + id);
        assertStatus(200, response);
    }

    @Test
    public void testIndexWithCondition() {
        Response response = GET("/orders?condition.createdAtBegin=&condition.createdAtEnd=&condition.paidAtBegin=&condition.paidAtEnd=&condition.brandId=0&condition.refundAtBegin=&condition.refundAtEnd=&condition.payMethod=&condition.userType=&condition.searchKey=&condition.searchItems=&condition.status=&condition.deliveryType=&desc=1000000");
        assertIsOk(response);
        assertNotNull(renderArgs("desc"));
    }
}
