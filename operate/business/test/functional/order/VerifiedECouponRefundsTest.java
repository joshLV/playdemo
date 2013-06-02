package functional.order;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.operator.OperateUserFactory;
import factory.callback.BuildCallback;
import factory.resale.ResalerFactory;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.operator.OperateRole;
import models.operator.OperateUser;
import models.consumer.User;
import models.operator.Operator;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class VerifiedECouponRefundsTest extends FunctionalTest {
    BigDecimal baseAmount = new BigDecimal(10000);
    Goods goods;
    Order order;
    OperateUser operateUser;
    ECoupon ecoupon;
    Account supplierAccount;
    Account platformCommissionAccount;
    Resaler yibaiquanResaler;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        yibaiquanResaler = ResalerFactory.getYibaiquanResaler();

        // only sales role.
        operateUser = FactoryBoy.create(OperateUser.class, new BuildCallback<OperateUser>() {
            @Override
            public void build(OperateUser user) {
                // 定义角色
                user.roles = new ArrayList<OperateRole>();
                user.roles.add(OperateUserFactory.role("sales"));
                user.roles.add(OperateUserFactory.role("manager"));
            }
        });
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        Supplier supplier = FactoryBoy.create(Supplier.class);
        supplierAccount = AccountUtil.getSupplierAccount(supplier.id, supplier.defaultOperator());
        supplierAccount.amount = baseAmount;
        supplierAccount.save();

        goods = FactoryBoy.create(Goods.class);
        order = FactoryBoy.create(Order.class);
        ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon ecoupon) {
                ecoupon.salePrice = goods.salePrice;
                ecoupon.originalPrice = goods.originalPrice;
                ecoupon.status = ECouponStatus.CONSUMED;
            }
        });

        platformCommissionAccount = AccountUtil.getPlatformCommissionAccount(Operator.defaultOperator());
        platformCommissionAccount.amount = new BigDecimal(1000);
        platformCommissionAccount.save();
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Response response = GET("/verified-ecoupon-refunds");
        assertIsOk(response);
    }

    @Test
    public void 不输入券号() throws Exception {
        Response response = POST("/verified-ecoupon-do-refund");
        assertIsOk(response);
        assertContentMatch("券号不能为空", response);
        assertNull(renderArgs("ecoupon"));
    }

    @Test
    public void 不输入备注() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("eCouponSn", ecoupon.eCouponSn);
        Response response = POST("/verified-ecoupon-do-refund", map);
        assertIsOk(response);
        assertContentMatch("备注不能为空", response);
        assertNull(renderArgs("ecoupon"));
    }

    @Test
    public void 输入不存在的券号() throws Exception {
        Response response = POST("/verified-ecoupon-do-refund", getECouponSnParams("11234"));
        assertIsOk(response);
        assertContentMatch("不存在的券号或券号未验证", response);
        assertNull(renderArgs("ecoupon"));
    }

    @Test
    public void 输入未验证的券号() throws Exception {
        ecoupon.status = ECouponStatus.UNCONSUMED;
        ecoupon.save();
        Response response = POST("/verified-ecoupon-do-refund", getECouponSnParams(ecoupon.eCouponSn));
        assertIsOk(response);
        assertContentMatch("不存在的券号或券号未验证", response);
        assertNull(renderArgs("ecoupon"));
    }

    @Test
    public void 输入已验证的一百券券号并完成退款() throws Exception {
        User user = FactoryBoy.create(User.class);
        order.consumerId = user.id;
        order.userId = yibaiquanResaler.id;
        order.accountPay = ecoupon.salePrice;
        order.save();

        Response response = POST("/verified-ecoupon-do-refund", getECouponSnParams(ecoupon.eCouponSn));
        assertIsOk(response);

        ECoupon e = (ECoupon) renderArgs("ecoupon");
        assertEquals(ecoupon.id, e.id);
        ECoupon checkECoupon = ECoupon.findById(e.id);
        checkECoupon.refresh();
        assertEquals(ECouponStatus.REFUND, checkECoupon.status);

        // 检查余额
        Account userAccount = AccountUtil.getAccount(user.id, AccountType.CONSUMER);
        userAccount.refresh();
        assertEquals(ecoupon.salePrice.setScale(2), userAccount.amount.setScale(2));
        supplierAccount.refresh();
        assertEquals(baseAmount.subtract(ecoupon.originalPrice).setScale(2), supplierAccount.amount.setScale(2));

        // 佣金账户少钱
        platformCommissionAccount.refresh();
        assertEquals((new BigDecimal(1000)).subtract(ecoupon.salePrice.subtract(ecoupon.originalPrice)).setScale(2), platformCommissionAccount.amount.setScale(2));
    }

    @Test
    public void 输入已验证的分销券号并完成退款() throws Exception {
        // 分销商
        Resaler resaler = FactoryBoy.create(Resaler.class);
        order.userId = resaler.id;
        order.accountPay = new BigDecimal("8.5");
        order.save();

        Response response = POST("/verified-ecoupon-do-refund", getECouponSnParams(ecoupon.eCouponSn));
        assertIsOk(response);

        ECoupon e = (ECoupon) renderArgs("ecoupon");
        assertEquals(ecoupon.id, e.id);
        ECoupon checkECoupon = ECoupon.findById(e.id);
        checkECoupon.refresh();
        assertEquals(ECouponStatus.REFUND, checkECoupon.status);

        // 检查余额
        // 商户少了oritinPrice
        Account resalerAccount = AccountUtil.getAccount(resaler.id, AccountType.RESALER);
        assertEquals(ecoupon.salePrice.setScale(2), resalerAccount.amount.setScale(2));
        supplierAccount.refresh();
        assertEquals(baseAmount.subtract(ecoupon.originalPrice).setScale(2), supplierAccount.amount.setScale(2));
        // 佣金账户少钱
        platformCommissionAccount.refresh();
        assertEquals((new BigDecimal(1000)).subtract(ecoupon.salePrice.subtract(ecoupon.originalPrice)).setScale(2), platformCommissionAccount.amount.setScale(2));
    }

    private Map<String, String> getECouponSnParams(String eCouponSn) {
        Map<String, String> map = new HashMap<>();
        map.put("eCouponSn", eCouponSn);
        map.put("choice", "REFUND");
        map.put("refundComment", "取消XXX验证");
        return map;
    }
}
