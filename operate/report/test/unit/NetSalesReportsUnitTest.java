package unit;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.SalesOrderItemReport;
import models.SalesOrderItemReportCondition;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-22
 * Time: 下午3:18
 */
public class NetSalesReportsUnitTest extends UnitTest {
    Supplier supplier;
    Shop shop;
    Goods goods;
    Order order;
    OrderItems orderItem;
    Category category;
    ECoupon coupon;
  
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
                
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        supplier = FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.paidAt = DateHelper.beforeHours(1);
                o.status = OrderStatus.PAID;
            }
            
        });
        
        FactoryBoy.create(OrderItems.class);
        coupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.refundAt = new Date();
                e.status = ECouponStatus.REFUND;
                e.refundPrice = new BigDecimal("8.0");
            }
        });

        FactoryBoy.create(OrderItems.class);
        coupon = FactoryBoy.create(ECoupon.class,  new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.refundAt = new Date();
                e.refundPrice = new BigDecimal("8.0");
                e.status = ECouponStatus.REFUND;
            }
        });
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testDefaultIndex() {
        SalesOrderItemReportCondition condition = new SalesOrderItemReportCondition();
        condition.supplier = supplier;
        List<SalesOrderItemReport> reports = SalesOrderItemReport.getNetSales(condition);
        assertEquals(1, reports.size());
        assertEquals(supplier.id, reports.get(0).supplier.id);
        SalesOrderItemReport summary = SalesOrderItemReport.getNetSummary(reports);
        assertEquals(17, summary.salesAmount.intValue());
        assertEquals(17, summary.refundAmount.intValue());
        assertEquals(0, summary.netSalesAmount.intValue());
    }
}
