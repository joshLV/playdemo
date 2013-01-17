package functional;

import factory.callback.BuildCallback;
import models.consumer.Address;
import models.order.OrderItems;
import models.sales.Goods;
import models.sales.GoodsHistory;

import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;

import java.util.Date;

/**
 * User: wangjia
 * Date: 12-11-9
 * Time: 上午9:48
 */
public class GoodsShowHistoryTest extends FunctionalTest {
    Shop shop;
    Goods goods;
    OrderItems orderItems;
    GoodsHistory goodsHistory;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods goods) {
                goods.updatedAt = new Date();
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class, new BuildCallback<OrderItems>() {
            @Override
            public void build(OrderItems orderItems) {
                orderItems.createdAt = new Date();
            }
        });
        goodsHistory = FactoryBoy.create(GoodsHistory.class);
    }


    @Test
    public void testGoodsShowHistory() {
        Http.Response response = GET("/p/" + goodsHistory.goodsId + "/h/" + goodsHistory.id);
        assertStatus(302, response);
        GoodsHistory getGoodsHistory = (GoodsHistory) renderArgs("goods");
        assertEquals(goodsHistory.name, getGoodsHistory.name);

    }
}
