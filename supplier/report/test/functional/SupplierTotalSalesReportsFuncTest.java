package functional;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import models.totalsales.TotalSalesReport;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * User: hejun
 * Date: 12-8-22
 * Time: 下午4:35
 */
@Ignore
public class SupplierTotalSalesReportsFuncTest extends FunctionalTest {
    Shop shop;
    Supplier supplier;

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        shop = FactoryBoy.create(Shop.class);
        // todo 登陆失败，需要配置permission
        final SupplierUser user = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        supplier = Supplier.findUnDeleted().get(0);
        // 初始化数据
        FactoryBoy.batchCreate(10, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon target, int seq) {
                target.isFreeze = 0;
                target.status = ECouponStatus.CONSUMED;
                target.eCouponSn = "1000" + seq;
                target.supplierUser = user;
                target.goods.supplierId = supplier.id;
                target.faceValue = new BigDecimal(100);
                target.originalPrice = new BigDecimal(80);
                target.salePrice = new BigDecimal(90);
                target.consumedAt = new Date();
            }
        });

    }


    @Test
    public void testTrends() {


        Http.Response response = GET("/totalsales/trends?condition.type=1&condition.shopId=" + shop.id +
                "&condition.beginAt=" + DateHelper.beforeDays(new Date(), 1) + "&condition.endAt=" + DateHelper.afterDays(new Date(), 5) + "&condition.interval=");
        assertStatus(200, response);
        assertNotNull(renderArgs("reportPage"));
        List<TotalSalesReport> list = (List) renderArgs("reportPage");
        assertEquals(10, list.size());
        //assertNotNull(renderArgs("dateList"));
        //assertNotNull(renderArgs("chartsMap"));
        //assertNotNull(renderArgs("reportPage"));

    }
}
