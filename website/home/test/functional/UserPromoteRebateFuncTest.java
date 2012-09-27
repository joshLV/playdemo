package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.order.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

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
    User inviteUser;

    @Before
    public void setUp() {

        FactoryBoy.create(User.class);
        FactoryBoy.create(Order.class);
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(PromoteRebate.class);
        FactoryBoy.create(OrderItems.class);
        user = FactoryBoy.create(User.class);
        System.out.println(user+"%%%%%%%");
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Ignore
    @Test
    public void testIndex() {
        final PromoteRebate promoteRebate = FactoryBoy.create(PromoteRebate.class);
        promoteRebate.rebateAmount = new BigDecimal(30);
        promoteRebate.promoteUser = user;
        promoteRebate.save();

        final PromoteRebate promoteRebate0 = FactoryBoy.create(PromoteRebate.class);
        promoteRebate0.rebateAmount = new BigDecimal(20);
        promoteRebate0.promoteUser = user;
        promoteRebate0.status = RebateStatus.PART_REBATE;
        promoteRebate0.rebateAmount = new BigDecimal(2.5);
        promoteRebate0.save();

        final PromoteRebate promoteRebate1 = FactoryBoy.create(PromoteRebate.class);
        promoteRebate1.rebateAmount = new BigDecimal(30);
        promoteRebate1.promoteUser = user;
        promoteRebate1.status = RebateStatus.UN_CONSUMED;
        promoteRebate1.rebateAmount = new BigDecimal(5.5);
        promoteRebate1.save();

        FactoryBoy.batchCreate(3, ECoupon.class,
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.eCouponSn = "000000" + seq;
                        target.status = ECouponStatus.UNCONSUMED;
                        target.consumedAt = new Date();
                        target.promoterRebateValue = BigDecimal.ONE;
                        target.order = promoteRebate.order;

                    }
                });
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        coupon.status = ECouponStatus.CONSUMED;
        coupon.consumedAt = new Date();
        coupon.promoterRebateValue = BigDecimal.TEN;
        coupon.order = promoteRebate.order;
        coupon.save();
        Http.Response response = GET("/rebate");
        assertStatus(200, response);
        PromoteRebate p = (PromoteRebate) renderArgs("promoteRebate");
        assertEquals(0, p.willGetAmount);
        assertEquals(1, p.haveGotAmount);
        BigDecimal boughtAmount = (BigDecimal) renderArgs("boughtAmount");
        BigDecimal consumedAmount = (BigDecimal) renderArgs("consumedAmount");
        assertEquals(1, boughtAmount);
        assertEquals(10, consumedAmount);
        assertEquals("qweu2a", user.promoterCode);
    }


    @Ignore
    @Test
    public void testRank() {
        PromoteRebate promoteRebate = FactoryBoy.create(PromoteRebate.class);
        promoteRebate.promoteUser = user;
        promoteRebate.save();
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
