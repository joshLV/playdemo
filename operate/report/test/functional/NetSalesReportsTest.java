package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.SalesOrderItemReport;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-22
 * Time: 下午3:18
 */
public class NetSalesReportsTest extends FunctionalTest {
    ECoupon coupon;
    ECoupon coupon1;
    Goods goods;
    Supplier supplier;
    OrderItems orderItems;
    Order order;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        coupon = FactoryBoy.create(ECoupon.class);
        coupon1 = FactoryBoy.create(ECoupon.class);
        goods = FactoryBoy.create(Goods.class);
        supplier = FactoryBoy.create(Supplier.class);
        orderItems = FactoryBoy.create(OrderItems.class);
        order = FactoryBoy.create(Order.class);
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        order.status = OrderStatus.PAID;
        order.paidAt = new Date();
        order.save();

        orderItems.order = order;
        orderItems.goods = goods;
        orderItems.createdAt = new Date();
        orderItems.save();

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        coupon.goods = goods;
        coupon.order = order;
        coupon.orderItems = orderItems;
        coupon.refundAt = new Date();
        coupon.refundPrice = BigDecimal.ONE;
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        coupon1.goods = goods;
        coupon1.order = order;
        coupon1.refundPrice = new BigDecimal("5");
        coupon1.orderItems = orderItems;
        coupon1.refundAt = new Date();
        coupon1.status = ECouponStatus.REFUND;
        coupon1.save();

        goods.supplierId = supplier.id;
        goods.save();
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testDefaultIndex() {
        Http.Response response = GET("/reports/net_sales");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        play.modules.paginate.ValuePaginator<SalesOrderItemReport> reportPage = (play.modules.paginate.ValuePaginator<SalesOrderItemReport>) renderArgs("reportPage");
        assertNotNull(reportPage);
        SalesOrderItemReport summary = (SalesOrderItemReport) renderArgs("summary");
        assertEquals(8, summary.salesAmount.intValue());
        assertEquals(6, summary.refundAmount.intValue());
        assertEquals(2, summary.netSalesAmount.intValue());
    }
}
