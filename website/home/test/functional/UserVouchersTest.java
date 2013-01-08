package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.Voucher;
import models.consumer.User;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-7
 */

public class UserVouchersTest extends FunctionalTest {
    Voucher assignedVoucher = null, unassignedVoucher = null;
    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        // 设置虚拟登陆
        // 设置测试登录的用户名
        final User user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);

        final Account account = FactoryBoy.create(Account.class, new BuildCallback<Account>() {
            @Override
            public void build(Account target) {
                target.uid = user.getId();
                target.accountType = AccountType.CONSUMER;
            }
        });

        assignedVoucher = FactoryBoy.create(Voucher.class, new BuildCallback<Voucher>() {
            @Override
            public void build(Voucher target) {
                target.account = account;
                target.assignedAt = new Date();
            }
        });

        unassignedVoucher = FactoryBoy.create(Voucher.class, new BuildCallback<Voucher>() {
            @Override
            public void build(Voucher target) {
                target.chargeCode = target.chargeCode + "1";
                target.serialNo = target.serialNo + "1";
            }
        });

    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/voucher");

        assertIsOk(response);
        assertNotNull(renderArgs("voucherList"));
        assertNotNull(renderArgs("validVouchers"));
        assertNotNull(renderArgs("validValue"));

        List<Voucher> voucherList = (List<Voucher>)renderArgs("validVouchers");
        BigDecimal validValue = (BigDecimal)renderArgs("validValue");

        assertEquals(1, voucherList.size());
        assertEquals(0, assignedVoucher.value.compareTo(validValue));
    }

    @Test
    public void testShowAssign() {
        Http.Response response = GET("/voucher/assign");

        assertIsOk(response);

        assertNotNull(renderArgs("randomID"));
        assertNotNull(renderArgs("action"));
    }

    @Test
    public void testVerify() {
        String randomID = Codec.UUID();

        //模拟请求一次验证码

        Map<String, String> params = new HashMap<>();
        params.put("voucherCode", unassignedVoucher.chargeCode);
        params.put("code", (String)Cache.get(randomID));
        params.put("randomID", randomID);

        //测试验证码
        params.put("code", (String)Cache.get(randomID) + "1");

        GET("/captcha?id=" + randomID);
        Http.Response response = POST("/voucher/verify", params);
        assertIsOk(response);
        assertEquals("验证码错误", renderArgs("errMsg"));

        randomID = (String)renderArgs("randomID");
        params.put("randomID", randomID);

        //测试券不存在
        params.put("voucherCode", unassignedVoucher.chargeCode + "1");

        GET("/captcha?id=" + randomID);
        params.put("code", (String) Cache.get(randomID));
        response = POST("/voucher/verify", params);
        assertIsOk(response);
        assertEquals("抵用券充值密码输入错误", renderArgs("errMsg"));

        params.put("voucherCode", unassignedVoucher.chargeCode);
        randomID = (String)renderArgs("randomID");
        params.put("randomID", randomID);

        //测试券已被使用
        unassignedVoucher.account = FactoryBoy.last(Account.class);
        unassignedVoucher.save();

        GET("/captcha?id=" + randomID);
        params.put("code", (String)Cache.get(randomID));
        response = POST("/voucher/verify", params);
        assertIsOk(response);
        assertEquals("该抵用券已被使用", renderArgs("errMsg"));

        unassignedVoucher.account = null;
        unassignedVoucher.save();
        randomID = (String)renderArgs("randomID");
        params.put("randomID", randomID);

        //测试券已被删除
        unassignedVoucher.deleted = DeletedStatus.DELETED;
        unassignedVoucher.save();

        GET("/captcha?id=" + randomID);
        params.put("code", (String) Cache.get(randomID));
        response = POST("/voucher/verify", params);
        assertIsOk(response);
        assertEquals("该券无法使用", renderArgs("errMsg"));

        unassignedVoucher.deleted = DeletedStatus.UN_DELETED;
        unassignedVoucher.save();
        randomID = (String)renderArgs("randomID");
        params.put("randomID", randomID);

        //测试券已过期
        unassignedVoucher.expiredAt = new Date(System.currentTimeMillis() - 10000000);
        unassignedVoucher.save();

        GET("/captcha?id=" + randomID);
        params.put("code", (String) Cache.get(randomID));
        response = POST("/voucher/verify", params);
        assertIsOk(response);
        assertEquals("该券已过期", renderArgs("errMsg"));

        unassignedVoucher.expiredAt = new Date(System.currentTimeMillis() + 1000000);
        unassignedVoucher.save();
        randomID = (String)renderArgs("randomID");
        params.put("randomID", randomID);

        //验证成功

        GET("/captcha?id=" + randomID);
        params.put("code", (String) Cache.get(randomID));
        response = POST("/voucher/verify", params);
        assertIsOk(response);
        assertNull(renderArgs("errMsg"));
        assertNotNull(renderArgs("randomID"));
        assertNotNull(renderArgs("action"));
        assertEquals("assign", renderArgs("action"));
        assertNotNull(renderArgs("voucherCode"));
        assertEquals(unassignedVoucher.chargeCode, (String)renderArgs("voucherCode"));
        assertNotNull(renderArgs("ridA"));
        assertNotNull(renderArgs("ridB"));
        assertEquals(renderArgs("ridB"), Cache.get((String)renderArgs("ridA")));
        assertEquals(unassignedVoucher.getId(), (Long)Cache.get((String)renderArgs("ridB")));
    }

    @Test
    public void testAssign() {
        String ridA = Codec.UUID();
        String ridB = Codec.UUID();
        Cache.set(ridA, ridB, "5mn");
        Cache.set(ridB, unassignedVoucher.getId(), "5mn");

        Map<String, String> params = new HashMap<>();
        params.put("ridA", ridA);
        params.put("ridB", ridB);

        //验证random id a错误
        params.put("ridA", ridA + "1");
        Http.Response response = POST("/voucher/assign", params);
        assertIsOk(response);
        assertEquals("验证失败", renderArgs("errMsg"));

        unassignedVoucher.refresh();
        assertNull(unassignedVoucher.account);
        assertNull(unassignedVoucher.assignedAt);

        //验证random id b错误
        Cache.set(ridA, ridB, "5mn");
        Cache.set(ridB, unassignedVoucher.getId(), "5mn");
        params.put("ridB", ridB + "1");

        response = POST("/voucher/assign", params);
        assertIsOk(response);
        assertEquals("验证失败", renderArgs("errMsg"));

        unassignedVoucher.refresh();
        assertNull(unassignedVoucher.account);
        assertNull(unassignedVoucher.assignedAt);

        //测试已领取的券
        params.put("ridA", ridA);
        params.put("ridB", ridB);
        Cache.set(ridA, ridB, "5mn");
        Cache.set(ridB, assignedVoucher.getId(), "5mn");

        response = POST("/voucher/assign", params);
        assertIsOk(response);
        assertEquals("验证失败", renderArgs("errMsg"));

        unassignedVoucher.refresh();
        assertNull(unassignedVoucher.account);
        assertNull(unassignedVoucher.assignedAt);

        //测试已删除的券
        unassignedVoucher.deleted = DeletedStatus.DELETED;
        unassignedVoucher.save();
        params.put("ridA", ridA);
        params.put("ridB", ridB);
        Cache.set(ridA, ridB, "5mn");
        Cache.set(ridB, unassignedVoucher.getId(), "5mn");

        response = POST("/voucher/assign", params);
        assertIsOk(response);
        assertEquals("该券无法使用", renderArgs("errMsg"));

        unassignedVoucher.refresh();
        assertNull(unassignedVoucher.account);
        assertNull(unassignedVoucher.assignedAt);
        unassignedVoucher.deleted = DeletedStatus.UN_DELETED;
        unassignedVoucher.save();

        //测试过期
        unassignedVoucher.expiredAt = new Date(System.currentTimeMillis() - 100000);
        unassignedVoucher.save();
        params.put("ridA", ridA);
        params.put("ridB", ridB);
        Cache.set(ridA, ridB, "5mn");
        Cache.set(ridB, unassignedVoucher.getId(), "5mn");

        response = POST("/voucher/assign", params);
        assertIsOk(response);
        assertEquals("该券已过期", renderArgs("errMsg"));

        unassignedVoucher.refresh();
        assertNull(unassignedVoucher.account);
        assertNull(unassignedVoucher.assignedAt);
        unassignedVoucher.deleted = DeletedStatus.UN_DELETED;
        unassignedVoucher.expiredAt = new Date(System.currentTimeMillis() + 100000);
        unassignedVoucher.save();

        //正常验证
        params.put("ridA", ridA);
        params.put("ridB", ridB);
        Cache.set(ridA, ridB, "5mn");
        Cache.set(ridB, unassignedVoucher.getId(), "5mn");

        response = POST("/voucher/assign", params);
        assertIsOk(response);
        assertNull(renderArgs("errMsg"));

        unassignedVoucher.refresh();
        assertNotNull(unassignedVoucher.account);
        assertNotNull(unassignedVoucher.assignedAt);
    }
}
