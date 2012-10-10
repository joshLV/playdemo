package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.PromoteRebate;
import models.sales.Goods;
import models.sales.MaterialType;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-9
 * Time: 下午3:53
 */
public class OrderRebateTest extends FunctionalTest {
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
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void test_推荐新注册用户建立推荐关系() {

        //----------建立推荐关系------------------------
        List old = User.findAll();
        int count = old.size();
        Map<String, String> loginUserParams = new HashMap<String, String>();
        loginUserParams.put("user.loginName", "12@qq.com");
        loginUserParams.put("user.password", "12@qq.com");
        loginUserParams.put("user.confirmPassword", "12@qq.com");
        loginUserParams.put("user.captcha", "A2WQ");
        loginUserParams.put("randomID", "RANDOMID");
        Cache.set("RANDOMID", "A2WQ", "30mn");
        Map<String, Http.Cookie> passCookie = new HashMap();
        Http.Cookie newCookie = new Http.Cookie();
        newCookie.name = "promoter_track";
        newCookie.value = "qweu2a";
        passCookie.put("promoter_track", newCookie);
        Http.Request request = FunctionalTest.newRequest();
        request.cookies = passCookie;
        Http.Response response = POST(request, "/register", loginUserParams, new HashMap<String, File>());
        assertStatus(200, response);
        response.removeCookie("promoter_track");
        List newList = User.findAll();
        assertEquals(count + 1, newList.size());
        List<PromoteRebate> plist = PromoteRebate.findAll();
        assertEquals(1, plist.size());
        assertEquals(user, plist.get(0).promoteUser);
        User invitedUser = User.findByLoginName("12@qq.com");
        assertEquals(invitedUser, plist.get(0).invitedUser);
        assertTrue(plist.get(0).registerFlag);

    }

    @Test
    public void test_下单推荐并获得返利() {
        inviteUser = FactoryBoy.create(User.class, "loginName", new BuildCallback<User>() {
            @Override
            public void build(User target) {
                target.promoterCode = "123";
                target.promoteUserId = user.id;
            }
        });

        promoteRebate = FactoryBoy.create(PromoteRebate.class, "UN_CONSUMED", new BuildCallback<PromoteRebate>() {
            @Override
            public void build(PromoteRebate target) {
                target.registerFlag = true;
                target.invitedUser = inviteUser;
                target.promoteUser = user;
            }
        });
        //----------下单购买并产生返利------------------------
        // 设置测试登录的用户名
        Security.setLoginUserForTest(inviteUser.loginName);
        Map<String, String> params = new HashMap<>();
        Goods goods = FactoryBoy.create(Goods.class);
        goods.materialType = MaterialType.ELECTRONIC;
        goods.salePrice = new BigDecimal(128.9);
        goods.save();

        String gids = goods.id.toString() + "-3,";
        int orderCount = Order.findAll().size();
        params.put("items", gids);
        params.put("mobile", "13800001111");
        params.put("remark", "hehe");
        params.put("discountSN", "qweu2a");
        Map<String, Http.Cookie> passCookie1 = new HashMap();
        Http.Cookie newCookie1 = new Http.Cookie();
        newCookie1.name = "promoter_track";
        newCookie1.value = "qweu2a";
        passCookie1.put("promoter_track", newCookie1);
        Http.Request request1 = FunctionalTest.newRequest();
        request1.cookies = passCookie1;
        Http.Response response = POST(request1, "/orders/new", params, new HashMap<String, File>());
        assertStatus(302, response);
        int resultOrderCount = Order.findAll().size();
        assertEquals(orderCount + 1, resultOrderCount);
        promoteRebate.refresh();
        assertEquals("11@qq.com", promoteRebate.invitedUser.loginName);
        assertEquals("selenium@uhuila.com", promoteRebate.promoteUser.loginName);
        assertEquals(new BigDecimal(7.74).setScale(2, BigDecimal.ROUND_HALF_UP), promoteRebate.rebateAmount);
        Order order = Order.find("promoteUserId=?", user.id).first();
        assertEquals(order, promoteRebate.order);
        assertFalse(promoteRebate.registerFlag);
    }


}
