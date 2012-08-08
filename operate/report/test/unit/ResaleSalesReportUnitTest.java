package unit;

import controllers.operate.cas.Security;
import models.ResaleSalesReport;
import models.ResaleSalesReportCondition;
import models.accounts.AccountType;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User:yjy
 * Date: 12-8-2
 * Time: 上午9:38
 * To change this template use File | Settings | File Templates.
 */
public class ResaleSalesReportUnitTest extends UnitTest {

    @Before
    public void setup() {

        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(SupplierUser.class);
        Fixtures.delete(Resaler.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.loadModels("fixture/suppliers_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/ecoupon.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/resaler.yml");
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user1");
        OperateUser user = OperateUser.findById(id);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_1");
        id = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(id);
        order.userId = resalerId;
        order.userType = AccountType.RESALER;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE));
        order.paidAt = calendar.getTime();
        order.save();

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testQueryConsumer() {
        ResaleSalesReportCondition condition = new ResaleSalesReportCondition();
        List<ResaleSalesReport> list = ResaleSalesReport.queryConsumer(condition);
        assertEquals(1, list.size());


    }

    @Test
    public void testQueryResaler() {
        ResaleSalesReportCondition condition = new ResaleSalesReportCondition();
        List<ResaleSalesReport> list = ResaleSalesReport.query(condition);
        assertEquals(1, list.size());

        ResaleSalesReport report = ResaleSalesReport.summary(list);
        assertEquals(1, report.totalNumber.intValue());
        assertEquals(0, report.totalRefundPrice.intValue());
        assertEquals(90, report.amount.intValue());
        assertEquals(0, report.consumedPrice.intValue());

    }

}
