package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.SalesOrderItemReport;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderStatus;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: hejun
 * Date: 12-8-2
 */
public class SalesTaxReportsFuncTest extends FunctionalTest{

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        Security.setLoginUserForTest(operateUser.loginName);

        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order target) {
                target.status = OrderStatus.PAID;
                target.paidAt = new Date();
            }
        });
        FactoryBoy.create(ECoupon.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testDefaultIndex() {
        Http.Response response = GET("/reports/sales");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightCondition(){
        Supplier supplier = FactoryBoy.last(Supplier.class);
        assertNotNull(supplier);
        Goods goods = FactoryBoy.last(Goods.class);
        assertNotNull(goods);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Http.Response response = GET("/reports/sales?condition.supplier.id=" + supplier.id +
                "&condition.goodsLike=" + goods.name +
                "&condition.createdAtBegin=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L*24*2)) +
                "&condition.createdAtEnd=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L*24*2)) +
                "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<SalesOrderItemReport> reportPage = (ValuePaginator<SalesOrderItemReport>)renderArgs("reportPage");
        assertNotNull(reportPage);
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithError(){
        Supplier supplier = FactoryBoy.last(Supplier.class);
        assertNotNull(supplier);
        Goods goods = FactoryBoy.last(Goods.class);
        assertNotNull(goods);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Http.Response response = GET("/reports/sales?condition.supplier.id=" + supplier.id +
                "&condition.goodsLike=NotExist" + goods.name +
                "&condition.createdAtBegin=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L*24*2)) +
                "&condition.createdAtEnd=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L*24*2)) +
                "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<SalesOrderItemReport> reportPage = (ValuePaginator<SalesOrderItemReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }
}
