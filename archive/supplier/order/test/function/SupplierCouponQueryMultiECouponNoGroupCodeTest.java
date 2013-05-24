package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店验证测试
 * @author tanglq
 */
public class SupplierCouponQueryMultiECouponNoGroupCodeTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    Goods goods100, goods50;
    Order order;
    OrderItems orderItem;
    Category category;
    ECoupon coupon1, coupon2, coupon3, coupon4, coupon5;
    SupplierUser supplierUser;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        goods100 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("100");
            }
            
        });
        supplierUser = FactoryBoy.create(SupplierUser.class);
        coupon1 = FactoryBoy.create(ECoupon.class);
        coupon2 = FactoryBoy.create(ECoupon.class);
        goods50 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("50");
            }
            
        });
        coupon3 = FactoryBoy.create(ECoupon.class);
        coupon4 = FactoryBoy.create(ECoupon.class);
        coupon5 = FactoryBoy.create(ECoupon.class);
        
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void 有多张券时输入任一张券会返回提示200元券() {
        Http.Response response = GET("/coupons/multi-query?shopId=" + shop.id + "&eCouponSn=" + coupon2.eCouponSn);
        assertStatus(200, response);
        assertContentMatch("券状态:未消费", response);
        assertContentMatch("券编号: " + coupon2.eCouponSn, response);
        List<ECoupon> ecoupons = (List<ECoupon>) renderArgs("ecoupons");
        assertEquals(2, ecoupons.size());
        BigDecimal amount = (BigDecimal) renderArgs("verifyAmount");
        assertNotNull(amount);
        assertEquals(new BigDecimal("200").setScale(2), amount.setScale(2));
    }

}
