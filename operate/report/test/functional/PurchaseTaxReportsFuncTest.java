package functional;

import controllers.operate.cas.Security;
import models.PurchaseECouponReport;
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
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-1
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates.
 */
public class PurchaseTaxReportsFuncTest extends FunctionalTest {

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
        Http.Response response = GET("/reports/purchase");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightCondition(){
        /*
        List<ECoupon> eCoupons = ECoupon.findAll();
        System.out.println("size----------------------: "+eCoupons.size());
        for ( ECoupon eCoupon : eCoupons){
            System.out.println("id: "+eCoupon.getId()+" and ComsumedAt"+ (eCoupon.consumedAt==null? "null":eCoupon.consumedAt.toString()));
        }
         */
        /*
        long id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        assertNotNull(id);
        ECoupon eCoupon = ECoupon.findById(id);
        assertNotNull(eCoupon);
        System.out.println(eCoupon.order.userType);

        Set<String> keys = Fixtures.idCache.keySet();
        List<ECoupon> coupons = new ArrayList<>();
        for(String key : keys){
            if (key.startsWith("models.order.ECoupon")){
                ECoupon coupon = ECoupon.findById((Long)(Fixtures.idCache.get(key)));
                if(coupon != null){
                    coupons.add(coupon);
                }
            }
        }
        System.out.println("----------------- "+ coupons.size());
        */

        Http.Response response = GET("/reports/purchase?condition.supplier.id=0&condition.goodsLike=&condition.createdAtBegin=2012-07-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<PurchaseECouponReport> reportPage = (ValuePaginator<PurchaseECouponReport>)renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithError(){
        Http.Response response = GET("/reports/purchase?condition.supplier.id=5&condition.goodsLike=&condition.createdAtBegin=2012-07-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<PurchaseECouponReport> reportPage = (ValuePaginator<PurchaseECouponReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }
}
