package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.*;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-25
 * Time: 下午4:15
 */
public class UserPromoteRebateTest extends FunctionalTest {

    List<Order> orderList;
    List<PromoteRebate> pList;
    PromoteRebate promoteRebate;
    User user;
    User inviteUser;

    @Before
    public void setUp() {

        FactoryBoy.delete(User.class);
        FactoryBoy.delete(UserInfo.class);
        FactoryBoy.delete(Order.class);
        FactoryBoy.delete(OrderItems.class);
        FactoryBoy.delete(ECoupon.class);
        FactoryBoy.delete(PromoteRebate.class);
        FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        inviteUser = FactoryBoy.create(User.class, "loginName", new BuildCallback<User>() {
            @Override
            public void build(User target) {
            }
        });

        orderList = FactoryBoy.batchCreate(3, Order.class,
                new SequenceCallback<Order>() {
                    @Override
                    public void sequence(Order target, int seq) {
                        target.orderNumber = "000000" + seq;
                        target.amount = BigDecimal.TEN;
                        target.promoteUserId = user.id;
                    }
                });

        pList = FactoryBoy.batchCreate(3, PromoteRebate.class,
                new SequenceCallback<PromoteRebate>() {
                    @Override
                    public void sequence(PromoteRebate target, int seq) {
                        target.rebateAmount = new BigDecimal(30);
                        target.promoteUser = user;
                        target.invitedUser = inviteUser;
                        target.status = RebateStatus.UN_CONSUMED;
                        target.order = orderList.get(0);
                    }
                });

        pList.get(0).status = RebateStatus.PART_REBATE;
        pList.get(0).order = orderList.get(1);
        pList.get(0).save();

        pList.get(1).status = RebateStatus.UN_CONSUMED;
        pList.get(1).order = orderList.get(2);
        pList.get(1).save();

        promoteRebate = FactoryBoy.create(PromoteRebate.class);
        promoteRebate.rebateAmount = new BigDecimal(30);
        promoteRebate.promoteUser = user;
        promoteRebate.invitedUser = inviteUser;
        promoteRebate.order = orderList.get(0);
        promoteRebate.save();
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(OrderItems.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }


    @Test
    public void testIndex() {
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
        assertEquals(89, p.willGetAmount.intValue());
        assertEquals(31, p.haveGotAmount.intValue());
        BigDecimal boughtAmount = (BigDecimal) renderArgs("boughtAmount");
        BigDecimal consumedAmount = (BigDecimal) renderArgs("consumedAmount");
        assertEquals(20, boughtAmount.intValue());
        assertEquals(10, consumedAmount.intValue());
        assertEquals("qweu2a", user.promoterCode);
    }


    @Test
    public void testRank() {
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

    @Test
    public void testAccount() {
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

        Http.Response response = GET("/rebate-account");
        assertStatus(200, response);
        assertNotNull(renderArgs("accountList"));
        JPAExtPaginator<PromoteRebate> reportPage = (JPAExtPaginator<PromoteRebate>) renderArgs("accountList");
        assertEquals(4, reportPage.getRowCount());
        PromoteRebate p = (PromoteRebate) renderArgs("promoteRebate");
        assertEquals(89, p.willGetAmount.intValue());
        assertEquals(31, p.haveGotAmount.intValue());
        BigDecimal boughtAmount = (BigDecimal) renderArgs("boughtAmount");
        BigDecimal consumedAmount = (BigDecimal) renderArgs("consumedAmount");
        assertEquals(20, boughtAmount.intValue());
        assertEquals(10, consumedAmount.intValue());
    }
}
