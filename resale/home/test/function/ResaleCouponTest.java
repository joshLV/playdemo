package function;

import static org.junit.Assert.*;

import java.security.Security;

import models.accounts.Account;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import controllers.modules.resale.cas.SecureCAS;

import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import factory.FactoryBoy;

public class ResaleCouponTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    Goods goods;
    Order order;
    OrderItems orderItem;
    Category category;
    ECoupon coupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        coupon = FactoryBoy.create(ECoupon.class);
        shop = FactoryBoy.last(Shop.class);
        goods = FactoryBoy.last(Goods.class);
        FactoryBoy.create(Account.class, "balanceAccount");

        Security.class;
    }
    
    @Test
    public void testCoupons() throws Exception {
        
    }

}
