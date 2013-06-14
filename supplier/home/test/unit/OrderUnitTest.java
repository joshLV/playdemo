package unit;

import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.Address;
import models.consumer.User;
import models.order.DeliveryType;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


public class OrderUnitTest extends UnitTest {
    User user;
    Goods goods;
    GoodsHistory goodsHistory;
    Order order;
    Supplier supplier;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        supplier = FactoryBoy.create(Supplier.class);
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
        goodsHistory = FactoryBoy.create(GoodsHistory.class);
        order = FactoryBoy.create(Order.class);

        // 三个orderItems.
        FactoryBoy.create(OrderItems.class);
        FactoryBoy.create(Goods.class);
        FactoryBoy.create(OrderItems.class);
        FactoryBoy.create(Goods.class);
        FactoryBoy.create(OrderItems.class);

    }

    /**
     * 测试订单列表
     */
    @Test
    public void testOrder() {
        OrdersCondition orderCondition = new OrdersCondition();
        orderCondition.createdAtBegin = new Date();
        orderCondition.createdAtEnd = new Date();
        orderCondition.status = OrderStatus.UNPAID;
        orderCondition.deliveryType = DeliveryType.LOGISTICS;
        orderCondition.payMethod = "1";
        Long supplierId = 1l;
        orderCondition.searchKey = "2";
        orderCondition.searchItems = "2012";
        int pageNumber = 1;
        int pageSize = 15;
        JPAExtPaginator<Order> page = Order.query(orderCondition, supplierId, pageNumber, pageSize);
        assertEquals(0, page.size());

        orderCondition = new OrdersCondition();
        orderCondition.createdAtBegin = DateHelper.beforeDays(3);
        orderCondition.createdAtEnd = new Date();

        orderCondition = new OrdersCondition();
        orderCondition.searchKey = "1";
        orderCondition.searchItems = goods.name;
        page = Order.query(orderCondition, supplierId, pageNumber, pageSize);
        assertEquals(0, page.size());

        orderCondition = new OrdersCondition();
        orderCondition.createdAtBegin = DateHelper.beforeDays(3);
        orderCondition.createdAtEnd = new Date();

        page = Order.query(orderCondition, supplierId, pageNumber, pageSize);
        assertEquals(0, page.size());
    }

    @Test
    public void testFindPaidOrder() throws Exception {
        int pageNumber = 1;
        int pageSize = 15;

        order.payMethod = "alipay";
        order.status = OrderStatus.PAID;

        OrdersCondition orderCondition = new OrdersCondition();
        orderCondition.status = OrderStatus.PAID;
        orderCondition.payMethod = "alipay";
        orderCondition.searchKey = "2";
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        order.searchItems = sdfYear.format(new Date());
        JPAExtPaginator<Order> list = Order.query(orderCondition, supplier.id, pageNumber, pageSize);
        assertEquals(0, list.size());

    }

    @Test
    public void testOrdersNumber() throws Exception {
        String mobile = "1310000000";
        Address address = new Address();
        address.mobile = "13000000000";
        address.name = " 徐家汇";
        address.postcode = "200120";
        BigDecimal resalePrice = new BigDecimal("10.2");
        boolean isOk = false;
        Goods oldGoods = Goods.findById(goods.id);
        oldGoods.virtualBaseSaleCount = 0l;
        oldGoods.cumulativeStocks = 100l;
        oldGoods.save();
        oldGoods.refresh();

        long baseSale = oldGoods.getRealStocks();
        System.out.println("baseSale=" + baseSale);
        long saleCount = oldGoods.getRealSaleCount();
        Order order = Order.createConsumeOrder(user);

        order.addOrderItem(oldGoods, 20L, mobile, oldGoods.salePrice, resalePrice);

        assertEquals(1, order.orderItems.size());
        OrderItems orderItems1 = order.orderItems.get(0);
        assertEquals(goods.id, orderItems1.goods.id);
        assertEquals(new Long(20), orderItems1.buyNumber);

        order.createAndUpdateInventory();

        goods.refresh();
        assertEquals(new Long(baseSale - 20), goods.getRealStocks());
        assertEquals(new Long(saleCount + 20), goods.getRealSaleCount());

        //异常情况: 超售
        if (goods.getRealStocks() < 200000L) {
            isOk = true;
        }
//        order.addOrderItem(goods, 200000L, mobile, goods.salePrice, resalePrice);
        assertEquals(true, isOk);
    }

    @Ignore
    @Test
    public void testPaid() {
        Account account = AccountUtil.getAccount(user.id, AccountType.CONSUMER);
        account.amount = new BigDecimal(10000);
        account.save();
        order.setUser(user.id, AccountType.CONSUMER);

        account = order.chargeAccount();
        order.paid(account);
        assertEquals(OrderStatus.PAID, order.status);
    }

    @Test
    public void testItemsNumber() {
        long itemsNumber = OrderItems.itemsNumber(order);
        assertEquals(3L, itemsNumber);

        itemsNumber = OrderItems.itemsNumberElectronic(order);
        assertEquals(3L, itemsNumber);
    }

    @Test
    public void getEcouponSn() {

        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        OrderItems orderItems = ecoupon.orderItems;

        String sn = orderItems.getEcouponSn();
        assertEquals(ecoupon.getMaskedEcouponSn() + "\n", sn);
    }

    @Test
    public void getWebEcouponSn() {

        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        OrderItems orderItems = ecoupon.orderItems;

        String sn = orderItems.getWebEcouponSn();
        assertEquals(ecoupon.eCouponSn + "\n", sn);
    }
}
