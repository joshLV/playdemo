package functional;

import java.util.regex.Pattern;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.consumer.UserWebIdentification;
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
import models.webop.WebTrackRefererReport;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;

public class WebTrackReferersTest extends FunctionalTest {

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
        Fixtures.delete(UserWebIdentification.class);
        Fixtures.loadModels("fixture/suppliers_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
        Fixtures.loadModels("fixture/detail_daily_reports.yml");
        Fixtures.loadModels("fixture/shop_daily_reports.yml");
        Fixtures.loadModels("fixture/goods_daily_reports.yml");
        Fixtures.loadModels("fixture/total_daily_reports.yml");
        Fixtures.loadModels("fixture/user_web_identifications.yml");
        
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        
        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user1");
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
    public void testDefaultIndex() {
        Response response = GET("/webop/referers");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }
 
    @Test
    public void testSearchRefererLike() {
        Response response = GET("/webop/referers?condition.refererLike=uhuila&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(2, reportPage.getRowCount());
        assertContentMatch("payment_info", response);
    }

    @Test
    public void testSearchRefererLikeNotExists() {
        Response response = GET("/webop/referers?condition.refererLike=yibaiqaa&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }    
       
    @Test
    public void testSearchRefererLikeOutOfRange() {
        Response response = GET("/webop/referers?condition.refererLike=uhuila&condition.begin=2012-06-16&condition.end=2012-06-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }

    @Test
    public void testSearchRefererHost() {
        Response response = GET("/webop/referers?condition.refererLike=uhuila&condition.isHost=true&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(2, reportPage.getRowCount());
        assertContentNotMatch("payment_info", response);
    }        

    public static void assertContentNotMatch(String pattern, Response response) {
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(getContent(response)).find();
        assertTrue("Response content does match '" + pattern + "'", !ok);
    }
}
