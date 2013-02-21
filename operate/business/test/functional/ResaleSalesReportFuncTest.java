package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.OperateResaleSalesReport;
import models.accounts.AccountType;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderStatus;
import models.resale.Resaler;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: yanjy
 */
public class ResaleSalesReportFuncTest extends FunctionalTest {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Ignore
    @Test
    public void testIndexDefault() {
        Http.Response response = GET("/reports/resale_sales");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightConditionNullTypeResaler() {
        //创建分销商订单
        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.status = OrderStatus.PAID;
                target.paidAt = new Date();
                target.userId = FactoryBoy.lastOrCreate(Resaler.class).id;
                target.userType = AccountType.RESALER;
            }
        });
        FactoryBoy.create(ECoupon.class);

        Http.Response response = GET("/reports/resale_sales" +
                "?condition.accountType=RESALER" +
                "&condition.paidAtBegin=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L * 60 * 24 * 2)) +
                "&condition.paidAtEnd=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L * 60 * 24 * 2)) +
                "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<OperateResaleSalesReport> reportPage = (ValuePaginator<OperateResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionNullTypeConsumer() {
        //创建分销商订单
        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.status = OrderStatus.PAID;
                target.paidAt = new Date();
            }
        });
        FactoryBoy.create(ECoupon.class);

        Http.Response response = GET("/reports/resale_sales" +
                "?condition.accountType=" +
                "&condition.paidAtBegin=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L * 60 * 24 * 2)) +
                "&condition.paidAtEnd=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L * 60 * 24 * 2)) +
                "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<OperateResaleSalesReport> reportPage = (ValuePaginator<OperateResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionConsumer() {
        //创建分销商订单
        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.status = OrderStatus.PAID;
                target.paidAt = new Date();
            }
        });
        FactoryBoy.create(ECoupon.class);

        Http.Response response = GET("/reports/resale_sales" +
                "?condition.accountType=CONSUMER" +
                "&condition.beginAt=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L * 60 * 24 * 2)) +
                "&condition.endAt=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L * 60 * 24 * 2)) +
                "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<OperateResaleSalesReport> reportPage = (ValuePaginator<OperateResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionResaler() {
        //创建分销商订单
        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.status = OrderStatus.PAID;
                target.paidAt = new Date();
                target.userId = FactoryBoy.lastOrCreate(Resaler.class).id;
                target.userType = AccountType.RESALER;
            }
        });
        FactoryBoy.create(ECoupon.class);
        Http.Response response = GET("/reports/resale_sales" +
                "?condition.accountType=RESALER" +
                "&condition.beginAt=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L * 60 * 24 * 2)) +
                "&condition.endAt=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L * 60 * 24 * 2)) +
                "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<OperateResaleSalesReport> reportPage = (ValuePaginator<OperateResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }
}
