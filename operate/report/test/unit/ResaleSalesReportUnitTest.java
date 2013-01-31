package unit;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.OperateResaleSalesReport;
import models.OperateResaleSalesReportCondition;
import models.accounts.AccountType;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * User:yjy
 * Date: 12-8-2
 * Time: 上午9:38
 */
public class ResaleSalesReportUnitTest extends UnitTest {
    Resaler resaler;
    Supplier supplier;
    Goods goods;
    Order order;
    OrderItems orderItem;


    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        OperateUser operateUser = FactoryBoy.create(OperateUser.class);

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        goods = FactoryBoy.create(Goods.class);
        supplier = FactoryBoy.create(Supplier.class);
        orderItem = FactoryBoy.create(OrderItems.class);
        order = FactoryBoy.create(Order.class);
        resaler = FactoryBoy.create(Resaler.class);


        order.userId = resaler.id;
        order.userType = AccountType.RESALER;
        order.paidAt = DateUtil.getBeginOfDay();

        order.save();
        orderItem.goods = goods;
        orderItem.order = order;
        orderItem.save();
        FactoryBoy.batchCreate(5, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon e, int seq) {
                e.order = order;
                e.goods = goods;
                e.orderItems = orderItem;
                e.status = ECouponStatus.CONSUMED;
            }

        });
        FactoryBoy.batchCreate(5, ECoupon.class, new SequenceCallback<ECoupon>() {
            @Override
            public void sequence(ECoupon e, int seq) {
                e.order = order;
                e.orderItems = orderItem;
                e.goods = goods;
                e.refundPrice = BigDecimal.TEN;
                e.status = ECouponStatus.REFUND;
            }

        });

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Ignore
    @Test
    public void testQueryConsumer() {
        System.out.println("esize()>>>>" + ECoupon.count());
        OperateResaleSalesReportCondition condition = new OperateResaleSalesReportCondition();
        List<OperateResaleSalesReport> list = OperateResaleSalesReport.queryConsumer(condition);
//        assertEquals(1, list.size());
    }

    @Ignore
    @Test
    public void testQueryResaler() {
        OperateResaleSalesReportCondition condition = new OperateResaleSalesReportCondition();
        List<OperateResaleSalesReport> list = OperateResaleSalesReport.query(condition);
        condition.endAt = new Date();
        condition.beginAt = DateHelper.beforeDays(1);
        condition.accountType = AccountType.RESALER;
        assertEquals(1, list.size());

        OperateResaleSalesReport report = OperateResaleSalesReport.summary(list);
        assertEquals(10, report.totalNumber.intValue());
        assertEquals(50, report.totalRefundPrice.intValue());
        assertEquals(85, report.amount.intValue());
        assertEquals(42, report.consumedPrice.intValue());

    }

}
