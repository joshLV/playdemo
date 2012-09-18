package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.order.ECoupon;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-20
 * Time: 下午4:12
 * To change this template use File | Settings | File Templates.
 */
public class OperateVerifyCouponsFuncTest extends FunctionalTest {

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
    }

    @Test
    public void testIndex() {

        Http.Response response = GET("/coupons/index");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertNotNull(renderArgs("shopList"));
        assertNotNull(renderArgs("supplierList"));

    }

    @Test
    public void testVerify() {

        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class, "SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = goods.supplierId;
            }
        });
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, "Id", new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.shop = shop;
                target.goods = goods;
                target.expireAt = goods.expireAt;
            }
        });
//        System.out.println(eCoupon.status);
//        System.out.println(eCoupon.eCouponSn);
//        System.out.println(eCoupon.expireAt);
        Http.Response response = GET("/coupons/verify?supplierId=" + eCoupon.shop.supplierId + "&shopId=" + eCoupon.shop.id + "&eCouponSn=" + eCoupon.eCouponSn);
        assertIsOk(response);
        assertContentMatch("未消费", response);

    }

    @Test
    public void testUpdate() {
        // 生产 电子券 测试数据
        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class, "SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = goods.supplierId;
            }
        });
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, "Id", new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.shop = shop;
                target.goods = goods;
                target.expireAt = goods.expireAt;
                target.operateUserId = 2L;
                target.originalPrice = new BigDecimal(100);
                target.salePrice = new BigDecimal(100);
                target.faceValue = new BigDecimal(150);
            }
        });
        // 设置 平台付款账户 金额，已完成向商户付款
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal(1000);
        account.save();

        // 将URL 参数放入Map 中
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("supplierId", goods.supplierId.toString());
        params.put("eCouponSn", eCoupon.eCouponSn.toString());
        params.put("shopName", shop.name);

        // 检测测试结果
        Http.Response response = POST("/coupons/update", params);
        assertIsOk(response);
        assertContentMatch("0", response);
//        ECoupon eCouponConsumed = ECoupon.findById(eCoupon.id);
//        assertEquals(ECouponStatus.CONSUMED,eCouponConsumed.status);
    }
}
