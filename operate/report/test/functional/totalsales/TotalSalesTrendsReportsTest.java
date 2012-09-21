package functional.totalsales;

import java.util.List;
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
import models.totalsales.TotalSalesReport;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;

public class TotalSalesTrendsReportsTest extends FunctionalTest {

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
        Fixtures.delete(ECoupon.class);
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

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user1");
        OperateUser user = OperateUser.findById(id);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        goods = Goods.findById(goodsId);
    }
    
    Goods goods = null;

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void 默认请求时只有商户列表_无其它数据() {
        Http.Response response = GET("/totalsales/trends");
        assertIsOk(response);
        assertNotNull(renderArgs("suppliers"));
        assertNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightCondition(){
        Http.Response response = GET("/totalsales/trends?condition.type=2&condition.goodsId=" + goods.id + "&condition.beginAt=2012-07-01&condition.endAt=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<TotalSalesReport> reportPage = (ValuePaginator<TotalSalesReport>)renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testListWithRightCondition(){
        Http.Response response = GET("/totalsales/list?condition.type=2&condition.goodsId=" + goods.id + "&condition.beginAt=2012-07-01&condition.endAt=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("ecoupons"));
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertEquals(1, ecoupons.size());
    }
    
    @Test
    public void testSearchWithError(){
        Http.Response response = GET("/totalsales/trends?condition.supplierId=5&condition.goodsId=&condition.beginAt=2012-07-01&condition.endAt=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<TotalSalesReport> reportPage = (ValuePaginator<TotalSalesReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }
}
