package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.Voucher;
import models.accounts.VoucherType;
import models.operator.OperateUser;
import models.consumer.User;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 生成抵用券 测试
 * <p/>
 * User: wangjia
 * Date: 13-1-5
 * Time: 上午9:41
 */
public class OperateVouchersTest extends FunctionalTest {
    Voucher voucher;
    Voucher voucher2;
    User user;
    OperateUser operator;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        operator = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operator.loginName);
        voucher = FactoryBoy.create(Voucher.class);
        user = FactoryBoy.create(User.class);
    }


    @Test
    public void testIndexOperatorNameNull() {
        Http.Response response = GET("/voucher");
        assertIsOk(response);
        assertEquals(((JPAExtPaginator<Voucher>) renderArgs("voucherPage")).get(0).value, voucher.value.setScale(2));
    }

    @Test
    public void testIndexVoucherExchangeType() {
        voucher.operatorId = operator.id;
        voucher.voucherType = VoucherType.EXCHANGE;
        voucher.save();
        voucher.refresh();
        Http.Response response = GET("/voucher");
        assertIsOk(response);
        assertEquals(((JPAExtPaginator<Voucher>) renderArgs("voucherPage")).get(0).value, voucher.value.setScale(2));
    }

    @Test
    public void testIndexVoucherOperateType() {
        voucher.operatorId = operator.id;
        voucher.voucherType = VoucherType.OPERATE;
        voucher.save();
        voucher.refresh();
        Http.Response response = GET("/voucher");
        assertIsOk(response);
        assertEquals(((JPAExtPaginator<Voucher>) renderArgs("voucherPage")).get(0).value, voucher.value.setScale(2));
    }


    @Test
    public void testUpdate() {
        String params = "id=" + voucher.id + "&action=delete";
        Http.Response response = PUT("/voucher", "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        assertEquals("{\"status\":\"ok\"}", getContent(response));
    }


    @Test
    public void testAssignNoInfo() {
        Map<String, String> params = new HashMap<>();
        Http.Response response = POST("/voucher/assign", params);
        assertStatus(302, response);
//        assertEquals((String) renderArgs("err"), "请输入一点信息啊");
    }


    @Test
    public void testAssignBlank() {
        Map<String, String> params = new HashMap<>();
        params.put("users", "\n");
        params.put("vouchers", "\n");
        params.put("type", "1toN");
        Http.Response response = POST("/voucher/assign", params);
        assertStatus(302, response);
//        assertEquals((String) renderArgs("err"), "起码输入一点内容，好不");
    }


    @Test
    public void testAssignBalance() {
        Map<String, String> params = new HashMap<>();
        params.put("users", user.id.toString());
        params.put("vouchers", voucher.chargeCode);
        params.put("type", "1to1");
//        voucher2 = FactoryBoy.create(Voucher.class);
//        DecimalFormat myFormatter = new DecimalFormat("00000");
//        voucher2.serialNo = "34" + myFormatter.format(33);
//        voucher2.save();
        Http.Response response = POST("/voucher/assign", params);
        assertStatus(302, response);
//        assertEquals((String) renderArgs("err"), "起码输入一点内容，好不");
    }


    @Test
    public void testAssignInvalidUser() {
        Long userId = user.id - 1l;
        Map<String, String> params = new HashMap<>();
        params.put("users", userId.toString());
        params.put("vouchers", voucher.chargeCode);
        params.put("type", "1to1");
        Http.Response response = POST("/voucher/assign", params);
        assertStatus(302, response);
    }


    @Test
    public void testAssignInvalidVoucherId() {
        Map<String, String> params = new HashMap<>();
        params.put("users", user.id.toString());
        params.put("vouchers", "1111");
        params.put("type", "1to1");
        Http.Response response = POST("/voucher/assign", params);
        assertStatus(302, response);
    }


    @Test
    public void testAssignInvalidUserId() {
        Map<String, String> params = new HashMap<>();
        params.put("users", "invalidId");
        params.put("vouchers", voucher.chargeCode);
        params.put("type", "1to1");
        Http.Response response = POST("/voucher/assign", params);
        assertStatus(302, response);
    }

    @Test
    public void testGeneratePrefixNull() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "test");
        params.put("prefix", "");
        params.put("faceValue", voucher.value.toString());
        params.put("count", "5");
        params.put("expire", DateHelper.afterDays(10).toString());
        Http.Response response = POST("/voucher/generate", params);
        assertStatus(302, response);
    }

    @Test
    public void testGenerateFaceValueNull() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "test");
        System.out.println("-----------------");
        params.put("prefix", voucher.prefix);
        params.put("faceValue", "");
        params.put("count", "5");
        params.put("expire", DateHelper.afterDays(10).toString());
        Http.Response response = POST("/voucher/generate", params);
        assertStatus(302, response);
    }

    @Test
    public void testGenerateInvalidCount() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "test");
        params.put("prefix", "1234");
        params.put("faceValue", voucher.value.toString());
        params.put("count", "99999999");
        params.put("expire", DateHelper.afterDays(10).toString());
        Http.Response response = POST("/voucher/generate", params);
        assertStatus(302, response);
    }

    @Test
    public void testGenerateInvalidExpire() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "test");
        params.put("prefix", "1234");
        params.put("faceValue", voucher.value.toString());
        params.put("count", "99999999");
        params.put("expire", DateHelper.beforeDays(100).toString());
        Http.Response response = POST("/voucher/generate", params);
        assertStatus(302, response);
    }


    @Test
    public void testGeneratePrefixExisted() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "test");
        params.put("prefix", "a");
        params.put("faceValue", voucher.value.toString());
        params.put("count", "5");
        params.put("expire", DateHelper.afterDays(10).toString());
        Http.Response response = POST("/voucher/generate", params);
        assertStatus(302, response);
    }

    @Test
    public void testGeneratePrefixNew() {
        Map<String, String> params = new HashMap<>();
//        String date = DateHelper.afterDays(10).toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(DateHelper.afterDays(10));
        params.put("name", "test");
        params.put("prefix", "b");
        params.put("faceValue", voucher.value.toString());
        params.put("count", "5");
        params.put("expire", date);
        params.put("uid", user.id.toString());
        Http.Response response = POST("/voucher/generate", params);
        assertStatus(302, response);
    }


    @Test
    public void testShowAssign() {
        Http.Response response = GET("/voucher/assign?err=error");
        assertStatus(200, response);
        assertEquals((String) renderArgs("err"), "error");
    }


}
