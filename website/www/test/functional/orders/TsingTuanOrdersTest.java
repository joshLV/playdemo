package functional.orders;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.resale.ResalerFactory;
import models.consumer.User;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Goods;
import models.supplier.Supplier;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.util.HashMap;
import java.util.Map;

public class TsingTuanOrdersTest extends FunctionalTest {
    
    Supplier supplier;
    
    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        //设置虚拟登陆
        // 设置测试登录的用户名
        User user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);
        
        MockMQ.clear();

        ResalerFactory.getYibaiquanResaler(); //一百券必须存在
        
        // 建立清团商户.
        supplier = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier s) {
                s.domainName = "tsingtuan";
            }
        });        
    }

    @Test
    public void testCreateOrder() {
        Map<String, String> params = new HashMap<>();
        Goods goodsA = FactoryBoy.create(Goods.class);
        Goods goodsB = FactoryBoy.create(Goods.class);
        String items = String.format("%s-3,%s-2", goodsA.id, goodsB.id);

        long orderCountBefore = Order.count();
        params.put("items", items);
        params.put("mobile", "13800001111");
        params.put("remark", "hehe");

        Http.Response response = POST("/orders/new", params);
        assertStatus(302, response);

        assertEquals(orderCountBefore + 1, Order.count());
        
        OrderItems orderItem = OrderItems.find("goods_id=? order by id desc", goodsA.id).first();
        String location = response.getHeader("Location");
        assertEquals("/payment_info/" + orderItem.order.orderNumber, location);
    }
    
    
    public void testConfirmOrder() {
        TsingTuanOrder tsingTuanOrder = (TsingTuanOrder) MockMQ.getLastMessage(TsingTuanSendOrder.SEND_ORDER);
        assertNotNull(tsingTuanOrder);
    }

}
