package unit;

import controllers.operate.cas.Security;
import models.RefundReport;
import models.RefundReportCondition;
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
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-8
 * Time: 下午2:30
 */
public class RefundReportUnitTest extends UnitTest {
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
        coupon.status = ECouponStatus.REFUND;
        coupon.refundAt = new Date();
        coupon.save();

        id = (Long) Fixtures.idCache.get("models.order.ECoupon-ecoupon_002");
        coupon = ECoupon.findById(id);
        coupon.status = ECouponStatus.REFUND;
        coupon.refundAt = new Date();
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

    public void testGoodsRefund() {
        RefundReportCondition condition = new RefundReportCondition();
        List<RefundReport> list = RefundReport.query(condition,null,true);
        System.out.println(list.get(0).reportDate);
        assertEquals(2, list.size());

        RefundReport refundReport = RefundReport.summary(list);
        assertEquals(2, refundReport.buyNumber.intValue());
        assertEquals(180, refundReport.amount.intValue());

    }

    @Test
    public void testConsumerRefund() {
        RefundReportCondition condition = new RefundReportCondition();
        List<RefundReport> list = RefundReport.getConsumerRefundData(condition);
        assertEquals(1, list.size());

        RefundReport refundReport = RefundReport.consumerSummary(list);
        assertEquals(2, refundReport.buyNumber.intValue());
        assertEquals(160, refundReport.totalAmount.intValue());

    }
}
