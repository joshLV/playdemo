package unit;

import controllers.operate.cas.Security;
import models.SalesOrderItemReport;
import models.SalesOrderItemReportCondition;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.report.DetailDailyReport;
import models.report.GoodsDailyReport;
import models.report.ShopDailyReport;
import models.report.TotalDailyReport;
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

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-22
 * Time: 下午3:18
 */
public class NetSalesReportsUnitTest extends UnitTest {

    @Before
    public void setup() {

        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.delete(DetailDailyReport.class);
        Fixtures.delete(ShopDailyReport.class);
        Fixtures.delete(GoodsDailyReport.class);
        Fixtures.delete(TotalDailyReport.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(SupplierUser.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.loadModels("fixture/suppliers_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/detail_daily_reports.yml");
        Fixtures.loadModels("fixture/ecoupon.yml");
        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/resaler.yml");
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);


        List<OrderItems> list = OrderItems.findAll();
        for (OrderItems item : list) {
            item.createdAt = new Date();
            item.save();
        }
        List<Order> orderList = Order.findAll();
        for (Order order : orderList) {
            order.paidAt = new Date();
            order.save();
        }
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user1");
        OperateUser user = OperateUser.findById(id);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_001");
        ECoupon coupon = ECoupon.findById(id);

        coupon.refundAt = new Date();
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        coupon = ECoupon.findById(id);

        coupon.refundAt = new Date();
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        id = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods goods = Goods.findById(id);
        goods.supplierId = supplierId;
        goods.save();
        id = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        goods = Goods.findById(id);
        goods.supplierId = supplierId;
        goods.save();
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testDefaultIndex() {
        SalesOrderItemReportCondition condition = new SalesOrderItemReportCondition();
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        condition.supplier = Supplier.findById(supplierId);
        List<SalesOrderItemReport> reports = SalesOrderItemReport.getNetSales(condition);
        assertEquals(1, reports.size());
        assertEquals("肯德基", reports.get(0).supplier.fullName);
        SalesOrderItemReport summary = SalesOrderItemReport.getNetSummary(reports);
        assertEquals(20, summary.salesAmount.intValue());
        assertEquals(160, summary.refundAmount.intValue());
        assertEquals(-140, summary.netSalesAmount.intValue());
    }
}
