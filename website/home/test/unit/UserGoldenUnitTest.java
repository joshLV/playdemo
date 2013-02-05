package unit;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.consumer.UserCondition;
import models.consumer.UserGoldenCoin;
import models.sales.Goods;
import models.sales.GoodsSchedule;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-18
 * Time: 下午5:07
 */
public class UserGoldenUnitTest extends UnitTest {
    User user;
    UserGoldenCoin goldenCoin;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        final Goods goods = FactoryBoy.create(Goods.class);
        user = FactoryBoy.lastOrCreate(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        FactoryBoy.batchCreate(20, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.goods = goods;
                        target.user = user;
                        target.remarks = "签到20天";
                        target.number = 5L;
                        target.createdAt = DateHelper.beforeMinuts(5);
                    }
                });

        goldenCoin = FactoryBoy.create(UserGoldenCoin.class);
    }

    @Test
    public void testList() {
        UserCondition condition = new UserCondition();
        condition.createdAtBegin = DateHelper.beforeDays(2);
        condition.createdAtEnd = new Date();
        JPAExtPaginator<UserGoldenCoin> reportPage = UserGoldenCoin.find(user, condition, 1, 15);
        assertEquals(21, reportPage.size());
        assertEquals(21, UserGoldenCoin.getCheckinNumber(user).intValue());
        assertEquals(105, UserGoldenCoin.getTotalCoins(user).intValue());
    }

    @Test
    public void testCheckin() {
        assertEquals(21, UserGoldenCoin.count());

        GoodsSchedule schedule = FactoryBoy.create(GoodsSchedule.class);
        schedule.expireAt = DateHelper.afterDays(2);
        schedule.save();
        //今天签到过的情况
        UserGoldenCoin.checkin(user, schedule.goods, "每天签到");
        assertEquals(21, UserGoldenCoin.count());
        //今天没有签到过的情况
        goldenCoin.createdAt = DateHelper.beforeDays(2);
        goldenCoin.save();
        UserGoldenCoin.checkin(user, schedule.goods, "每天签到");
        assertEquals(21, UserGoldenCoin.count());
    }
}
