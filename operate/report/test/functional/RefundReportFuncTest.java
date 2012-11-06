package functional;

import com.uhuila.common.util.DateUtil;
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
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import play.modules.paginate.ValuePaginator;

import java.util.Calendar;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-8
 * Time: 下午2:30
 */
public class RefundReportFuncTest extends FunctionalTest {
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
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE));
        coupon.refundAt = calendar.getTime();
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        coupon = ECoupon.findById(id);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE));
        coupon.refundAt = calendar.getTime();
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        id = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods goods = Goods.findById(id);
        goods.supplierId = supplierId;
        goods.save();
        id = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        goods = Goods.findById(id);
        goods.supplierId = supplierId;
        goods.save();


    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndexDefault() {
        Http.Response response = GET("/reports/refund");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchWithRightCondition() {
        Http.Response response = GET("/reports/refund?condition.refundAtBegin =" + DateUtil.getBeginOfDay() + "&condition.refundAtEnd =" + DateUtil.getEndOfDay(new Date()));
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<RefundReport> reportPage = (ValuePaginator<RefundReport>) renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }


}
