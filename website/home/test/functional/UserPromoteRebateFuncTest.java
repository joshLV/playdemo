package functional;

import factory.FactoryBoy;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.PromoteRebate;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-25
 * Time: 下午4:15
 */
public class UserPromoteRebateFuncTest extends FunctionalTest {
    User user;

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testRank() {
        PromoteRebate promoteRebate = FactoryBoy.create(PromoteRebate.class);
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.status = ECouponStatus.CONSUMED;
        coupon.consumedAt = new Date();
        coupon.promoterRebateValue = BigDecimal.TEN;
        coupon.order = promoteRebate.order;
        coupon.save();
        Http.Response response = GET("/rebate-rank");
        assertStatus(200, response);
        assertContentMatch("一百券 - 返利排名", response);
        assertNotNull(renderArgs("rankList"));
        ValuePaginator<PromoteRebate> reportPage = (ValuePaginator<PromoteRebate>) renderArgs("rankList");
        assertEquals(1, reportPage.getRowCount());
        PromoteRebate summary = (PromoteRebate) (renderArgs("summary"));
        assertEquals(1, summary.promoteTimes);
        assertEquals(10, summary.rebateAmount.intValue());
    }

}
