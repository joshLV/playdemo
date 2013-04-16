package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.resale.ResalerFactory;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单的功能测试.
 * <p/>
 * User: hejun
 * Date: 12-8-22
 * Time: 下午2:17
 */
public class OrderFuncTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(Goods.class);

        //设置虚拟登陆
        // 设置测试登录的用户名
        User user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);
        ResalerFactory.getYibaiquanResaler(); //必须存在一百券
    }

    @Test
    public void testIndex_无参数() {
        Http.Response response = GET("/orders");
        assertStatus(500, response);
    }

    @Test
    public void testIndex_有参数() {
        User user = FactoryBoy.last(User.class);
        Goods goodsA = FactoryBoy.last(Goods.class);
        Goods goodsB = FactoryBoy.create(Goods.class);
        Order order = FactoryBoy.create(Order.class);
        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        order.userId = user.id;
        order.save();
        orderItems.phone = user.mobile;
        orderItems.save();

        String[] args = new String[]{
                String.format("gid=%s", goodsA.id),
                String.format("g%s=1", goodsA.id),
                String.format("gid=%s", goodsB.id),
                String.format("g%s=1", goodsB.id),
        };
        //购买两个商品，一件各一个
        Http.Response response = GET("/orders?" + StringUtils.join(args, "&"));
        assertStatus(200, response);
        //测试renderArgs
        String queryStringInArgs = (String) renderArgs("querystring");
        for (String arg : args) {
            assertTrue("test arg:" + arg, queryStringInArgs.contains(arg));
        }
        assertNotNull(renderArgs("mobile"));
        assertEquals(1, ((List) renderArgs("orderItems_mobiles")).size());

        assertEquals(0,
                goodsA.salePrice.multiply(new BigDecimal("1.00"))
                        .add(goodsB.salePrice.multiply(new BigDecimal("1.00")))
                        .compareTo((BigDecimal) renderArgs("goodsAmount")));

        assertEquals(renderArgs("goodsAmount"), renderArgs("totalAmount"));
        assertEquals(renderArgs("goodsAmount"), renderArgs("needPay"));
//        assertEquals(String.format("%s-1,%s-1,", goodsA.id, goodsB.id), renderArgs("items"));
    }


    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();
        Goods goodsA = FactoryBoy.last(Goods.class);
        Goods goodsB = FactoryBoy.create(Goods.class);
        String items = String.format("%s-3,%s-2", goodsA.id, goodsB.id);

        long orderCountBefore = Order.count();
        params.put("items", items);
        params.put("mobile", "13800001111");
        params.put("remark", "hehe");

        Http.Response response = POST("/orders/new", params);
        assertStatus(302, response);

        long orderCountAfter = Order.count();
        assertEquals(orderCountBefore + 1, orderCountAfter);
    }

}
