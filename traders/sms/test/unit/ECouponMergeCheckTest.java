package unit;

import java.math.BigDecimal;
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
        
    }
    
    private ECoupon createEcoupon() {

        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        Goods goods = Goods.findById(goodsId);       

        Long orderId = (Long) Fixtures.idCache.get("");
        Order order = Order.findById(orderId);


        Long orderItemId = (Long) Fixtures.idCache.get("");
        OrderItems orderItem = OrderItems.findById(orderId);
        
        ECoupon ecoupon = new ECoupon(order, goods, orderItem);
        return null;
    }
}
