package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-22
 * Time: 上午10:29
 * To change this template use File | Settings | File Templates.
 */
public class OperateCouponsFuncTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.delete(Goods.class);
        FactoryBoy.delete(Shop.class);
        FactoryBoy.delete(ECoupon.class);
        FactoryBoy.delete(Order.class);
        FactoryBoy.delete(OrderItems.class);
        FactoryBoy.delete(OperateUser.class);
        FactoryBoy.delete(OperateRole.class);
        FactoryBoy.delete(Brand.class);
        FactoryBoy.delete(Category.class);

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        // 初始化 电子券数据
        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class);
        FactoryBoy.batchCreate(10, ECoupon.class, "Id",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.shop = shop;
                        target.goods = goods;
                        target.eCouponSn = "8888000" + seq;
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;

                    }
                });
    }

    @Test
    public void testIndex() {
        System.out.println("ECoupon size -------------- " + ECoupon.findAll().size());
        Http.Response response = GET("/coupons");
        assertIsOk(response);
        assertContentMatch("券号列表", response);
    }

    @Test
    public void testIndexWithCondition() {
        String condition = "?condition.status=UNCONSUMED";

        Http.Response response = GET("/coupons" + condition);
        assertIsOk(response);
        assertNotNull(renderArgs("couponPage"));
    }

    @Test
    public void testFreeze() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.isFreeze = 0;
        eCoupon.save();
        Http.Response response = PUT("/coupons/" + eCoupon.id.toString() + "/freeze", "text/html", "");
        assertStatus(302, response);
        eCoupon.refresh();
        assertEquals(1, eCoupon.isFreeze);
    }

    @Test
    public void testUnFreeze() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.isFreeze = 1;
        eCoupon.save();
        Http.Response response = PUT("/coupons/" + eCoupon.id.toString() + "/unfreeze", "text/html", "");
        assertStatus(302, response);
        eCoupon.refresh();
        assertEquals(0, eCoupon.isFreeze);
    }

    @Test
    public void testSendMessage() {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class);
        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.save();
        Http.Response response = GET("/coupons-message/" + eCoupon.id.toString() + "/send");
        assertIsOk(response);
    }

    @Test
    public void testExcelOut() {

        Http.Response response = GET("/coupon_excel");
        assertIsOk(response);
        assertNotNull(renderArgs("couponsList"));

    }
}
