package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.PurchaseECouponReport;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.consumer.UserWebIdentification;
import models.order.ECoupon;
import models.order.ECouponStatus;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        FactoryBoy.deleteAll();
        FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.shop = FactoryBoy.lastOrCreate(Shop.class);
                target.status = ECouponStatus.CONSUMED;
                target.consumedAt = new Date();
            }
        });

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
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
    public void testSearchWithRightCondition() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String url ="/reports/purchase?" +
                "condition.supplier.id="+ FactoryBoy.last(ECoupon.class).shop.supplierId +
                "&condition.goodsLike=" +
                "&condition.createdAtBegin=" + simpleDateFormat.format(new Date(System.currentTimeMillis() - 60000L*60*24*2)) +
                "&condition.createdAtEnd=" + simpleDateFormat.format(new Date(System.currentTimeMillis() + 60000L*60*24*30)) +
                "&condition.interval=";
        System.out.println(url);
        Http.Response response = GET(url);
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<PurchaseECouponReport> reportPage = (ValuePaginator<PurchaseECouponReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testSearchWithError() {
        Http.Response response = GET("/reports/purchase?condition.supplier.id=5&condition.goodsLike=&condition.createdAtBegin=2012-07-01&condition.createdAtEnd=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<PurchaseECouponReport> reportPage = (ValuePaginator<PurchaseECouponReport>) renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }
}
