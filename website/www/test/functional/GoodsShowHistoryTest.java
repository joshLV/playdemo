package functional;

import factory.FactoryBoy;
import models.consumer.User;
import models.order.OrderItems;
import models.sales.GoodsHistory;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-9
 * Time: 上午9:48
 * To change this template use File | Settings | File Templates.
 */
public class GoodsShowHistoryTest extends FunctionalTest {
    OrderItems orderItems;
    GoodsHistory goodsHistory;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        orderItems = FactoryBoy.create(OrderItems.class);
        goodsHistory = FactoryBoy.create(GoodsHistory.class);
    }

    @Test
    public void testGoodsShowHistory() {
        Http.Response response = GET("/p/" + goodsHistory.goodsId + "/h/" + goodsHistory.id + "/orderItem/" + orderItems.id);
        assertIsOk(response);
        GoodsHistory getGoodsHistory = (GoodsHistory) renderArgs("goods");
        assertEquals(goodsHistory.name, getGoodsHistory.name);

    }
}
