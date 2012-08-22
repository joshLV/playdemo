package functional;

import controllers.modules.website.cas.Security;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.*;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-22
 * Time: 下午2:17
 * To change this template use File | Settings | File Templates.
 */
public class OrderFuncTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(Order.class);
        Fixtures.delete(User.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Supplier.class);

        Fixtures.loadModels("fixture/user.yml");
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");

        //设置虚拟登陆
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        User user = User.findById(userId);

        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testCreate(){

        Map<String,String> params =new HashMap<>();
        Long goodsId1 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long goodsId2 = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        String order = goodsId1.toString()+"-3,"+goodsId2.toString()+"-2,";
        System.out.println("items>>>>>>>>>>>>>>>"+order);
        params.put("items", order);
        params.put("mobile","13800001111");
        params.put("remark","hehe");

        Http.Response response =POST("/orders/new",params);
        assertStatus(302,response);
        System.out.println(response.out.toString());

    }

}
