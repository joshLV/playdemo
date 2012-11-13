package function;

import models.accounts.Account;
import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class ResaleCouponTest extends FunctionalTest {
    Shop shop;
    Goods goods;
    ECoupon coupon;
    Resaler resaler;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        
        resaler = FactoryBoy.create(Resaler.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);

        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userType = AccountType.RESALER;
                o.userId = resaler.id;
            }
        });
        
        coupon = FactoryBoy.create(ECoupon.class);
        
        Security.setLoginUserForTest(resaler.loginName);
    }
    
    @Test
    public void testCoupons() throws Exception {
        Response response = GET("/ecoupons");
        assertIsOk(response);
        JPAExtPaginator<ECoupon> couponsList = (JPAExtPaginator<ECoupon>)renderArgs("couponsList");
        assertNotNull(couponsList);
        assertEquals(1, couponsList.getRowCount());
    }

}
