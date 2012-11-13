package function;

import models.accounts.Account;
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

import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;

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
        supplierUser = FactoryBoy.create(SupplierUser.class);
        coupon = FactoryBoy.create(ECoupon.class);
        shop = FactoryBoy.last(Shop.class);
        goods = FactoryBoy.last(Goods.class);
        
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    
}
