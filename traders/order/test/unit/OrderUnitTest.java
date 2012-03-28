package unit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.accounts.AccountType;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(models.order.OrderItems.class);
        Fixtures.delete(models.order.Order.class);
        Fixtures.delete(models.sales.Goods.class);
        Fixtures.delete(User.class);
        Fixtures.loadModels("fixture/goods_base.yml", 
        		"fixture/user.yml",
        		"fixture/goods.yml",
                "fixture/orders.yml", "fixture/orderItems.yml");
    }

    /**
     * 测试订单列表
     */
    @Test
    public void testOrder() {
        Order order = new Order();
        order.createdAtBegin = new Date();
        order.createdAtEnd = new Date();
        order.status = OrderStatus.UNPAID;
        order.deliveryType = 1;
        order.payMethod = "1";
        Long supplierId = 1l;
        order.searchKey = "2";
        order.searchItems = "2012";
        int pageNumber = 1;
        int pageSize = 15;
        List<Order> list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(0, list.size());

        order = new Order();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            order.createdAtBegin = sdf.parse("2012-03-01");
            order.createdAtEnd = new Date();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        order.status = OrderStatus.PAID;
        order.deliveryType = 1;
        order.payMethod = "alipay";
        order.searchKey = "2";
        order.searchItems = "2012";
        list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(1, list.size());

        order = new Order();
        order.searchKey = "1";
        order.searchItems = "哈根达斯200";
        list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(1, list.size());

        order = new Order();
        try {
            order.refundAtBegin = sdf.parse("2012-03-01");
            order.refundAtEnd = new Date();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        list = Order.query(order, supplierId, pageNumber, pageSize);
        assertEquals(0, list.size());
    }


    @Test
    public void testOrders() {
    	Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        Address address = new Address();
        address.mobile = "13000000000";
        address.name = " 徐家汇";
        address.postcode = "200120";
        Order orders = new Order(userId,AccountType.CONSUMER, address);
        assertNotNull(orders);
    }

    @Test
    public void testOrdersNumber() {
        String mobile = "1310000000";
    	Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        Address address = new Address();
        address.mobile = "13000000000";
        address.name = " 徐家汇";
        address.postcode = "200120";
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        boolean isOk = false;
        try {
            Order orders = new Order(userId, AccountType.CONSUMER,
                    goodsId, 2l, address, mobile);
            assertNotNull(orders);
            new Order(userId, AccountType.CONSUMER, goodsId,
                    200000l, address, mobile);
        } catch (NotEnoughInventoryException e) {
            isOk = true;
        }
        assertEquals(true, isOk);
    }

    @Test
    public void testOrdersCart() {
        String mobile = "1310000000";
        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
        Address address = new Address();
        address.mobile = "13000000000";
        address.name = " 徐家汇";
        address.postcode = "200120";
        List<Cart> cartList = new ArrayList<>();
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods goods = Goods.findById(goodsId);
        Cart cart = new Cart(goods, 2l);
        cartList.add(cart);
        boolean isOk = false;
        try {
            Order orders = new Order(userId, AccountType.CONSUMER,
                    cartList, address, mobile);
            assertNotNull(orders);
        } catch (NotEnoughInventoryException e) {
            isOk = true;
        }
        assertFalse(isOk);

    }

    @Test
    public void testPaid() {
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order orders = Order.findById(orderId);
        orders.paid();
        assertEquals(OrderStatus.PAID, orders.status);
    }

    @Test
    public void testItemsNumber() {
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order orders = Order.findById(orderId);
        long itemsNumber = OrderItems.itemsNumber(orders);
        assertEquals(5L, itemsNumber);
    }


}
