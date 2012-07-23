package unit;

import java.util.List;

import models.cms.VoteQuestion;
import models.cms.VoteType;
import models.consumer.User;
import models.consumer.UserVote;
import models.consumer.UserVoteCondition;
import models.sales.Brand;

import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ModelPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * @author wangjia
 * @date 2012-7-20 下午5:50:39
 */
public class OperateConsumersWinningInfoTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(VoteQuestion.class);
		Fixtures.delete(UserVote.class);

		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/votes.yml");
		Fixtures.loadModels("fixture/user_votes.yml");
	}

	@Test
	public void testUserVote() {
		List<UserVote> userVoteResult = UserVote.findAll();
		assertEquals(3, userVoteResult.size());
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		VoteQuestion votequestion = VoteQuestion.findById(voteId);
		String answer = "D";
		String mobile = "15618096151";
		new UserVote(user, votequestion, answer, mobile).save();
		userVoteResult = UserVote.findAll();
		// 增加一条记录
		assertEquals(4, userVoteResult.size());
		List<UserVote> userVoteResultLatest = UserVote.find("order by id desc").fetch();
		UserVote vote = userVoteResultLatest.get(0);
		// 判断增加的记录的内容是否正确
		assertEquals("selenium@uhuila.com", vote.user.loginName);
		assertEquals(VoteType.INQUIRY, vote.vote.type);
		assertEquals("D", vote.answer);
		assertEquals("15618096151", vote.mobile);

	}

	@Test
	public void testGetPage() {
		UserVoteCondition userVoteCondition = new UserVoteCondition();
		userVoteCondition.type = VoteType.QUIZ;
		JPAExtPaginator<UserVote> userVotePage = UserVote.getPage(1, 15,userVoteCondition);
		// 匹配条件的记录数
		assertEquals(2, userVotePage.size());
	}

	@Test
	public void isVoted() {
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		// 已投票
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		VoteQuestion votequestion = VoteQuestion.findById(voteId);
		assertTrue(UserVote.isVoted(user, votequestion));
		// 未投票
		userId = (long) Fixtures.idCache.get("models.consumer.User-user_test1");
		user = User.findById(userId);
		assertFalse(UserVote.isVoted(user, votequestion));
	}

}
