package functional;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
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
import util.DateHelper;
import util.mq.MockMQ;
import play.data.validation.Error;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营后台券验证功能测试.
 * <p/>
 * User: hejun
 * Date: 12-8-20
 * Time: 下午4:12
 */
public class OperateVerifyCouponsFuncTest extends FunctionalTest {
    PromoteRebate promoteRebate;
    User promoteUser;
    User inviteUser;
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockMQ.clear();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        FactoryBoy.delete(PromoteRebate.class);
        FactoryBoy.create(UserInfo.class);
        promoteUser = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
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
        Http.Response response = GET("/coupons/verify?supplierId=" + eCoupon.shop.supplierId + "&shopId=" + eCoupon.shop.id + "&eCouponSn=" + eCoupon.eCouponSn);
        assertIsOk(response);
        ECoupon coupon = (ECoupon) (renderArgs("ecoupon"));
        assertEquals(ECouponStatus.UNCONSUMED, coupon.status);

    }

    @Test
    public void testUpdate() {
        // 生产 电子券 测试数据
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
        Http.Response response = POST("/coupons/verify", params);
        assertIsOk(response);
        eCoupon.refresh();
        ECoupon eCouponConsumed = ECoupon.findById(eCoupon.id);
        assertEquals(ECouponStatus.CONSUMED, eCouponConsumed.status);

        ECouponHistoryMessage lastMessage = (ECouponHistoryMessage) MockMQ.getLastMessage(ECouponHistoryMessage.MQ_KEY);
        assertEquals("消费", lastMessage.remark);
    }

    @Test
    public void testUpdate_测试券过期情况() {
        // 生产 电子券 测试数据
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
                target.expireAt = DateUtil.getYesterday();
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
        Http.Response response = POST("/coupons/verify", params);
        assertIsOk(response);
        assertContentMatch("对不起，该券已过期", response);
    }

    /*
    @Test
    public void testUpdate_测试当当券已退款的情况() {
        // 生产 电子券 测试数据
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
                target.partner = ECouponPartner.DD;
                target.expireAt = goods.expireAt;
                target.operateUserId = 2L;
                target.originalPrice = new BigDecimal(100);
                target.salePrice = new BigDecimal(100);
                target.faceValue = new BigDecimal(150);
                target.partner = ECouponPartner.DD;
                target.effectiveAt = goods.effectiveAt;
            }
        });
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = eCoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><status_code>0</status_code><error_code>0</error_code>" +
                        "<desc><![CDATA[成功]]></desc><spid>3000003</spid><ver>1.0</ver>" +
                        "<data><ddgid>256</ddgid><spgid>256</spgid><state>2</state></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
        };

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
        Http.Response response = POST("/coupons/verify", params);
        assertIsOk(response);
        String info = (String) renderArgs("ecouponStatusDescription");
        assertNull(info);
        assertContentMatch("第三方DD券验证失败！", response);
    }
    */

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
        Http.Response response = POST("/coupons/verify", params);
        assertStatus(200, response);
        eCoupon.refresh();
        promoteRebate.refresh();
        ECoupon eCouponConsumed = ECoupon.findById(eCoupon.id);
        assertEquals(ECouponStatus.CONSUMED, eCouponConsumed.status);
        assertEquals(new BigDecimal("2.50"), promoteRebate.rebateAmount);
        assertEquals(eCouponConsumed.promoterRebateValue, promoteRebate.partAmount);
        assertEquals(RebateStatus.ALREADY_REBATE, promoteRebate.status);
    }

    @Test
    public void test_虚拟验证初始页面_conditionIsNull() {

        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, "Id", new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = goods;
                target.expireAt = DateHelper.afterDays(3);
                target.partner = ECouponPartner.JD;
                target.isCheatedOrder = true;
                target.isFreeze = 1;
            }
        });
        Http.Response response = GET("/coupons/virtual_verify");
        assertIsOk(response);
        assertContentType("text/html", response);
        List<ECoupon> couponList = (List) renderArgs("couponList");
        CouponsCondition condition = (CouponsCondition) renderArgs("condition");
        assertNotNull(couponList);
        assertNotNull(condition);

        assertEquals(1, couponList.size());
        assertEquals(DateUtil.getBeginExpiredDate(3), condition.expiredAtBegin);
        assertEquals(DateUtil.getEndExpiredDate(3), condition.expiredAtEnd);
    }

    @Test
    public void test_虚拟验证初始页面_conditionIsNotNull() {
        goods.noRefund = true;
        goods.save();

        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, "Id", new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = goods;
                target.expireAt = DateHelper.afterDays(10);
                target.partner = ECouponPartner.JD;
                target.isFreeze = 0;
            }
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Http.Response response = GET("/coupons/virtual_verify?condition.expiredAtBegin=" + simpleDateFormat.format(new Date()) + "&condition.expiredAtEnd=" + simpleDateFormat.format(DateHelper.afterDays(11)));
        assertIsOk(response);
        assertContentType("text/html", response);
        List<ECoupon> couponList = (List) renderArgs("couponList");
        CouponsCondition condition = (CouponsCondition) renderArgs("condition");
        assertNotNull(couponList);
        assertNotNull(condition);

        assertEquals(1, couponList.size());

    }

    @Test
    public void test_虚拟验证() {
        goods.noRefund = true;
        goods.save();

        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, "Id", new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.goods = goods;
                target.expireAt = DateHelper.afterDays(3);
                target.partner = ECouponPartner.JD;
                target.isFreeze = 0;
            }
        });

        Http.Response response = PUT("/coupons/" + eCoupon.id + "/virtual_verify","","");
        assertIsOk(response);
        assertContentType("text/html", response);
        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("虚拟验证失败！", errors.get(0).message());
    }

}
