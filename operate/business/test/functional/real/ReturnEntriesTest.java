package functional.real;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrderType;
import models.order.RealGoodsReturnEntry;
import models.order.RealGoodsReturnStatus;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.InventoryStockItem;
import models.sales.MaterialType;
import models.sales.Sku;
import models.supplier.Supplier;
import org.junit.Test;
import play.Logger;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
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
    GoodsHistory goodsHistory;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();

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

        goodsHistory = FactoryBoy.create(GoodsHistory.class);

        //添加库存
        stockItem = FactoryBoy.create(InventoryStockItem.class);

        //创建订单
        orderItems = FactoryBoy.create(OrderItems.class);
        orderItems.goods = goods;
        orderItems.status = OrderStatus.PAID;
        orderItems.goodsHistoryId=goodsHistory.id;
        orderItems.save();



        orderItems.order.orderType = OrderType.CONSUME;
        orderItems.order.paidAt = new Date();
        orderItems.order.refundedAmount = BigDecimal.ZERO;
        orderItems.order.amount = orderItems.getAmount();
        orderItems.order.needPay = orderItems.getAmount();
        orderItems.order.promotionBalancePay = BigDecimal.ZERO;
        orderItems.order.save();

        entry = FactoryBoy.create(RealGoodsReturnEntry.class);

        platformIncomingAccount = AccountUtil.getPlatformIncomingAccount();
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
    public void testReceived() throws Exception {
        BigDecimal oldPlatformAmount = platformIncomingAccount.amount;
        assertEquals(new BigDecimal("8.5"), orderItems.getAmount());
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", entry.id);
        Http.Response response = PUT(Router.reverse("real.ReturnEntries.received", urlParams).url,
                "application/x-www-form-urlencoded", "");
        assertStatus(302, response);

        entry.refresh();
        assertEquals(RealGoodsReturnStatus.RETURNED, entry.status);

        platformIncomingAccount.refresh();
        assertEquals(oldPlatformAmount.subtract(orderItems.order.amount).setScale(2), platformIncomingAccount.amount);
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

        Logger.info("orderItem.id=" +  orderItems.id + " of PAID:" + orderItems.status);
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
