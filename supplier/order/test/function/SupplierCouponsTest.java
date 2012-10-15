package function;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.admin.SupplierUser;

import models.order.ECoupon;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Category;
import models.sales.ConsultRecord;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.MockSMSProvider;
import models.sms.SMSMessage;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);
        coupon = FactoryBoy.create(ECoupon.class);
        shop = FactoryBoy.last(Shop.class);
        goods = FactoryBoy.last(Goods.class);
        Account account = FactoryBoy.create(Account.class, "balanceAccount");
    }

    protected static void assertSMSContentLength(String content) {
        assertTrue("短信内容(" + content + ")超过67字符, size:" + content.length(),
                content.length() <= 67);
    }

    /**
     * 使用正则匹配结果.
     *
     * @param pattern
     * @param content
     */
    public static void assertSMSContentMatch(String pattern, String content) {
        assertSMSContentLength(content);
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(content).find();
        assertTrue("The content (" + content + ") does not match '" + pattern
                + "'", ok);
    }

    @Test
    public void indexTest() {
        Http.Response response = GET("/coupons");
        assertStatus(200, response);
        assertContentMatch(goods.name, response);
        assertNotNull(renderArgs("couponPage"));
        JPAExtPaginator<ECoupon> couponList = (JPAExtPaginator<ECoupon>) renderArgs("couponPage");
        assertEquals(1, couponList.size());
        assertEquals(shop.name, couponList.get(0).shop.name);
    }

    @Test
    public void verifyTest() {
        Http.Response response = GET("/coupons/verify");
        assertStatus(200, response);
        assertContentMatch("商户验证消费券", response);
        assertNotNull(renderArgs("shop"));
        Shop getShop = (Shop) renderArgs("shop");
        assertEquals(shop.name, getShop.name);
    }

    @Test
    public void queryTest() {
        Http.Response response = GET("/coupons/query?shopId=" + shop.id + "&eCouponSn=" + coupon.eCouponSn);
        assertStatus(200, response);
        assertContentMatch("券状态:未消费", response);
        assertContentMatch("券编号: " + coupon.eCouponSn, response);
        ECoupon getCoupon = (ECoupon) renderArgs("ecoupon");
        assertEquals(coupon.eCouponSn, getCoupon.eCouponSn);
    }

    @Test
    public void updateTest() {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", coupon.eCouponSn);
        params.put("shopName", shop.name);
        Http.Response response = POST("/coupons/update", params);
        //renderJSON("0");
        assertContentMatch("0", response);
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【一百券】您尾号" + coupon.getLastCode(4)
                + "的券号于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：400-6262-166",
                msg.getContent());

    }

    @Test
    public void couponExcelOutTest() {
        Http.Response response = GET("/coupon-excel-out");
        JPAExtPaginator<ECoupon> couponList = (JPAExtPaginator<ECoupon>) renderArgs("couponPage");
        assertNotNull(couponList);
        assertEquals(1, couponList.size());
        assertEquals(shop.name, couponList.get(0).shop.name);

    }


}
