package unit;

import java.util.List;

import models.accounts.Account;
import models.accounts.AccountType;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * 用于优惠券合并产生相同replayCode的测试
 * 
 * @author 唐力群
 * 
 */
public class ECouponSmsMergeTest extends UnitTest {
    /**
     * 有分组代码的商品.
     */
    Goods groupedGoods1 = null;
    Goods groupedGoods2 = null;
    Goods otherGroupedGoods = null;

    /**
     * 无分组的商品.
     */
    Goods singleGoods = null;

    /**
     * 测试用消费者.
     */
    User user = null;

    @Before
    public void setup() {
        Play.configuration.setProperty(
                        ECoupon.KEY_USE_PRODUCT_SERIAL_REPLYCODE, "true");
        Fixtures.delete(Account.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(Account.class);
        Fixtures.delete(SupplierRole.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(SupplierUser.class);
        Fixtures.loadModels("fixture/goods_base.yml", "fixture/roles.yml",
                        "fixture/supplierusers.yml",
                        "fixture/user.yml",
                        "fixture/goods.yml", "fixture/accounts.yml",
                        "fixture/orders.yml",
                        "fixture/orderItems.yml");

        Long supplierId = (Long) Fixtures.idCache
                        .get("models.supplier.Supplier-kfc");
        Long goodsId = (Long) Fixtures.idCache
                        .get("models.sales.Goods-Goods_002");
        groupedGoods1 = Goods.findById(goodsId);
        groupedGoods1.supplierId = supplierId;
        groupedGoods1.groupCode = "group1";
        groupedGoods1.save();

        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");
        groupedGoods2 = Goods.findById(goodsId);
        groupedGoods2.supplierId = supplierId;
        groupedGoods2.groupCode = "group1";
        groupedGoods2.save();

        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        otherGroupedGoods = Goods.findById(goodsId);
        otherGroupedGoods.supplierId = supplierId;
        otherGroupedGoods.groupCode = "group2";
        otherGroupedGoods.save();

        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        singleGoods = Goods.findById(goodsId);
        singleGoods.supplierId = supplierId;
        singleGoods.groupCode = null;
        singleGoods.save();

        Long userId = (Long) Fixtures.idCache
                        .get("models.consumer.User-user_test2");
        user = User.findById(userId);

    }

    @Test
    public void 购买同组不同商品生成的ECoupon会有相同的ReplyCode() throws Exception {
        Order order = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order.save();

        generateECoupon(order, groupedGoods1, user, 1);
        generateECoupon(order, groupedGoods2, user, 1);

        List<ECoupon> ecoupons = ECoupon.findByOrder(order);
        assertNotNull(ecoupons);
        assertEquals(2, ecoupons.size());

        assertNotSame(ecoupons.get(0).goods.id, ecoupons.get(1).goods.id);
        assertNotSame(ecoupons.get(0).eCouponSn, ecoupons.get(1).eCouponSn);
        // replyCode应当相同
        assertEquals(ecoupons.get(0).replyCode, ecoupons.get(1).replyCode);
    }

    @Test
    public void 不同订单购买同组不同商品生成的ECoupon会有相同的ReplyCode() throws Exception {
        Order order1 = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order1.save();

        generateECoupon(order1, groupedGoods1, user, 1);

        Order order2 = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order2.save();
        generateECoupon(order2, groupedGoods2, user, 1);

        List<ECoupon> ecoupons1 = ECoupon.findByOrder(order1);
        assertNotNull(ecoupons1);
        assertEquals(1, ecoupons1.size());

        List<ECoupon> ecoupons2 = ECoupon.findByOrder(order2);
        assertNotNull(ecoupons2);
        assertEquals(1, ecoupons2.size());

        assertNotSame(ecoupons1.get(0).goods.id, ecoupons2.get(0).goods.id);
        assertNotSame(ecoupons1.get(0).eCouponSn, ecoupons2.get(0).eCouponSn);
        // replyCode应当相同
        assertEquals(ecoupons1.get(0).replyCode, ecoupons2.get(0).replyCode);
    }

    @Test
    public void 购买不同组不同商品生成的ECoupon的ReplyCode各不相同() throws Exception {
        Order order = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order.save();

        generateECoupon(order, groupedGoods1, user, 1);
        generateECoupon(order, otherGroupedGoods, user, 1);

        List<ECoupon> ecoupons = ECoupon.findByOrder(order);
        assertNotNull(ecoupons);
        assertEquals(2, ecoupons.size());

        assertNotSame(ecoupons.get(0).goods.id, ecoupons.get(1).goods.id);
        assertNotSame(ecoupons.get(0).goods.groupCode,
                        ecoupons.get(1).goods.groupCode);
        assertNotSame(ecoupons.get(0).eCouponSn, ecoupons.get(1).eCouponSn);
        // replyCode不相同
        assertNotSame(ecoupons.get(0).replyCode, ecoupons.get(1).replyCode);
    }

    @Test
    public void 购买有组与无组的两个商品生成的ECoupon的ReplyCode各不相同() throws Exception {
        Order order = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order.save();

        generateECoupon(order, groupedGoods1, user, 1);
        generateECoupon(order, singleGoods, user, 1);

        List<ECoupon> ecoupons = ECoupon.findByOrder(order);
        assertNotNull(ecoupons);
        assertEquals(2, ecoupons.size());

        assertNotSame(ecoupons.get(0).goods.id, ecoupons.get(1).goods.id);
        assertNotSame(ecoupons.get(0).goods.groupCode,
                        ecoupons.get(1).goods.groupCode);
        assertNotSame(ecoupons.get(0).eCouponSn, ecoupons.get(1).eCouponSn);
        // replyCode不相同
        assertNotSame(ecoupons.get(0).replyCode, ecoupons.get(1).replyCode);
    }

    @Test
    public void 购买未设置组代码的同一商品生成的ECoupon会产生相同的ReplyCode() throws Exception {
        Order order = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order.save();

        // 生成2个同一商品的ECoupon
        generateECoupon(order, singleGoods, user, 2);

        List<ECoupon> ecoupons = ECoupon.findByOrder(order);
        assertNotNull(ecoupons);
        assertEquals(2, ecoupons.size());

        assertEquals(singleGoods.id, ecoupons.get(0).goods.id);
        assertEquals(ecoupons.get(0).goods.id, ecoupons.get(1).goods.id);
        assertNotSame(ecoupons.get(0).eCouponSn, ecoupons.get(1).eCouponSn);
        // replyCode应当相同
        assertEquals(ecoupons.get(0).replyCode, ecoupons.get(1).replyCode);
    }

    @Test
    public void 不同订单购买未设置组代码的同一商品生成的ECoupon会产生相同的ReplyCode() throws Exception {
        Order order1 = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order1.save();
        generateECoupon(order1, singleGoods, user, 1);
        List<ECoupon> ecoupons1 = ECoupon.findByOrder(order1);
        assertNotNull(ecoupons1);
        assertEquals(1, ecoupons1.size());

        Order order2 = Order.createConsumeOrder(user.id, AccountType.CONSUMER);
        order2.save();
        generateECoupon(order2, singleGoods, user, 1);
        List<ECoupon> ecoupons2 = ECoupon.findByOrder(order2);
        assertNotNull(ecoupons2);
        assertEquals(1, ecoupons2.size());

        assertEquals(singleGoods.id, ecoupons1.get(0).goods.id);
        assertEquals(singleGoods.id, ecoupons2.get(0).goods.id);
        assertEquals(ecoupons1.get(0).goods.id, ecoupons2.get(0).goods.id);
        assertNotSame(ecoupons1.get(0).eCouponSn, ecoupons2.get(0).eCouponSn);
        // replyCode应当相同
        assertEquals(ecoupons1.get(0).replyCode, ecoupons2.get(0).replyCode);
    }

    /**
     * 生成ECoupon.
     * 
     * @param order
     * @param goods
     * @param consumer
     * @return
     * @throws Exception
     */
    private void generateECoupon(Order order, Goods goods, User consumer,
                    int number) throws Exception {
        OrderItems item = order.addOrderItem(goods, number, consumer.mobile,
                        groupedGoods1.salePrice, // 最终成交价
                        groupedGoods1.getResalerPriceOfUhuila()// 一百券作为分销商的成本价
                        );
        item.save();

        for (int i = 0; i < number; i++) {
            ECoupon eCoupon = new ECoupon(order, goods, item).save();
            eCoupon.save();
            assertEquals(order.id, eCoupon.order.id);
        }
    }

}
