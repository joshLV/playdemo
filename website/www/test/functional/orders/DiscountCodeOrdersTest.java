package functional.orders;

import java.math.BigDecimal;
import java.util.Date;

import models.consumer.User;
import models.consumer.UserInfo;
import models.order.DiscountCode;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http;
import play.test.FunctionalTest;
import util.DateHelper;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * User: wangjia
 * Date: 12-11-15
 * Time: 下午2:01
 */
public class DiscountCodeOrdersTest extends FunctionalTest {
    Goods goods1;
    Goods goods2;
    DiscountCode discountCode;
    User user;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goods1 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.salePrice = BigDecimal.valueOf(8.5);
            }
        });
        goods2 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.salePrice = BigDecimal.TEN;
            }
        });

    }

    @Test
    public void testInvalidDiscountCode() {
        Http.Response response = GET("/orders?g" + goods1.id + "=1&gid=" + goods1.id + "&discountSN=123&mobile=" + user.mobile);
        assertStatus(200, response);
        assertContentMatch((String) renderArgs("discountErrorInfo"), response);
        assertEquals(new BigDecimal("8.50"), ((BigDecimal) renderArgs("needPay")).setScale(2));
        assertEquals(new BigDecimal("8.50"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }

    @Test
    public void testWholeBillDiscountAmount() {
        discountCode = FactoryBoy.create(DiscountCode.class, new BuildCallback<DiscountCode>() {
            @Override
            public void build(DiscountCode discountCode) {
                discountCode.goods = null;
                discountCode.discountAmount = BigDecimal.valueOf(2);
                discountCode.discountPercent = null;
            }
        });
        //27
        Http.Response response = GET("/orders?g" + goods1.id + "=2&gid=" + goods1.id + "&gid=" + goods2.id + "&g" + goods2.id + "=1" + "&discountSN=" + discountCode.discountSn + "&mobile=" + user.mobile);
        assertStatus(200, response);
        //25
        assertEquals(new BigDecimal("25.00"), ((BigDecimal) renderArgs("needPay")).setScale(2));
        assertEquals(new BigDecimal("27.00"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }

    @Test
    public void testWholeBillDiscountPercent() {
        discountCode = FactoryBoy.create(DiscountCode.class, new BuildCallback<DiscountCode>() {
            @Override
            public void build(DiscountCode discountCode) {
                discountCode.goods = null;
                discountCode.discountAmount = null;
                discountCode.discountPercent = BigDecimal.valueOf(0.02);
            }
        });
        //27
        Http.Response response = GET("/orders?g" + goods1.id + "=2&gid=" + goods1.id + "&gid=" + goods2.id + "&g" + goods2.id + "=1" + "&discountSN=" + discountCode.discountSn + "&mobile=" + user.mobile);
        assertStatus(200, response);
        //26.46
        assertEquals(new BigDecimal("26.46"), ((BigDecimal) renderArgs("needPay")).setScale(2));
        assertEquals(new BigDecimal("27.00"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }

    @Test
    public void testSingleGoodsDiscountAmount() {
        discountCode = FactoryBoy.create(DiscountCode.class, new BuildCallback<DiscountCode>() {
            @Override
            public void build(DiscountCode discountCode) {
                discountCode.goods = goods1;
                discountCode.discountAmount = BigDecimal.valueOf(2);
                discountCode.discountPercent = null;
            }
        });
        //27
        Http.Response response = GET("/orders?g" + goods1.id + "=2&gid=" + goods1.id + "&gid=" + goods2.id + "&g" + goods2.id + "=1" + "&discountSN=" + discountCode.discountSn + "&mobile=" + user.mobile);
        assertStatus(200, response);
        //23
        assertEquals(new BigDecimal("23.00"), ((BigDecimal) renderArgs("needPay")).setScale(2));
        assertEquals(new BigDecimal("23.00"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }

    @Test
    public void testSingleBillDiscountPercent() {
        discountCode = FactoryBoy.create(DiscountCode.class, new BuildCallback<DiscountCode>() {
            @Override
            public void build(DiscountCode discountCode) {
                discountCode.goods = goods1;
                discountCode.discountAmount = null;
                discountCode.discountPercent = BigDecimal.valueOf(0.02);
            }
        });
        //27
        Http.Response response = GET("/orders?g" + goods1.id + "=2&gid=" + goods1.id + "&gid=" + goods2.id + "&g" + goods2.id + "=1" + "&discountSN=" + discountCode.discountSn + "&mobile=" + user.mobile);
        assertStatus(200, response);
        //26.66
        assertEquals(new BigDecimal("26.66"), ((BigDecimal) renderArgs("needPay")).setScale(2));
        assertEquals(new BigDecimal("26.66"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }

    @Test
    public void testExpiredDiscountCode() {
        discountCode = FactoryBoy.create(DiscountCode.class, new BuildCallback<DiscountCode>() {
            @Override
            public void build(DiscountCode discountCode) {
                discountCode.beginAt = DateHelper.beforeDays(new Date(), 10);
                discountCode.endAt = DateHelper.beforeDays(new Date(), 5);
                discountCode.goods = goods1;
                discountCode.discountAmount = null;
                discountCode.discountPercent = BigDecimal.valueOf(0.02);
            }
        });
        Http.Response response = GET("/orders?g" + goods1.id + "=2&gid=" + goods1.id + "&gid=" + goods2.id + "&g" + goods2.id + "=1" + "&discountSN=" + discountCode.discountSn + "&mobile=" + user.mobile);
        assertStatus(200, response);
        assertNull(renderArgs("discountCode"));
        assertEquals(new BigDecimal("27.00"), ((BigDecimal) renderArgs("needPay")).setScale(2));
        assertEquals(new BigDecimal("27.00"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }
}


