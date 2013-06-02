package functional.real;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
import models.operator.Operator;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrderType;
import models.order.RealGoodsReturnEntry;
import models.order.RealGoodsReturnStatus;
import models.order.TakeoutItem;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.MaterialType;
import models.sales.OrderBatch;
import models.sales.Sku;
import models.sales.StockActionType;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Test;
import play.Logger;
import play.modules.paginate.JPAExtPaginator;
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
 * User: tanglq
 * Date: 13-4-12
 * Time: 下午2:42
 */
public class ReturnEntriesTest extends FunctionalTest {


    Sku sku;
    OrderItems orderItems;
    InventoryStockItem stockItem;
    RealGoodsReturnEntry entry;
    Account platformIncomingAccount;
    Goods goods;
    GoodsHistory goodsHistory;

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
        goods = FactoryBoy.create(Goods.class);
        goods.materialType = MaterialType.REAL;
        goods.sku = sku;
        goods.skuCount = 2;
        goods.supplierId = Supplier.getShihui().id;
        goods.save();

        goodsHistory = FactoryBoy.create(GoodsHistory.class);

        //添加库存
        stockItem = FactoryBoy.create(InventoryStockItem.class);

        //创建订单
        orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.goods = goods;
        orderItems.status = OrderStatus.PAID;
        orderItems.goodsHistoryId = goodsHistory.id;
        orderItems.save();


        orderItems.order.orderType = OrderType.CONSUME;
        orderItems.order.paidAt = new Date();
        orderItems.order.refundedAmount = BigDecimal.ZERO;
        orderItems.order.amount = orderItems.getAmount();
        orderItems.order.needPay = orderItems.getAmount();
        orderItems.order.promotionBalancePay = BigDecimal.ZERO;
        orderItems.order.save();

        entry = FactoryBoy.create(RealGoodsReturnEntry.class);

        platformIncomingAccount = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        platformIncomingAccount.amount = new BigDecimal(1000l);
        platformIncomingAccount.save();

    }

    @Test
    public void testIndex() throws Exception {
        Http.Response response = GET(Router.reverse("real.ReturnEntries.index").url);
        assertIsOk(response);
        JPAExtPaginator<RealGoodsReturnEntry> entryPage = (JPAExtPaginator<RealGoodsReturnEntry>) renderArgs("entryPage");
        assertEquals(1, entryPage.getRowCount());
    }




    @Test
    public void testTakeoutSkuChangeReceived() throws Exception {

        //先根据订单自动出库
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

        assertEquals(1l, TakeoutItem.count());
        TakeoutItem takeoutItem = (TakeoutItem) TakeoutItem.findAll().get(0);
        assertEquals(sku, takeoutItem.sku);
        assertEquals(Long.valueOf(2), takeoutItem.count);
        assertEquals(orderItems, takeoutItem.orderItem);
        //修改商品对应sku的关系
        Sku sku1 = FactoryBoy.create(Sku.class);
        goods.sku = sku1;
        goods.skuCount = 4;
        goods.save();
        //入库10件
        InventoryStockItem stockItem1 = FactoryBoy.create(InventoryStockItem.class);
        assertEquals(8, sku.getRemainCount());


        //orderItem状态：待打包  操作申请退货后 已收到货 更新相应sku库存 入库
        BigDecimal oldPlatformAmount = platformIncomingAccount.amount;
        assertEquals(new BigDecimal("8.5").setScale(2), orderItems.getAmount().setScale(2));
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", entry.id);
        response = PUT(Router.reverse("real.ReturnEntries.received", urlParams).url,
                "application/x-www-form-urlencoded", "");
        assertStatus(302, response);

        entry.refresh();
        assertEquals(RealGoodsReturnStatus.RETURNED, entry.status);

        platformIncomingAccount.refresh();
        assertEquals(oldPlatformAmount.subtract(orderItems.order.amount).setScale(2), platformIncomingAccount.amount);
        assertEquals(10, sku.getRemainCount());
        assertEquals(10, sku1.getRemainCount());
    }


    @Test
    public void testUnreceived() throws Exception {
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", entry.id);
        Http.Response response = PUT(Router.reverse("real.ReturnEntries.unreceived", urlParams).url,
                "application/x-www-form-urlencoded", "unreceivedReason=Test");
        assertStatus(302, response);

        entry.refresh();
        assertEquals(RealGoodsReturnStatus.RETURNED, entry.status);
    }


    @Test
    public void testReturnGoodsForPaidOrder() throws Exception {
        RealGoodsReturnEntry.deleteAll();
        orderItems.status = OrderStatus.PAID;
        orderItems.save();

        Logger.info("orderItem.id=" + orderItems.id + " of PAID:" + orderItems.status);
        Map<String, String> params = new HashMap<>();
        params.put("entry.orderItems.id", orderItems.id.toString());
        params.put("entry.returnedCount", "1");
        params.put("entry.reason", "for test");
        Http.Response response = POST(Router.reverse("real.ReturnEntries.returnGoods").url, params);
        assertIsOk(response);

        entry = RealGoodsReturnEntry.find("order by id desc").first();
        assertEquals(RealGoodsReturnStatus.RETURNED, entry.status);
        assertEquals("for test", entry.reason);
    }

    @Test
    public void testReturnGoodsForPREPAREDOrder() throws Exception {
        RealGoodsReturnEntry.deleteAll();
        orderItems.status = OrderStatus.PREPARED;
        orderItems.save();

        Map<String, String> params = new HashMap<>();
        params.put("entry.orderItems.id", orderItems.id.toString());
        params.put("entry.returnedCount", "1");
        params.put("entry.reason", "for test");
        Http.Response response = POST(Router.reverse("real.ReturnEntries.returnGoods").url, params);
        assertIsOk(response);

        orderItems.refresh();
        entry = RealGoodsReturnEntry.find("order by id desc").first();
        assertEquals(RealGoodsReturnStatus.RETURNING, entry.status);
        assertEquals(OrderStatus.RETURNING, orderItems.status);
        assertEquals("for test", entry.reason);
    }
}
