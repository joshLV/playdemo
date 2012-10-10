package function;

import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;

import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Category;
import models.sales.ConsultRecord;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-9
 * Time: 下午6:46
 * To change this template use File | Settings | File Templates.
 */
public class SupplierCouponsTest extends FunctionalTest {
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

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        supplier = FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
        category = FactoryBoy.create(Category.class);
        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class);
        orderItem = FactoryBoy.create(OrderItems.class);
        coupon = FactoryBoy.create(ECoupon.class);
    }


    @Test
    public void indexTest() {
        Http.Response response = GET("/coupons");
        System.out.println(">>>>>>>" + getContent(response));
        assertStatus(200, response);
    }

    @Ignore
    @Test
    public void verifyTest() {
        Http.Response response = GET("/coupons/verify");
//        assertStatus(200, response);
    }

    @Ignore
    @Test
    public void queryTest() {
        Http.Response response = GET("/coupons/query");

    }

    @Ignore
    @Test
    public void updateTest() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", coupon.eCouponSn);
        params.put("shopName", shop.name);
        Http.Response response = POST("/coupons/update", params);
    }

    @Ignore
    @Test
    public void couponExcelOutTest() {
        Http.Response response = GET("/coupon-excel-out");
        JPAExtPaginator<ECoupon> couponList = (JPAExtPaginator<ECoupon>) renderArgs("couponList");
//        assertNotNull(couponList);

    }


}
