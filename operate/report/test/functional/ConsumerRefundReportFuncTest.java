package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.RefundReport;
import models.operator.OperateUser;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
                target.refundPrice = target.salePrice;
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
    public void testIndexDefault() {
        Http.Response response = GET("/reports/consumer_refund");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<RefundReport> reportPage = (ValuePaginator<RefundReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
        RefundReport summary = (RefundReport) renderArgs("summary");
        BigDecimal refundPrice = BigDecimal.ZERO;
        List<ECoupon> allCoupon = ECoupon.findAll();
        for(ECoupon coupon : allCoupon){
            refundPrice = refundPrice.add(coupon.refundPrice);
        }


        assertEquals(0, refundPrice.compareTo(summary.totalAmount));
        assertEquals(2, summary.buyNumber.intValue());
    }

}
