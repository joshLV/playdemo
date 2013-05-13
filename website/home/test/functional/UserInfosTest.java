/**
 *
 */
package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sms.SMSMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangjia
 * @date 2012-7-24 下午2:10:09
 */
public class UserInfosTest extends FunctionalTest {
    User user;
    UserInfo userInfo;

    @Before
    public void setup() {
        MockMQ.clear();
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        user.mobile = "15026666506";
        user.save();
        userInfo.user = user;
        userInfo.save();
        Security.setLoginUserForTest(user.loginName);

    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }


    @Test
    public void testSendValidCodeOldphoneNotExist() {
        Map<String, String> mobileParams = new HashMap<>();

        //旧手机号不存在
        mobileParams.put("mobile", "15026666504");
        mobileParams.put("oldMobile", "15026666505");
        Response response = POST("/user-info/send", mobileParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("3", response.out.toString()); // 浏览器相应

    }

    @Test
    public void testSendValidCodeNewOldphoneExist() {
        //新手机号存在 旧手机号存在
        Map<String, String> mobileParams = new HashMap<>();
        mobileParams.put("mobile", "15026666506");
        mobileParams.put("oldMobile", "15026666506");
        Response response = POST("/user-info/send", mobileParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("2", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testSendValidCodeChangePhone() {
        //旧手机号存在 新手机号格式正确
        Map<String, String> mobileParams = new HashMap<>();
        mobileParams.put("mobile", "15026666505");
        mobileParams.put("oldMobile", "15026666506");
        Response response = POST("/user-info/send", mobileParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("1", response.out.toString()); // 浏览器相应

        //测试cache
        String validCode = "123456";

        //更新手机信息
        String mobile = "15026666503";
        Cache.set("mobile_", mobile, "10mn");
        Cache.set("validCode_", validCode, "10mn");    //

        Object objCode = Cache.get("validCode_");
        Object objMobile = Cache.get("mobile_");
        assertEquals("123456", objCode.toString());
        assertEquals("15026666503", objMobile.toString());

        //验证手机发送的验证码

        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertNotNull("您的验证码是123456, 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码【一百券】", msg);
        assertEquals("您的验证码是123456, 请将该号码输入后即可验证成功。如非本人操作，请及时修改密码【一百券】", msg.getContent());

    }

    @Test
    public void testBindMobileOldPhoneNotExist() {
        Map<String, String> mobileParams = new HashMap<>();

        //旧手机号不存在
        mobileParams.put("mobile", "15026666504");
        mobileParams.put("oldMobile", "15026666505");
        mobileParams.put("validCode", "123456");
        Response response = POST("/user-info/mobile-bind", mobileParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("3", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testBindMobileWrongCode() {
        Map<String, String> mobileParams = new HashMap<>();

        //验证码不正确
        mobileParams.put("mobile", "15026666505");
        mobileParams.put("oldMobile", "15026666506");
        mobileParams.put("validCode", "123443");
        Response response = POST("/user-info/mobile-bind", mobileParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("1", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testBindMobileWrongNewPhone() {
        Map<String, String> mobileParams = new HashMap<>();

        //新手机号不正确
        String mobile = "15026666504";
        String validCode = "123456";
        Cache.set("mobile_", mobile, "10mn");
        mobileParams.put("mobile", "15026666503");
        mobileParams.put("oldMobile", "15026666506");
        mobileParams.put("validCode", "123456");
        Response response = POST("/user-info/mobile-bind", mobileParams);
        Cache.set("validCode_", validCode, "10mn");    //
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK

    }

    @Test
    public void testBindMobileUpdatePhone() {
        Map<String, String> mobileParams = new HashMap<>();
        String validCode = "123456";

        //更新手机信息
        String mobile = "15026666503";
        Cache.set("mobile_", mobile, "10mn");
        Cache.set("validCode_", validCode, "10mn");    //
        mobileParams.put("mobile", "15026666503");
        mobileParams.put("oldMobile", "15026666506");
        mobileParams.put("validCode", "123456");
        Response response = POST("/user-info/mobile-bind", mobileParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("0", response.out.toString()); // 浏览器相应


    }

    @Test
    public void testUpdate() {
        //存在则修改
        Long prevUserInfoId = userInfo.id;
        Map<String, String> mobileParams = new HashMap<>();
        mobileParams.put("userInfo.fullName", "xxx");
        mobileParams.put("id", userInfo.id.toString());
        mobileParams.put("interest", "yyy");
        Response response = POST("/user-info", mobileParams);
        assertStatus(302, response);
        userInfo.refresh();
        assertEquals(prevUserInfoId, userInfo.id);
        assertEquals("xxx", userInfo.fullName);
        assertEquals("yyy", userInfo.interest);

    }

}
