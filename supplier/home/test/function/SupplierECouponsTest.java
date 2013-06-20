package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.util.Date;

/**
 * 商户后台v2的券列表功能测试.
 * <p/>
 * User: sujie
 * Date: 12/27/12
 * Time: 2:54 PM
 */
public class SupplierECouponsTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    Goods goods;
    Order order;
    Category category;
    ECoupon coupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
        coupon = FactoryBoy.create(ECoupon.class);
        coupon.status = ECouponStatus.CONSUMED;
        coupon.shop = shop;
        coupon.consumedAt= new Date();
        coupon.order.paidAt= DateHelper.beforeDays(1);
        coupon.order.save();
        coupon.save();
    }

    @Test
    public void indexTest() {
        Http.Response response = GET("/coupons");
        assertStatus(200, response);
        assertContentMatch(goods.shortName, response);
        assertNotNull(renderArgs("couponPage"));
        JPAExtPaginator<ECoupon> couponList = (JPAExtPaginator<ECoupon>) renderArgs("couponPage");
        assertEquals(1, couponList.size());
        assertEquals(goods.id, couponList.get(0).goods.id);
    }

    @Test
    public void couponExcelOutTest() {
        Http.Response response = GET("/coupon-excel-out");
        assertIsOk(response);
        JPAExtPaginator<ECoupon> couponList = (JPAExtPaginator<ECoupon>) renderArgs("couponPage");
        assertNotNull(couponList);
        assertEquals(1, couponList.size());
        assertEquals(goods.id, couponList.get(0).goods.id);
    }
}
    