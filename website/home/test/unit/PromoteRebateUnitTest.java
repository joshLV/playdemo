package unit;

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
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-8
 * Time: 下午4:26
 */
public class PromoteRebateUnitTest extends UnitTest {

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

        pList.get(2).status = RebateStatus.ALREADY_REBATE;
        pList.get(2).order = orderList.get(2);
        pList.get(2).save();

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
    public void testFindByUser() {
        PromoteRebate p = PromoteRebate.getRebateAmount(user);
        assertEquals(59, p.willGetAmount.intValue());
        assertEquals(61, p.haveGotAmount.intValue());
    }

    @Test
    public void testFindAccounts() {
        PromoteRebateCondition condition = new PromoteRebateCondition();
        JPAExtPaginator<PromoteRebate> list = PromoteRebate.findAccounts(user, condition, 1, 10);
        assertEquals(4, list.size());
    }

    @Test
    public void testRank() {
        promoteRebate.promoteUser = inviteUser;
        promoteRebate.invitedUser = user;
        promoteRebate.save();

        FactoryBoy.batchCreate(3, ECoupon.class,
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.eCouponSn = "000000" + seq;
                        target.status = ECouponStatus.CONSUMED;
                        target.consumedAt = new Date();
                        target.promoterRebateValue = BigDecimal.ONE;
                        target.order = promoteRebate.order;

                    }
                });


        List<PromoteRebate> list = PromoteRebate.findRank();
        assertEquals(1, list.size());
        PromoteRebate p = PromoteRebate.rank(inviteUser, list);
        assertEquals(3, p.rebateAmount.intValue());
        assertEquals(1, p.promoteTimes);
        assertEquals(inviteUser, p.promoteUser);

        p = PromoteRebate.allRank(list);
        assertEquals(3, p.rebateAmount.intValue());
        assertEquals(1, p.promoteTimes);
    }

    @Test
    public void testMaskedLoginName() {
        String userName = promoteRebate.getMaskedLoginName();
        assertEquals("1***q.com", userName);
    }

}
