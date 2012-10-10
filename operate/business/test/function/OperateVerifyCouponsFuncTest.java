package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.OperateUser;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.*;
import models.sales.Goods;
import models.sales.MaterialType;
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
    PromoteRebate promoteRebate;
    User promoteUser;
    User inviteUser;

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        FactoryBoy.delete(PromoteRebate.class);
        FactoryBoy.create(UserInfo.class);
        promoteUser = FactoryBoy.create(User.class);
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
        ECoupon coupon = (ECoupon) (renderArgs("ecoupon"));
        assertEquals(ECouponStatus.UNCONSUMED, coupon.status);

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
        goods.shops.add(shop);
        goods.save();
        // 将URL 参数放入Map 中
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("supplierId", goods.supplierId.toString());
        params.put("eCouponSn", eCoupon.eCouponSn.toString());
        params.put("shopName", shop.name);

        // 检测测试结果
        Http.Response response = POST("/coupons/update", params);
        assertIsOk(response);
        eCoupon.refresh();
        ECoupon eCouponConsumed = ECoupon.findById(eCoupon.id);
        assertEquals(ECouponStatus.CONSUMED, eCouponConsumed.status);
    }

    @Test
    public void test_消费验证后给推荐人返利() {
        final Goods goods = FactoryBoy.create(Goods.class);

        inviteUser = FactoryBoy.create(User.class, "loginName", new BuildCallback<User>() {
            @Override
            public void build(User target) {
                target.promoterCode = "123";
                target.promoteUserId = promoteUser.id;
            }
        });

        final Shop shop = FactoryBoy.create(Shop.class, "SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = goods.supplierId;
            }
        });

        goods.materialType = MaterialType.ELECTRONIC;
        goods.salePrice = new BigDecimal(30);
        goods.shops.add(shop);
        goods.save();
        final Order order = FactoryBoy.create(Order.class);
        order.promoteUserId = promoteUser.id;
        order.save();

        promoteRebate = FactoryBoy.create(PromoteRebate.class, "UN_CONSUMED", new BuildCallback<PromoteRebate>() {
            @Override
            public void build(PromoteRebate target) {
                target.registerFlag = true;
                target.invitedUser = inviteUser;
                target.promoteUser = promoteUser;
                target.partAmount = BigDecimal.ZERO;
                target.order = order;
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
                target.salePrice = new BigDecimal(30);
                target.status = ECouponStatus.UNCONSUMED;
                target.rebateValue = new BigDecimal("3");
                target.order = order;
                target.promoterRebateValue = new BigDecimal(6);
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
        assertStatus(200, response);
        eCoupon.refresh();
        promoteRebate.refresh();
        ECoupon eCouponConsumed = ECoupon.findById(eCoupon.id);
        assertEquals(ECouponStatus.CONSUMED, eCouponConsumed.status);
        assertEquals(new BigDecimal("2.50"), promoteRebate.rebateAmount);
        assertEquals(eCouponConsumed.promoterRebateValue, promoteRebate.partAmount);
        assertEquals(RebateStatus.ALREADY_REBATE, promoteRebate.status);
    }

}
