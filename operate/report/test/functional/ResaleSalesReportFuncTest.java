package functional;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import models.ResaleSalesReport;
import models.ResaleSalesReportCondition;
import models.accounts.AccountType;
import models.admin.OperateRole;
import models.admin.OperateUser;
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
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-2
 * Time: 上午9:38
 * To change this template use File | Settings | File Templates.
 */
public class ResaleSalesReportFuncTest extends FunctionalTest {

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
    public void testIndexDefault() {
        Http.Response response = GET("/reports/resale");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightConditionNull() {
        long id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_001");
        ECoupon eCoupon = ECoupon.findById(id);
        long id2 = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        ECoupon eCoupon2 = ECoupon.findById(id2);

        Http.Response response = GET("/reports/resale?condition.accountType=null&condition.paidAtBegin=2012-02-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionConsumer() {
        Http.Response response = GET("/reports/resale?condition.accountType=CONSUMER&condition.paidAtBegin=2012-02-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionResaler() {
        Http.Response response = GET("/reports/resale?condition.accountType=RESALER&" +
                "condition.paidAtBegin=" + DateUtil.getBeginOfDay() + "&condition.createdAtEnd=" + DateUtil.getEndOfDay(new Date()) + "&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithError() {
        Http.Response response = GET("/reports/resale?condition.accountType=null&condition.paidAtBegin=2012-06-06&condition.createdAtEnd=2012-06-03&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

}
