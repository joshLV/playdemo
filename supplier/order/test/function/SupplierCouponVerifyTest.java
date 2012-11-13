package function;

import java.util.List;

import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;

/**
 * 门店验证测试
 * @author tanglq
 */
public class SupplierCouponVerifyTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    Goods goods;
    Order order;
    OrderItems orderItem;
    Category category;
    ECoupon coupon;
    SupplierUser supplierUser;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);
        coupon = FactoryBoy.create(ECoupon.class);
        
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void 门店店号验证页面只出现一个Shop() throws Exception {
        Response response = GET("/coupons/verify");
        assertIsOk(response);
        assertNull(renderArgs("shopList"));
        Shop myShop = (Shop)renderArgs("shop");
        assertEquals(shop.id, myShop.id);
    }

    @Test
    public void 超级用户验证页面出现ShopList() throws Exception {
        supplierUser.shop = null;
        supplierUser.save();
        Response response = GET("/coupons/verify");
        assertIsOk(response);
        assertNull(renderArgs("shop"));
        List<Shop> myShops = (List<Shop>)renderArgs("shopList");
        assertEquals(1, myShops.size());
    }

    @Test
    public void 商户没有录入门店时不能使用验证() throws Exception {
        // 使此商户无门店.
        supplierUser.shop = null;
        supplierUser.save();
        goods.shops.clear();
        goods.save();
        shop.delete();
        
        // 测试.
        Response response = GET("/coupons/verify");
        assertStatus(500, response);
    }
    
    
}
