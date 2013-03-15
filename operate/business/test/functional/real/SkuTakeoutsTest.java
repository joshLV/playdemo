package functional.real;

import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import models.sales.InventoryStockItem;
import models.sales.Sku;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 实物根据订单出库的测试.
 * <p/>
 * User: sujie
 * Date: 3/15/13
 * Time: 9:14 AM
 */
public class SkuTakeoutsTest extends FunctionalTest {
    Sku sku;
    OrderItems orderItems;
    InventoryStockItem stockItem;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        //创建货品
        sku = FactoryBoy.create(Sku.class);
        //添加库存
        stockItem = FactoryBoy.create(InventoryStockItem.class);
        //创建订单
        orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.status = OrderStatus.PAID;
        orderItems.save();
        //指定货品和商品的关系
        Goods goods = FactoryBoy.last(Goods.class);
        goods.sku = sku;
        goods.skuCount = 2;
        goods.save();
    }


    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("SkuTakeoutsTest.index").url);
        assertIsOk(response);

        //render(paidOrderCount, preparingTakeoutSkuMap, takeoutSkuMap, skuAveragePriceMap, stockoutOrderList, deficientOrderList, toDate);

        Long paidOrderCount = (Long) renderArgs("paidOrderCount");
        assertEquals(1L, paidOrderCount.longValue());

        Map<Sku, Long> preparingTakeoutSkuMap = (Map<Sku, Long>) renderArgs("preparingTakeoutSkuMap");
        assertEquals(1L, preparingTakeoutSkuMap.size());
        assertEquals(4L, preparingTakeoutSkuMap.get(sku).longValue());

        Map<Sku, Long> takeoutSkuMap = (Map<Sku, Long>) renderArgs("takeoutSkuMap");
        assertEquals(1L, takeoutSkuMap.size());
        assertEquals(4L, takeoutSkuMap.get(sku).longValue());

        Map<Sku, BigDecimal> skuAveragePriceMap = (Map<Sku, BigDecimal>) renderArgs("skuAveragePriceMap");
        assertEquals(1, skuAveragePriceMap.size());
        assertEquals(4.25, skuAveragePriceMap.get(sku).doubleValue());

        List<Order> stockoutOrderList = (List<Order>) renderArgs("stockoutOrderList");
        assertEquals(1, stockoutOrderList.size());
        assertEquals(orderItems.order.id, stockoutOrderList.get(0).id);

        List<Order> deficientOrderList = (List<Order>) renderArgs("deficientOrderList");
        assertEquals(0, deficientOrderList.size());

        Date toDate = (Date) renderArgs("toDate");
        assertEquals(DateUtil.getBeginOfDay(), DateUtil.getBeginOfDay(toDate));
    }

}
    