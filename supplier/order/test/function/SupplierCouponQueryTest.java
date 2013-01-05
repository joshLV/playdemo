package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
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
import util.DateHelper;

/**
 * 门店验证测试
 *
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
        supplierUser.shop = shop;
        supplierUser.save();

        shop.supplierId = supplierUser.supplier.id;
        shop.save();
        goods.supplierId = supplierUser.supplier.id;
        goods.save();

        coupon = FactoryBoy.create(ECoupon.class);
        coupon.shop = shop;
        coupon.goods = goods;
        coupon.save();
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
    }

    @Test
    public void 正常券() {
        String url = "/coupons/single-query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn;
        Http.Response response = GET(url);
        assertStatus(200, response);
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("该券未消费！", response);
        assertContentMatch("券编号：" + coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void 在当当已经退款的券进行消费() {
        String url = "/coupons/single-query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn;
        Http.Response response = GET(url);
        assertStatus(200, response);
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("该券未消费！", response);
        assertContentMatch("券编号：" + coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void 已消费的券() {
        coupon.status = ECouponStatus.CONSUMED;
        coupon.save();
        String url = "/coupons/single-query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn;
        Http.Response response = GET(url);
        assertStatus(200, response);
        System.out.println("getContent(response):" + getContent(response));
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("对不起，该券已使用过", response);
        assertContentMatch(coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void 已过期的券() {
        coupon.expireAt = DateHelper.beforeDays(2);
        coupon.save();
        String url = "/coupons/single-query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn;
        Http.Response response = GET(url);
        assertStatus(200, response);
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("对不起，该券已过期", response);
        assertContentMatch(coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void 已冻结的券() {
        coupon.refresh();
        coupon.isFreeze = 1;
        coupon.save();
        String url = "/coupons/single-query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn;
        Http.Response response = GET(url);
        assertStatus(200, response);
        assertNotNull(renderArgs("ecoupon"));
        assertContentMatch("对不起，该券已被冻结", response);
        assertContentMatch(coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void 非法参数() {
        Http.Response response = GET("/coupons/single-query?shopId=" + shop.id + "&eCouponSn=11aa");
        assertStatus(200, response);
        assertNull(renderArgs("ecoupon"));
    }
}
