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
public class SupplierCouponQueryMultiECouponTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    // 订单1同组券
    Order order1;
    Goods goods100, goods50;
    Goods singleGoods;

    Category category;
    ECoupon coupon1, coupon2, singleCoupon1;
    SupplierUser supplierUser;
    
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);

        goods50 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("50");
                g.groupCode = "GROUP1";
            }
        });
        goods100 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("100");
                g.groupCode = "GROUP1";
            }
            
        });
        singleGoods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("75");
            }
        });
        
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    private void generateOrder2With2Group3Single() {
        order1 = FactoryBoy.create(Order.class);
        
        FactoryBoy.create(OrderItems.class);
        coupon1 = createCoupon(goods100);
        coupon2 = createCoupon(goods100);
        
        FactoryBoy.create(OrderItems.class);
        singleCoupon1 = createCoupon(singleGoods);
        createCoupon(singleGoods);
        createCoupon(singleGoods);
    }

    protected void generateOrder1WithSameGroupGoods() {
        order1 = FactoryBoy.create(Order.class);
        
        FactoryBoy.create(OrderItems.class);
        coupon1 = createCoupon(goods100);
        coupon2 = createCoupon(goods100);
        
        FactoryBoy.create(OrderItems.class);
        createCoupon(goods50);
        createCoupon(goods50);
        createCoupon(goods50);
    }
    
    public ECoupon createCoupon(final Goods goods) {
        return FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.goods = goods;
                e.salePrice = goods.salePrice;
                e.faceValue = goods.faceValue;
                e.originalPrice = goods.originalPrice;
            }
        });
    }

    @Test
    public void 有多张券是输入任一张券会返回提示350元券() {
        // 订单1: 2个同组商品
        generateOrder1WithSameGroupGoods();
        
        Http.Response response = GET("/coupons/multi-query?shopId=" + shop.id + "&eCouponSn=" + coupon2.eCouponSn);
        assertStatus(200, response);
        
        assertNotNull(renderArgs("ecoupon"));        
        assertContentMatch("券状态:未消费", response);
        assertContentMatch("券编号: " + coupon2.eCouponSn, response);

        List<ECoupon> ecoupons = (List<ECoupon>) renderArgs("ecoupons");
        assertEquals(5, ecoupons.size());
        BigDecimal amount = (BigDecimal) renderArgs("verifyAmount");
        assertNotNull(amount);
        assertEquals(new BigDecimal("350").setScale(2), amount.setScale(2));
    }
    
    @Test
    public void 输入同一订单中无组商品券只返回提示225元券() throws Exception {
        // 订单2：2个同组商品，3个无组商品
        generateOrder2With2Group3Single();
                
        Http.Response response = GET("/coupons/multi-query?shopId=" + shop.id + "&eCouponSn=" + singleCoupon1.eCouponSn);
        assertStatus(200, response);
        
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("券状态:未消费", response);
        assertContentMatch("券编号: " + singleCoupon1.eCouponSn, response);

        List<ECoupon> ecoupons = (List<ECoupon>) renderArgs("ecoupons");
        assertEquals(3, ecoupons.size());
        BigDecimal amount = (BigDecimal) renderArgs("verifyAmount");
        assertNotNull(amount);
        assertEquals(new BigDecimal("225").setScale(2), amount.setScale(2));
    }

}
