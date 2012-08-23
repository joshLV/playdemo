/**
 * 
 */
package functional;

import java.util.HashMap;
import java.util.Map;

import models.cms.CmsQuestion;
import models.consumer.User;
import models.consumer.UserInfo;
import models.sales.Goods;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;

/**
 * @author wangjia
 * @date 2012-7-27 上午10:02:37
 */
public class UserQuestionsTest extends FunctionalTest {

	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(UserInfo.class);
		Fixtures.delete(Goods.class);
		Fixtures.delete(CmsQuestion.class);
		Fixtures.loadModels("fixture/goods.yml");
		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/user_question.yml");

		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		Long userQuestionId = (Long) Fixtures.idCache.get("models.cms.CmsQuestion-cmsquestion1");
		CmsQuestion userQuestion = CmsQuestion.findById(userQuestionId);
		userQuestion.goodsId = goodsId;
		userQuestion.save();

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
		long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		userQuestionParams.put("goodsId", String.valueOf(goodsId)); // goodsId+""
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
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
		Map<String, String> userQuestionParams = new HashMap<>();
		userQuestionParams.put("content", "");
		long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		userQuestionParams.put("goodsId", String.valueOf(goodsId)); // goodsId+""
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
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
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

		long userId = auth();
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		Response response = GET("/g/" + goodsId);
		response.setCookie("identity", String.valueOf(userId), "365d");
		Map<String, String> userQuestionParams = new HashMap<>();
		userQuestionParams.put("content", "aaa");
		userQuestionParams.put("goodsId", String.valueOf(goodsId)); // goodsId+""
		response = POST("/user-question", userQuestionParams);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK
		assertEquals("{\"questions\":[{\"content\":\"aaa\",\"date\":\"2012-07-26\",\"user\":\"selenium@uhuila.com\"}]}",response.out.toString()); // 浏览器相应

	}

	@Test
	public void testAddGoodIdExistUserNotExist() {

		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		Response response = GET("/g/" + goodsId);
		response.setCookie("identity", String.valueOf(1), "365d");
		Map<String, String> userQuestionParams = new HashMap<>();
		userQuestionParams.put("content", "aaa");
		userQuestionParams.put("goodsId", String.valueOf(goodsId)); // goodsId+""
		response = POST("/user-question", userQuestionParams);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK
		assertEquals("{\"questions\":[{\"content\":\"aaa\",\"date\":\"2012-07-26\",\"user\":\"游客\"}]}",response.out.toString()); // 浏览器相应
	}

	@Test
	public void testMoreQuestionsUser() {
		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		Response response = GET("/more-questions?goodsId=" + goodsId+ "&firstResult=0" + "&size=5");// ?goodsId="+goodsId        +"&size=1"
		assertStatus(200, response);
		assertEquals("{\"questions\":[{\"content\":\"满百送电影票活动，是不是拍一张这个面值一百的就可以了？还是这个只算80块？\",\"date\":\"2012-07-26\",\"user\":\"用户\"}]}",response.out.toString()); // 浏览器相应

	}

	@Test
	public void testMoreQuestionsVisitor() {

		Long userQuestionId = (Long) Fixtures.idCache.get("models.cms.CmsQuestion-cmsquestion1");
		CmsQuestion userQuestion = CmsQuestion.findById(userQuestionId);
		userQuestion.userName = null;
		userQuestion.save();

		Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-goods1");
		Response response = GET("/more-questions?goodsId=" + goodsId+ "&firstResult=0" + "&size=5");// ?goodsId="+goodsId        +"&size=1"
		assertStatus(200, response);
		System.out.println("Result:" + response.out.toString());
		assertEquals("{\"questions\":[{\"content\":\"满百送电影票活动，是不是拍一张这个面值一百的就可以了？还是这个只算80块？\",\"date\":\"2012-07-26\",\"user\":\"游客\"}]}",response.out.toString()); // 浏览器相应
		

	}

	private long auth() {
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
		return userId;
	}

	

}
