package functional.totalsales;

import java.util.List;

import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderStatus;
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
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class TotalSalesRatiosReportsTest extends FunctionalTest {
    Goods goods;
    
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        
        FactoryBoy.create(Supplier.class);
        FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);

        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.status = OrderStatus.PAID;
                o.createdAt = DateHelper.t("2012-07-15 12:31:00");
                o.paidAt = DateHelper.t("2012-07-15 12:31:30");
            }
        });
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.status = ECouponStatus.CONSUMED;
                e.createdAt = DateHelper.t("2012-07-15 12:31:32");
                e.consumedAt = DateHelper.t("2012-07-15 18:31:00");
            }
        });        
        
        
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        
        
    }
    
    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void 默认请求时只有商户列表_无其它数据() {
        Http.Response response = GET("/totalsales/ratios");
        assertIsOk(response);
        assertNotNull(renderArgs("suppliers"));
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightCondition(){
        Http.Response response = GET("/totalsales/ratios?condition.type=0&condition.supplierId=" + goods.supplierId + "&condition.beginAt=2012-07-01&condition.endAt=2012-08-01&condition.interval=");
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
        Http.Response response = GET("/totalsales/ratios?condition.supplierId=5&condition.goodsId=&condition.beginAt=2012-07-01&condition.endAt=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<TotalSalesReport> reportPage = (ValuePaginator<TotalSalesReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }
}
