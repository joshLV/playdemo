package unit;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.consumer.UserCondition;
import models.consumer.UserGoldenCoin;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-18
 * Time: 下午5:07
 */
public class UserGoldenUnitTest extends UnitTest {
    User user;

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
                        target.createdAt = new Date();
                    }
                });

        FactoryBoy.create(UserGoldenCoin.class);
    }

    @Test
    public void testList() {
        JPAExtPaginator<UserGoldenCoin> reportPage = UserGoldenCoin.find(user,new UserCondition(), 1, 15);
        assertEquals(21, reportPage.size());
        assertEquals(21, UserGoldenCoin.getCheckinNumber(user).intValue());
        assertEquals(105, UserGoldenCoin.getTotalCoins(user).intValue());
    }
}
