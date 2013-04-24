package functional.real;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrderType;
import models.sales.Goods;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.MaterialType;
import models.sales.OrderBatch;
import models.sales.Sku;
import models.sales.StockActionType;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
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
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        Supplier shihui = FactoryBoy.create(Supplier.class, "seewi");

        //创建货品
        sku = FactoryBoy.create(Sku.class);
        //指定货品和商品的关系
        Goods goods = FactoryBoy.create(Goods.class);
        goods.materialType = MaterialType.REAL;
        goods.sku = sku;
        goods.skuCount = 2;
        goods.supplierId = Supplier.getShihui().id;
        goods.save();

        //添加库存
        stockItem = FactoryBoy.create(InventoryStockItem.class);

        //创建订单
        orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.goods = goods;
        orderItems.status = OrderStatus.PAID;
        orderItems.save();

        orderItems.order.orderType = OrderType.CONSUME;
        orderItems.order.paidAt = new Date();
        orderItems.order.save();

    }

    /**
     * 不缺货情况的出库信息
     */
    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("SkuTakeouts.index").url);
        assertIsOk(response);

        //render(paidOrderCount, preparingTakeoutSkuMap, takeoutSkuMap, skuAveragePriceMap, stockoutOrderList, deficientOrderList, toDate);
        Long paidOrderCount = (Long) renderArgs("paidOrderCount");
        assertEquals(1L, paidOrderCount.longValue());

        Map<Sku, Long> preparingTakeoutSkuMap = (Map<Sku, Long>) renderArgs("preparingTakeoutSkuMap");
        assertEquals(1L, preparingTakeoutSkuMap.size());
        assertEquals(2L, preparingTakeoutSkuMap.get(sku).longValue());

        Map<Sku, Long> takeoutSkuMap = (Map<Sku, Long>) renderArgs("takeoutSkuMap");
        assertEquals(1L, takeoutSkuMap.size());
        assertEquals(2L, takeoutSkuMap.get(sku).longValue());

        Map<Sku, BigDecimal> skuAveragePriceMap = (Map<Sku, BigDecimal>) renderArgs("skuAveragePriceMap");
        assertEquals(1, skuAveragePriceMap.size());
        assertEquals(0, skuAveragePriceMap.get(sku).compareTo(new BigDecimal(4.25)));

        List<Order> stockoutOrderList = (List<Order>) renderArgs("stockoutOrderList");
        assertEquals(1, stockoutOrderList.size());
        assertEquals(orderItems.order.id, stockoutOrderList.get(0).id);

        List<Order> deficientOrderList = (List<Order>) renderArgs("deficientOrderList");
        assertEquals(0, deficientOrderList.size());

        Date toDate = (Date) renderArgs("toDate");
        assertEquals(DateUtil.getBeginOfDay().getTime(), DateUtil.getBeginOfDay(toDate).getTime());
    }

    @Test
    public void testIndex_缺货() {
        //创建订单
        Order order = FactoryBoy.create(Order.class);

        OrderItems orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.buyNumber = 5L;
        orderItems.status = OrderStatus.PAID;
        orderItems.save();

        orderItems.order.orderType = OrderType.CONSUME;
        orderItems.order.paidAt = new Date();
        orderItems.order.save();

        Http.Response response = GET(Router.reverse("SkuTakeouts.index").url);
        assertIsOk(response);

        //render(paidOrderCount, preparingTakeoutSkuMap, takeoutSkuMap, skuAveragePriceMap, stockoutOrderList, deficientOrderList, toDate);
        Long paidOrderCount = (Long) renderArgs("paidOrderCount");
        assertEquals(2L, paidOrderCount.longValue());

        Map<Sku, Long> preparingTakeoutSkuMap = (Map<Sku, Long>) renderArgs("preparingTakeoutSkuMap");
        assertEquals(1L, preparingTakeoutSkuMap.size());
        assertEquals(12L, preparingTakeoutSkuMap.get(sku).longValue());

        Map<Sku, Long> takeoutSkuMap = (Map<Sku, Long>) renderArgs("takeoutSkuMap");
        assertEquals(1L, takeoutSkuMap.size());
        assertEquals(2L, takeoutSkuMap.get(sku).longValue());

        Map<Sku, BigDecimal> skuAveragePriceMap = (Map<Sku, BigDecimal>) renderArgs("skuAveragePriceMap");
        assertEquals(1, skuAveragePriceMap.size());
        assertEquals(0, skuAveragePriceMap.get(sku).compareTo(new BigDecimal(4.25)));

        List<Order> stockoutOrderList = (List<Order>) renderArgs("stockoutOrderList");
        assertEquals(1, stockoutOrderList.size());
        assertEquals(this.orderItems.order.id, stockoutOrderList.get(0).id);

        List<Order> deficientOrderList = (List<Order>) renderArgs("deficientOrderList");
        assertEquals(1, deficientOrderList.size());
        assertEquals(order.id, deficientOrderList.get(0).id);

        Date toDate = (Date) renderArgs("toDate");
        assertEquals(DateUtil.getBeginOfDay().getTime(), DateUtil.getBeginOfDay(toDate).getTime());
    }


    /**
     * 出库操作测试
     */
    @Test
    public void testStockOut() {
        Map<String, String> params = new HashMap<>();
        params.put("toDate", DateUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        params.put("stockoutOrderId", String.valueOf(orderItems.order.id));
        Http.Response response = POST("/sku-takeouts", params);
        assertStatus(200, response);

        orderItems.refresh();
        //订单状态检查
        assertEquals(OrderStatus.PREPARED, orderItems.status);

        //出库单检查
        List<InventoryStock> stockList = InventoryStock.find("actionType=?", StockActionType.OUT).fetch();
        assertEquals(1, stockList.size());
        assertEquals(orderItems.goods.supplierId, stockList.get(0).supplier.id);
        //出库批次检查
        List<OrderBatch> batchList = OrderBatch.find("stock=?", stockList.get(0)).fetch();
        assertEquals(1, batchList.size());
        assertEquals(batchList.get(0).id, orderItems.orderBatch.id);
        //检查库存剩余数量
        stockItem.refresh();
        assertEquals(stockItem.changeCount - orderItems.getSkuCount(), (Object) stockItem.remainCount);
    }

}
