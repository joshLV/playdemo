package unit;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.Goods;
import models.sales.GoodsHistory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(models.order.OrderItems.class);
        Fixtures.delete(models.order.Order.class);
        Fixtures.delete(models.sales.Goods.class);
        Fixtures.delete(User.class);
        Fixtures.loadModels("fixture/goods_base.yml",
                "fixture/user.yml",
                "fixture/goods.yml",
                "fixture/goodsHistory.yml",
                "fixture/payment_source.yml",
                "fixture/orders.yml", "fixture/orderItems.yml");
    }

    /**
     * 测试订单列表
     */
    @Test
    public void testOrder() {
        OrdersCondition order = new OrdersCondition();
        order.createdAtBegin = new Date();
        order.createdAtEnd = new Date();
        order.status = OrderStatus.UNPAID;
        order.deliveryType = DeliveryType.LOGISTICS;
        order.payMethod = "1";
        Long supplierId = 1l;
        order.searchKey = "2";
        order.searchItems = "2012";
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<Order> list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(0, list.size());

        order = new OrdersCondition();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            order.createdAtBegin = sdf.parse("2012-03-01");
            order.createdAtEnd = new Date();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        order.status = OrderStatus.PAID;
        order.payMethod = "alipay";
        order.searchKey = "2";
        order.searchItems = "2012";
        list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(1, list.size());

        order = new OrdersCondition();
        order.searchKey = "1";
        order.searchItems = "哈根达斯200";
        list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(2, list.size());

        order = new OrdersCondition();
        try {
            order.refundAtBegin = sdf.parse("2012-03-01");
            order.refundAtEnd = new Date();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(0, list.size());
    }

    // FIXME
    @Ignore
    @Test
    public void testOrdersNumber() throws Exception {
        String mobile = "1310000000";
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        Address address = new Address();
        address.mobile = "13000000000";
        address.name = " 徐家汇";
        address.postcode = "200120";
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long goodsHistoryId = (Long) Fixtures.idCache.get("models.sales.GoodsHistory-GoodsHistory_001");
        BigDecimal resalePrice = new BigDecimal("10.2");
        boolean isOk = false;
        Goods oldGoods = Goods.findById(goodsId);
        oldGoods.virtualBaseSaleCount = 0l;
        oldGoods.cumulativeStocks = 100l;
        oldGoods.save();
        oldGoods.refresh();
        
        long baseSale = oldGoods.getRealStocks();
        long saleCount = oldGoods.getRealSaleCount();
        Order order = Order.createConsumeOrder(userId, AccountType.CONSUMER);
        Goods goods1 = Goods.findById(goodsId);
        GoodsHistory goodsHistory = GoodsHistory.findById(goodsHistoryId);
        goodsHistory.goodsId = goods1.id;
        goodsHistory.save();
        order.addOrderItem((Goods) Goods.findById(goodsId), 20, mobile, oldGoods.salePrice, resalePrice);
        order.createAndUpdateInventory();

        Goods goods = Goods.findById(goodsId);
        assertEquals(new Long(baseSale - 20), goods.getRealStocks());
        assertEquals(new Long(saleCount + 20), goods.getRealSaleCount());

        try {
            //异常情况
            order.addOrderItem((Goods) Goods.findById(goodsId), 200000, mobile, goods.salePrice, resalePrice);
        } catch (NotEnoughInventoryException e) {
            isOk = true;
        }
        assertEquals(true, isOk);
    }


    @Test
    public void testPaid() {
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        Order orders = Order.findById(orderId);

        orders.setUser(userId, AccountType.CONSUMER);
        orders.paid();
        assertEquals(OrderStatus.PAID, orders.status);
    }

    @Test
    public void testItemsNumber() {
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order orders = Order.findById(orderId);
        long itemsNumber = OrderItems.itemsNumber(orders);
        assertEquals(3L, itemsNumber);

        itemsNumber = OrderItems.itemsNumberElectronic(orders);
        assertEquals(2L, itemsNumber);
    }

    @Test
    public void getEcouponSn() {
        Long id = (Long) Fixtures.idCache.get("models.order.OrderItems-orderItems1");
        OrderItems orderItems = OrderItems.findById(id);
        String sn = orderItems.getEcouponSn();
        String s = "******7001\n******7003\n******7004\n";
        assertEquals(s, sn);
    }

    @Test
    public void getWebEcouponSn() {
        Long id = (Long) Fixtures.idCache.get("models.order.OrderItems-orderItems1");
        OrderItems orderItems = OrderItems.findById(id);
        String sn = orderItems.getWebEcouponSn();
        String s = "1234567001\n1234567003\n1234567004\n";
        assertEquals(s, sn);
    }
}
