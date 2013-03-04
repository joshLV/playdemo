package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.consumer.User;
import models.consumer.UserGoldenCoin;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.DateHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-19
 * Time: 下午1:41
 */
public class UserGoldenTest extends FunctionalTest {
    User user;
    UserGoldenCoin goldenCoin;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        final Goods goods = FactoryBoy.create(Goods.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        FactoryBoy.batchCreate(20, UserGoldenCoin.class,
                new SequenceCallback<UserGoldenCoin>() {
                    @Override
                    public void sequence(UserGoldenCoin target, int seq) {
                        target.goods = goods;
                        target.user = user;
                        target.remarks = "签到20天";
                        target.checkinNumber = 5L;
                        target.createdAt = DateHelper.beforeDays(1);
                    }
                });

        goldenCoin = FactoryBoy.create(UserGoldenCoin.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/user-coins");
        assertIsOk(response);
        List<UserGoldenCoin> coinList = (List) renderArgs("coinList");
        assertEquals(21, coinList.size());
        Long coinsNumber = (Long) renderArgs("coinsNumber");
        Long num = (Long) renderArgs("checkinNumber");
        assertNull(renderArgs("isExchange"));
        assertEquals(105, coinsNumber.intValue());
        assertEquals(0, num.intValue());
    }

    @Test
    public void testExchange() {
        goldenCoin.checkinNumber = 1000l;
        goldenCoin.save();
        Map<String, String> params = new HashMap();
        params.put("exNumber", "1");
        Http.Response response = POST("/coins-exchange", params);
        assertStatus(302, response);

        response = GET("/user-coins");
        assertIsOk(response);
        List<UserGoldenCoin> coinList = (List) renderArgs("coinList");
        assertEquals(22, coinList.size());
        Long coinsNumber = (Long) renderArgs("coinsNumber");
        Long num = (Long) renderArgs("checkinNumber");
        assertEquals(600, coinsNumber.intValue());
        assertEquals(1, num.intValue());
    }
}
