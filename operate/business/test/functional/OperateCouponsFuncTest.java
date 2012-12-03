package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.sales.Goods;
import models.sales.Shop;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-22
 * Time: 上午10:29
 * To change this template use File | Settings | File Templates.
 */
public class OperateCouponsFuncTest extends FunctionalTest {
    OperateUser user;
    Order order;

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        // 初始化 电子券数据
        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order order) {
                order.paidAt = DateHelper.beforeHours(1);
            }
        });
        FactoryBoy.batchCreate(10, ECoupon.class, "Id",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.shop = shop;
                        target.goods = goods;
                        target.eCouponSn = "8888000" + seq;
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                    }
                });


    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/coupons");
        assertIsOk(response);
        assertContentMatch("券号列表", response);
        assertEquals(10, ((JPAExtPaginator<ECoupon>) renderArgs("couponPage")).size());
    }

    @Ignore
    @Test
    public void testIndexWithCondition() {
        String condition = "?condition.status=UNCONSUMED";
        Http.Response response = GET("/coupons" + condition);
        assertIsOk(response);
        assertNotNull(renderArgs("couponPage"));
        boolean hasRight = (Boolean) renderArgs("hasRight");
        assertTrue(hasRight);
    }

    @Ignore
    @Test
    public void testIndexWithoutRight() {
        String condition = "?condition.status=UNCONSUMED";
        user.roles.remove(role("manager"));
        user.save();
        Http.Response response = GET("/coupons" + condition);
        assertIsOk(response);
        boolean hasRight = (Boolean) renderArgs("hasRight");
        assertFalse(hasRight);
    }

    private static OperateRole role(String roleName) {
        OperateRole role = OperateRole.find("byKey", roleName).first();
        return role;
    }

    @Ignore
    @Test
    public void testFreeze() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.isFreeze = 0;
        eCoupon.save();
        Http.Response response = PUT("/coupons/" + eCoupon.id.toString() + "/freeze", "text/html", "");
        assertStatus(302, response);
        eCoupon.refresh();
        assertEquals(1, eCoupon.isFreeze);
        assertEquals(1, CouponHistory.count());
        CouponHistory historyList = CouponHistory.find("coupon=? order by createdAt desc", eCoupon).first();
        assertEquals("冻结券号", historyList.remark);
    }

    @Ignore
    @Test
    public void testUnFreeze() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.isFreeze = 1;
        eCoupon.save();
        Http.Response response = PUT("/coupons/" + eCoupon.id.toString() + "/unfreeze", "text/html", "");
        assertStatus(302, response);
        eCoupon.refresh();
        assertEquals(0, eCoupon.isFreeze);
        assertEquals(1, CouponHistory.count());

        CouponHistory historyList = CouponHistory.find("coupon=? order by createdAt desc", eCoupon).first();
        assertEquals("解冻券号", historyList.remark);
    }

    @Ignore
    @Test
    public void testSendMessage() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.save();
        Http.Response response = GET("/coupons-message/" + eCoupon.id.toString() + "/send");
        assertIsOk(response);
        assertEquals(1, CouponHistory.count());
        CouponHistory historyList = CouponHistory.find("coupon=? order by createdAt desc", eCoupon).first();
        assertEquals("重发短信", historyList.remark);

    }

    @Ignore
    @Test
    public void testCouponHistory() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        CouponHistory history = FactoryBoy.create(CouponHistory.class);
        history.coupon = eCoupon;
        history.save();
        Http.Response response = GET("/coupon_history?couponSn=" + eCoupon.eCouponSn);
        assertIsOk(response);
        assertNotNull(renderArgs("couponList"));
        List<CouponHistory> historyList = (List) renderArgs("couponList");
        assertEquals("产生券号", historyList.get(0).remark);
    }

    @Ignore
    @Test
    public void testExcelOut() {
        Http.Response response = GET("/coupon_excel");
        assertIsOk(response);
        assertNotNull(renderArgs("couponsList"));
    }
}
