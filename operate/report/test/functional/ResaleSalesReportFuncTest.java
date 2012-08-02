package functional;

import controllers.operate.cas.Security;
import models.ResaleSalesReport;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.consumer.UserWebIdentification;
import models.order.ECoupon;
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
import org.junit.Ignore;
import org.junit.Test;
import play.db.DB;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        Fixtures.loadModels("fixture/ecoupon.yml");
        Fixtures.loadModels("fixture/user.yml");

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
    public void testIndexDefault(){
        Http.Response response = GET("/resale/sales");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightConditionNull(){
/*

        try {
            ResultSet rs =  DB.executeQuery("select goods_id, id from e_coupon");
            while (rs.next()){
                System.out.println("coupon: " + rs.getLong(1) + "," + rs.getLong(2));
            }
            rs.close();
            rs = DB.executeQuery("select id from goods");
            while (rs.next()){
                System.out.println("goods: " + rs.getInt(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        */
        /*
        List<ECoupon> eCoupons = ECoupon.findAll();

        List<Order> orders = Order.findAll();
        for (Order order : orders){
            System.out.println("---------------"+order.userType);
        }

        for (ECoupon eCoupon : eCoupons){
            System.out.println("++++++   "+eCoupon.order.userType);
        }
         */

        long id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_001");
        ECoupon eCoupon = ECoupon.findById(id);
        System.out.println("Type-------- "+eCoupon.order.userType+"  ID  "+eCoupon.order.userId);
        long id2 = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        ECoupon eCoupon2 = ECoupon.findById(id2);
        System.out.println("Type-------- "+eCoupon2.order.userType+" ID  "+eCoupon2.order.userId);

        Http.Response response = GET("/resale/sales?condition.accountType=null&condition.createdAtBegin=2012-02-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>)renderArgs("reportPage");
        assertEquals(2, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionConsumer(){
        Http.Response response = GET("/resale/sales?condition.accountType=CONSUMER&condition.createdAtBegin=2012-02-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>)renderArgs("reportPage");
        assertEquals(2, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithRightConditionResaler(){
        Http.Response response = GET("/resale/sales?condition.accountType=RESALER&condition.createdAtBegin=2012-02-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithError(){
        Http.Response response = GET("/resale/sales?condition.accountType=null&condition.createdAtBegin=2012-06-06&condition.createdAtEnd=2012-06-03&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<ResaleSalesReport> reportPage = (ValuePaginator<ResaleSalesReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }

}
