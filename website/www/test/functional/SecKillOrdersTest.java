package functional;

import controllers.modules.website.cas.Security;
import models.consumer.User;
import models.sales.SecKillGoodsItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-22
 * Time: 下午3:03
 * To change this template use File | Settings | File Templates.
 */
public class SecKillOrdersTest  extends FunctionalTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        User user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }


    @Test
    public void testIndex() {

        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);

        Http.Response response = GET("/seckill-orders?secKillGoodsItemId="+item.id);
        assertStatus(200, response);
        assertContentMatch("确认订单信息",response);

    }

    @Test
    public void testCreate() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);

        Map<String,String> params = new HashMap<>();
        params.put("secKillGoodsItemId",item.id.toString());
        params.put("secKillGoodsId",item.secKillGoods.id.toString());
        params.put("mobile","15026666875");
        params.put("remark","good");

        Http.Response response = POST("/seckill-orders/new",params);
        assertStatus(200, response);


    }


}
