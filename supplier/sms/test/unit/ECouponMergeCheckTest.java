package unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import models.accounts.Account;
import models.accounts.util.AccountUtil;
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

import org.junit.Ignore;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class ECouponMergeCheckTest extends UnitTest {
    @org.junit.Before
    public void setup() {
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(SupplierRole.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(SupplierUser.class);
        Fixtures.loadModels("fixture/roles.yml", "fixture/shop.yml",
                "fixture/supplierusers.yml", "fixture/goods_base.yml",
                "fixture/user.yml", "fixture/accounts.yml",
                "fixture/goods.yml",
                "fixture/orders.yml",
                "fixture/orderItems.yml");


        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");

        Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc2");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");
        goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc3");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("99999");
        account.save();

    }

    @Test
    public void testMergeCheckLess() {
    }
    
    @Test
    public void testSelectCheckEcoupons() {
        List<ECoupon> ecoupons = createEcoupons("1234", new String[]{"100", "100", "100", "50", "20", "20"});
        assertArrayEquals(new String[]{"100", "100", "100", "50", "20", "20"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("500"), ecoupons)));
        assertArrayEquals(new String[]{"100", "100", "20"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("230"), ecoupons)));
        assertArrayEquals(new String[]{"100", "100", "50"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("260"), ecoupons)));
        assertArrayEquals(new String[]{"100"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("112"), ecoupons)));
        assertArrayEquals(new String[]{"50", "20"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("80"), ecoupons)));
        assertArrayEquals(new String[]{"50"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("60"), ecoupons)));
        assertArrayEquals(new String[]{}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("3"), ecoupons)));
    }
    
    private List<ECoupon> createEcoupons(String replyCode, String[] prices) {
        List<ECoupon> ecoupons = new ArrayList<>();
        for (int i = 0; i < prices.length; i++) {
            ECoupon ecoupon = createEcoupon(replyCode, prices[i]);
            ecoupons.add(ecoupon);
        }
        return ecoupons;
    }
    
    private ECoupon createEcoupon(String replyCode, String price) {

        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        Goods goods = Goods.findById(goodsId);       
        Long orderId = (Long) Fixtures.idCache.get("models.order.Order-order1");
        Order order = Order.findById(orderId);
        Long orderItemId = (Long) Fixtures.idCache.get("models.order.OrderItems-orderItems2");
        OrderItems orderItem = OrderItems.findById(orderItemId);
        
        ECoupon ecoupon = new ECoupon(order, goods, orderItem);
        ecoupon.faceValue = new BigDecimal(price); // 方便测试所以用String
        ecoupon.replyCode = replyCode;
        return ecoupon;
    }
    
    private String[] toPriceStringArray(List<ECoupon> ecoupons) {
        String[] results = new String[ecoupons.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = ecoupons.get(i).faceValue.toString();
        }
        return results;
    }
    
}
