package functional.totalsales;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Goods;
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

import java.util.List;

public class TotalSalesTrendsReportsTest extends FunctionalTest {
    ECoupon coupon;
    Goods goods;
    Supplier supplier;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        goods = FactoryBoy.create(Goods.class);

        coupon = FactoryBoy.create(ECoupon.class);
        coupon.consumedAt = DateUtil.stringToDate("2012-07-02 11:30","yyy-MM-dd HH:mm");
        coupon.status = ECouponStatus.CONSUMED;
        coupon.save();

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
        Http.Response response = GET("/totalsales/trends");
        assertIsOk(response);
        assertNotNull(renderArgs("suppliers"));
        assertNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightCondition() {
        Http.Response response = GET("/totalsales/trends?condition.type=2&condition.goodsId=" + goods.id + "&condition.beginAt=2012-07-01&condition.endAt=2022-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<TotalSalesReport> reportPage = (ValuePaginator<TotalSalesReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
    }

    @Test
    public void testListWithRightCondition() {
        Http.Response response = GET("/totalsales/list?condition.type=2&condition.goodsId=" + goods.id + "&condition.beginAt=2012-07-01&condition.endAt=2022-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("ecoupons"));
        List<ECoupon> ecoupons = (List<ECoupon>) renderArgs("ecoupons");
        assertEquals(1, ecoupons.size());
    }

    @Test
    public void testSearchWithError() {
        Http.Response response = GET("/totalsales/trends?condition.supplierId=5&condition.goodsId=&condition.beginAt=2012-07-01&condition.endAt=2012-08-01&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<TotalSalesReport> reportPage = (ValuePaginator<TotalSalesReport>) renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }
}
