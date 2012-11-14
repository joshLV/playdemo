package function;

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

import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;

/**
 * 门店验证测试
 * @author tanglq
 */
public class SupplierCouponQueryTest extends FunctionalTest {
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
    public void 正常券() {
        System.out.println("ecoupon.su=" + coupon.goods.supplierId + ", sn=" + coupon.eCouponSn);
        String url = "/coupons/query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn;
        System.out.println("url=" + url);
        Http.Response response = GET(url);
        assertStatus(200, response);
        System.out.println("result:" + getContent(response));
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("券状态:未消费", response);
        assertContentMatch("券编号: " + coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void 非法参数() {
        Http.Response response = GET("/coupons/query?shopId=" + shop.id + "&eCouponSn=11aa");
        assertStatus(200, response);
        assertNull(renderArgs("ecoupon"));
    }
}
