package functional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.cms.VoteQuestion;
import models.consumer.User;
import models.consumer.UserVote;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;

/**
 * @author wangjia
 * @date 2012-7-26 上午9:36:30
 */
public class VotesTest extends FunctionalTest {

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(VoteQuestion.class);
		Fixtures.delete(UserVote.class);
		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/votes.yml");
		Fixtures.loadModels("fixture/user_votes.yml");
		Long id = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
		VoteQuestion vote = VoteQuestion.findById(id);
		Date nowTime = new Date();
		vote.effectiveAt = nowTime;
		vote.expireAt = nowTime;
		vote.save();

		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);

		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
	}

	@After
	public void tearDown() {
		// 清除登录Mock
		Security.cleanLoginUserForTest();
	}

	@Test
	public void testIndexUserVote() {

		Response response = GET("/votes");
		assertStatus(302, response); // redirect
		response = GET("/votes/view");
		assertStatus(200, response);
		assertContentType("text/html", response);
		assertCharset(play.Play.defaultWebEncoding, response);
		assertContentMatch("参与成功页面", response);
	}

	@Test
	public void testIndexUserNotVote() {
		Long id = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote3");
		VoteQuestion vote = VoteQuestion.findById(id);
		long userVoteId = (Long) Fixtures.idCache.get("models.consumer.UserVote-seq_002");
		UserVote userVote = UserVote.findById(userVoteId);
		userVote.vote = vote;
		userVote.save();
		Response response = GET("/votes");
		assertStatus(200, response); // same
		assertContentType("text/html", response);
		assertCharset(play.Play.defaultWebEncoding, response);
		assertContentMatch("一百券", response);
	}

	@Test
	public void testIsVoted() {
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote3");
		String answer = voteId + "-B";
		Response response = GET("/is_voted?answers=" + answer);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("application/json", response); // this is OK
		assertCharset("utf-8", response); // this is OK
		assertEquals("voted", response.out.toString()); // 浏览器相应
	}

	@Test
	public void testIsNotVoted() {
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		String answer = voteId + "-B";
		Response response = GET("/is_voted?answers=" + answer);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(200, response);
		assertNotNull(response); // this is OK
		assertIsOk(response); // this is OK 200
		assertContentType("text/html", response);
		assertCharset("gbk", response); // this is OK
		assertNotSame("voted", response.out.toString()); // 浏览器相应
	}

	@Test
	public void testUpdateUserVote() {
		Map<String, String> anwserParams = new HashMap<>();
		Long voteIdTest = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
		anwserParams.put("answers", voteIdTest + "-B");
		anwserParams.put("mobile", "15618096151");
		//prev
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
		VoteQuestion votequestion = VoteQuestion.findById(voteId);
		List<UserVote> userVoteResultLastest = UserVote.find("user=? and vote=?", user, votequestion).fetch();
		assertEquals(1, userVoteResultLastest.size());
		Response response = POST("/votes", anwserParams);
		assertStatus(302, response); // redirect
		//after
		userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		user = User.findById(userId);
		voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
		votequestion = VoteQuestion.findById(voteId);
		userVoteResultLastest = UserVote.find("user=? and vote=?", user,votequestion).fetch();
		assertEquals(1, userVoteResultLastest.size());
	}

	@Test
	public void testUpdateUserNotVote() {
		Map<String, String> anwserParams = new HashMap<>();
		Long voteIdTest = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		anwserParams.put("answers", voteIdTest + "-B");
		anwserParams.put("mobile", "15618096151");
		//prev
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		VoteQuestion votequestion = VoteQuestion.findById(voteId);
		List<UserVote> userVoteResultLastest = UserVote.find("user=? and vote=?", user, votequestion).fetch();
		assertEquals(0, userVoteResultLastest.size());
		Response response = POST("/votes", anwserParams);
		assertStatus(302, response); // redirect
		//after
		userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		user = User.findById(userId);
		voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		votequestion = VoteQuestion.findById(voteId);
		userVoteResultLastest = UserVote.find("user=? and vote=?", user,votequestion).fetch();
		assertEquals(1, userVoteResultLastest.size());
	}
}
