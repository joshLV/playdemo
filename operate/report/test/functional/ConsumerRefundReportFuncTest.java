package functional;

import controllers.operate.cas.Security;
import models.RefundReport;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
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

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-8
 * Time: 下午2:30
 */
public class ConsumerRefundReportFuncTest extends FunctionalTest {
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
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_001");
        ECoupon coupon = ECoupon.findById(id);
        coupon.refundAt = new Date();
        coupon.status = ECouponStatus.REFUND;
        coupon.save();
        coupon.refresh();
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        coupon = ECoupon.findById(id);
        coupon.refundAt = new Date();
        coupon.status = ECouponStatus.REFUND;
        coupon.save();
        coupon.refresh();
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndexDefault() {
        Http.Response response = GET("/reports/consumer_refund");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<RefundReport> reportPage = (ValuePaginator<RefundReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
        RefundReport summary = (RefundReport) renderArgs("summary");
        assertEquals(160, summary.totalAmount.intValue());
        assertEquals(2, summary.buyNumber.intValue());
    }

}
