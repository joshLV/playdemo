package functional;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.RefundReport;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import play.modules.paginate.ValuePaginator;

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-8
 * Time: 下午2:30
 */
public class RefundReportFuncTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(Goods.class);
        FactoryBoy.create(OrderItems.class);
        FactoryBoy.create(ECoupon.class);
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
        List<ECoupon> allCoupons = ECoupon.findAll();
        assertEquals(2, allCoupons.size());
        for(ECoupon eCoupon : allCoupons) {
            eCoupon.refundAt = new Date();
            eCoupon.status = ECouponStatus.REFUND;
            eCoupon.save();
        }

        Http.Response response = GET("/reports/refund" +
                "?condition.refundAtBegin =" + DateUtil.getBeginOfDay() +
                "&condition.refundAtEnd =" + DateUtil.getEndOfDay(new Date()));
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<RefundReport> reportPage = (ValuePaginator<RefundReport>) renderArgs("reportPage");
        assertEquals(2, reportPage.getRowCount());
    }


}
