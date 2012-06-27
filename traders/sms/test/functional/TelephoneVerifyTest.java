package functional;

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
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * @author likang
 */
public class TelephoneVerifyTest extends FunctionalTest{
    @Before
    public void setup(){
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
    }

    @Test
    public void testVerify(){
        assertTrue(1==1);
    }
}
