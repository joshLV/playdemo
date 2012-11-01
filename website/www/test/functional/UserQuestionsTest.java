/**
 *
 */
package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import models.cms.CmsQuestion;
import models.consumer.User;
import models.sales.Goods;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;


/**
 * @author wangjia
 * @date 2012-7-27 上午10:02:37
 */
public class UserQuestionsTest extends FunctionalTest {
    User user;
    Goods goods;
    CmsQuestion cmsQuestion;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
        cmsQuestion = FactoryBoy.create(CmsQuestion.class);
        cmsQuestion.goodsId = goods.id;
        cmsQuestion.save();
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testAddNoIdentity() {
        Map<String, String> userQuestionParams = new HashMap<>();
        userQuestionParams.put("content", "aaa");
        userQuestionParams.put("goodsId", String.valueOf(goods.id)); // goodsId+""
        Response response = POST("/user-question", userQuestionParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        // TODO:
        // assertEquals("{\"error\":\"无法获知提问者身份\"}", getContent(response)); // 浏览器相应
    }

    @Test
    public void testAddInvalidQuestion() {
        auth();
        Map<String, String> userQuestionParams = new HashMap<>();
        userQuestionParams.put("content", "");
        userQuestionParams.put("goodsId", String.valueOf(goods.id)); // goodsId+""
        Response response = POST("/user-question", userQuestionParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("{\"error\":\"请输入问题\"}", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testAddGoodIdNull() {
        auth();
        Map<String, String> userQuestionParams = new HashMap<>();
        userQuestionParams.put("content", "aaa");
        Response response = POST("/user-question", userQuestionParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("{\"error\":\"该商品无法评论\"}", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testAddGoodIdUserExist() {
        auth();
        Response response = GET("/g/" + goods.id);
        response.setCookie("identity", String.valueOf(user.id), "365d");
        Map<String, String> userQuestionParams = new HashMap<>();
        userQuestionParams.put("content", "aaa");
        userQuestionParams.put("goodsId", String.valueOf(goods.id)); // goodsId+""
        response = POST("/user-question", userQuestionParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("{\"error\":\"\",\"questions\":[{\"content\":\"aaa\",\"date\":\"2012-07-26\",\"user\":\"selenium@uhuila.com\"}]}", response.out.toString()); // 浏览器相应

    }

    @Test
    public void testAddGoodIdExistUserNotExist() {
        Response response = GET("/g/" + goods.id);
        response.setCookie("identity", String.valueOf(1), "365d");
        Map<String, String> userQuestionParams = new HashMap<>();
        userQuestionParams.put("content", "aaa");
        userQuestionParams.put("goodsId", String.valueOf(goods.id)); // goodsId+""
        response = POST("/user-question", userQuestionParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        assertNotNull(response); // this is OK
        assertIsOk(response); // this is OK
        assertContentType("application/json", response); // this is OK
        assertCharset("utf-8", response); // this is OK
        assertEquals("{\"error\":\"\",\"questions\":[{\"content\":\"aaa\",\"date\":\"2012-07-26\",\"user\":\"游客\"}]}", response.out.toString()); // 浏览器相应
    }

    @Test
    public void testMoreQuestionsUser() {
        Response response = GET("/more-questions?goodsId=" + goods.id + "&firstResult=0" + "&size=5");// ?goodsId="+goodsId        +"&size=1"
        assertStatus(200, response);
        assertEquals("{\"questions\":[{\"content\":\"满百送电影票活动，是不是拍一张这个面值一百的就可以了？还是这个只算80块？\",\"date\":\"2012-07-26\",\"user\":\"用户\"}]}", response.out.toString()); // 浏览器相应

    }

    @Test
    public void testMoreQuestionsVisitor() {
        cmsQuestion.userName = null;
        cmsQuestion.save();
        Response response = GET("/more-questions?goodsId=" + goods.id + "&firstResult=0" + "&size=5");// ?goodsId="+goodsId        +"&size=1"
        assertStatus(200, response);
        System.out.println("Result:" + response.out.toString());
        assertEquals("{\"questions\":[{\"content\":\"满百送电影票活动，是不是拍一张这个面值一百的就可以了？还是这个只算80块？\",\"date\":\"2012-07-26\",\"user\":\"游客\"}]}", response.out.toString()); // 浏览器相应

    }

    private void auth() {
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }


}
