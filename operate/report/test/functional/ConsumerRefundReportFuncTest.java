package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.RefundReport;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
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
        FactoryBoy.deleteAll();
        FactoryBoy.batchCreate(2,ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.refundAt = new Date();
                target.status = ECouponStatus.REFUND;
            }
        });

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);

        /*
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
        */
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        /*
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
        */
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
