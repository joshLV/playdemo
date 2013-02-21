package unit;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.RefundReport;
import models.RefundReportCondition;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-8
 * Time: 下午2:30
 */
public class RefundReportUnitTest extends UnitTest {
    ECoupon coupon;
    Goods goods;
    Goods goods1;
    Supplier supplier;
    OrderItems orderItems;
    OrderItems orderItems1;
    Order order;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        coupon = FactoryBoy.create(ECoupon.class);
        goods = FactoryBoy.create(Goods.class);
        goods1 = FactoryBoy.create(Goods.class);
        supplier = FactoryBoy.create(Supplier.class);
        orderItems = FactoryBoy.create(OrderItems.class);
        orderItems1 = FactoryBoy.create(OrderItems.class);
        order = FactoryBoy.create(Order.class);
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        order.status = OrderStatus.PAID;
        order.paidAt = new Date();
        order.save();

        orderItems.order = order;
        orderItems.goods = goods;
        orderItems.createdAt = new Date();
        orderItems.save();

        orderItems1.order = order;
        orderItems1.goods = goods1;
        orderItems1.createdAt = new Date();
        orderItems1.save();

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        FactoryBoy.batchCreate(2, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon target, int seq) {
                target.goods = goods;
                target.order = order;
                target.orderItems = orderItems;
                target.refundAt = new Date();
                target.refundPrice = BigDecimal.ONE;
                target.status = ECouponStatus.REFUND;
            }
        });
        coupon.goods = goods1;
        coupon.order = order;
        coupon.orderItems = orderItems1;
        coupon.refundAt = new Date();
        coupon.refundPrice = BigDecimal.TEN;
        coupon.status = ECouponStatus.REFUND;
        coupon.save();

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goods.supplierId = supplier.id;
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
        List<RefundReport> list = RefundReport.query(condition);
        assertEquals(2, list.size());

        RefundReport refundReport = RefundReport.summary(list);
        assertEquals(3, refundReport.buyNumber.intValue());
        assertEquals(17, refundReport.amount.intValue());

    }

    @Test
    public void testConsumerRefund() {
        RefundReportCondition condition = new RefundReportCondition();
        List<RefundReport> list = RefundReport.getConsumerRefundData(condition);
        assertEquals(1, list.size());

        RefundReport refundReport = RefundReport.consumerSummary(list);
        assertEquals(3, refundReport.buyNumber.intValue());
        assertEquals(12, refundReport.totalAmount.intValue());

    }
}
