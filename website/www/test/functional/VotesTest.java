package functional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.cms.VoteQuestion;
import models.cms.VoteType;
import models.consumer.User;
import models.consumer.UserInfo;
import models.consumer.UserVote;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;

/**
 * @author wangjia
 * @date 2012-7-26 上午9:36:30
 */
public class VotesTest extends FunctionalTest {
    User user;
    VoteQuestion voteQuestion;
    UserVote userVote;


    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();

        UserInfo userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        voteQuestion = FactoryBoy.create(VoteQuestion.class);
        voteQuestion.type = VoteType.QUIZ;
        voteQuestion.save();
        userVote = FactoryBoy.create(UserVote.class);
        user.mobile = "15618096151";
        user.save();

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
        voteQuestion.type = VoteType.INQUIRY;
        voteQuestion.save();
        Response response = GET("/votes");
        assertStatus(200, response);
        assertEquals(user, renderArgs("user"));
        voteQuestion.type = VoteType.QUIZ;
        voteQuestion.save();
        response = GET("/votes/view");
        assertStatus(200, response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        assertEquals(voteQuestion.getAnswer(), renderArgs("answer"));
        assertContentMatch("参与成功页面", response);
    }

    @Test
    public void testIndexUserNotVote() {
        User user2 = FactoryBoy.create(User.class);
        VoteQuestion voteQuestion2 = FactoryBoy.create(VoteQuestion.class);
        userVote.vote = voteQuestion2;
        userVote.save();
        Response response = GET("/votes");
        assertStatus(200, response); // same
        assertEquals(user, renderArgs("user"));
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
        assertContentMatch("一百券", response);
    }

    @Test
    public void testIsVoted() {
        String answer = voteQuestion.id.toString() + "-A";
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
        VoteQuestion voteQuestion2 = FactoryBoy.create(VoteQuestion.class);
        String answer = voteQuestion2.id + "-A";
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
        anwserParams.put("answers", voteQuestion.id + "-B");
        anwserParams.put("mobile", "15618096151");
        //prev
        List<UserVote> userVoteResultLastest = UserVote.find("user=? and vote=?", user, voteQuestion).fetch();
        assertEquals(1, userVoteResultLastest.size());
        Response response = POST("/votes", anwserParams);
        assertStatus(302, response); // redirect
        //after
        userVoteResultLastest = UserVote.find("user=? and vote=?", user, voteQuestion).fetch();
        assertEquals(1, userVoteResultLastest.size());
    }

    @Test
    public void testUpdateUserNotVote() {
        Map<String, String> anwserParams = new HashMap<>();
        VoteQuestion voteQuestion1 = FactoryBoy.create(VoteQuestion.class);
        anwserParams.put("answers", voteQuestion.id + "-B");
        anwserParams.put("mobile", "15618096151");
        //prev
        List<UserVote> userVoteResultLastest = UserVote.find("user=? and vote=?", user, voteQuestion1).fetch();
        assertEquals(0, userVoteResultLastest.size());
        Response response = POST("/votes", anwserParams);
        assertStatus(302, response); // redirect
        //after
        userVoteResultLastest = UserVote.find("user=? and vote=?", user, voteQuestion1).fetch();
        assertEquals(0, userVoteResultLastest.size());
    }
}
