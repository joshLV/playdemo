package functional;

import java.io.File;
import java.net.CookieStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.Play;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class RegisterTest extends FunctionalTest {
    UserInfo userInfo;
    User user;

    @Before
    public void setUp() {
//        FactoryBoy.lazyDelete();
        FactoryBoy.delete(User.class);
        FactoryBoy.delete(UserInfo.class);
//        FactoryBoy.deleteAll();
        userInfo=FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

    }

    @Test
    public void testIndex() {
        Response response = GET("/register");
        assertStatus(200, response);
        assertContentMatch("新用户注册", response);
    }

    @Test
    public void testCreate_tj_cookieNull() {
        List old = User.findAll();
        int count = old.size();
        Map<String, String> loginUserParams = new HashMap<String,String>();
        loginUserParams.put("user.loginName", "11@qq.com");
        loginUserParams.put("user.password", "123456");
        loginUserParams.put("user.confirmPassword", "123456");
        loginUserParams.put("user.captcha", "A2WQ");
        loginUserParams.put("randomID", "RANDOMID");
        Cache.set("RANDOMID", "A2WQ","30mn");
        Response response = POST("/register", loginUserParams);
        assertStatus(200,response);
        List newList = User.findAll();
        assertEquals(count+1,newList.size());
    }

    @Test
    public void testCreate_PromoterUserNull() {
        List old = User.findAll();
        int count = old.size();
        Map<String, String> loginUserParams = new HashMap<String, String>();
        loginUserParams.put("user.loginName", "11@qq.com");
        loginUserParams.put("user.password", "123456");
        loginUserParams.put("user.confirmPassword", "123456");
        loginUserParams.put("user.captcha", "A2WQ");
        loginUserParams.put("randomID", "RANDOMID");
        loginUserParams.put("promoter_track", "123");
        Cache.set("RANDOMID", "A2WQ", "30mn");
        Map<String, Http.Cookie> passCookie = new HashMap();
        Http.Cookie newCookie=new Http.Cookie();
        newCookie.name="promoter_track";
        newCookie.value="123";
        passCookie.put("promoter_track",newCookie);
        Http.Request request= FunctionalTest.newRequest();
        request.cookies =passCookie;
        Response  response = POST(request,"/register", loginUserParams,new HashMap<String, File>());
        assertStatus(200, response);
        List newList = User.findAll();
        assertEquals(count + 1, newList.size());
		List userInfos = UserInfo.findAll();
		assertEquals(2,userInfos.size());

    }

    @Test
    public void testCreate_tj_cookie_PromoterUserNotNull() {
        List old = User.findAll();
        int count = old.size();
        Map<String, String> loginUserParams = new HashMap<String, String>();
        loginUserParams.put("user.loginName", "11@qq.com");
        loginUserParams.put("user.password", "123456");
        loginUserParams.put("user.confirmPassword", "123456");
        loginUserParams.put("user.captcha", "A2WQ");
        loginUserParams.put("randomID", "RANDOMID");
        loginUserParams.put("promoter_track", "123");
        Cache.set("RANDOMID", "A2WQ", "30mn");
        Map<String, Http.Cookie> passCookie = new HashMap();
        Http.Cookie newCookie=new Http.Cookie();
        newCookie.name="promoter_track";
        newCookie.value="123";
        passCookie.put("promoter_track",newCookie);
        Http.Request request= FunctionalTest.newRequest();
        request.cookies =passCookie;
        user.promoterCode="123";
        user.save();
        Response  response = POST(request,"/register", loginUserParams,new HashMap<String, File>());
        assertStatus(200, response);
        List newList = User.findAll();
        assertEquals(count + 1, newList.size());
    }

    @Test
    public void testCheckLoginNameNotExist() {
        Map<String, String> loginUserParams = new HashMap<String, String>();
        loginUserParams.put("loginName", "11@qq.com");
        loginUserParams.put("mobile", "1313112112");
        Response response = POST("/register/checkLoginName", loginUserParams);
        assertStatus(200, response);
        assertCharset(Play.defaultWebEncoding, response);
        assertContentMatch("0", response);
    }

    @Test
    public void testCheckLoginNameExist() {
        Map<String, String> loginUserParams = new HashMap<String, String>();
        loginUserParams.put("loginName", "selenium@uhuila.com");
        loginUserParams.put("mobile", "15026682165");
        Response response = POST("/register/checkLoginName", loginUserParams);
        assertStatus(200, response);
        assertCharset(Play.defaultWebEncoding, response);
        assertContentMatch("1", response);
    }

    @Test
    public void testCreateUserError() {
        Long userId = user.id;
        User user = User.findById(userId);
         User user1 = FactoryBoy.create(User.class, "loginName");
        //mobile error,loginName existed ,user's captcha empty,user's password not equal user's confirmpassword
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("user.mobile", "123");
        userParams.put("user.loginName", "11@qq.com");
        userParams.put("user.password", "123456");
        userParams.put("user.confirmPassword", "1234567");
//		userParams.put("user.captcha", "A2WQ");
        userParams.put("randomID", "RANDOMID");
        Cache.set("RANDOMID", "A2WQ", "30mn");
        Response response = POST("/register", userParams);
        assertEquals(0, Validation.errors().size());
    }

    @Test
    public void testCaptcha() {
        Response response = GET("/captcha");
        assertStatus(200, response);

    }


}
