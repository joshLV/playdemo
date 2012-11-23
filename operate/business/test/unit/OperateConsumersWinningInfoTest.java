package unit;

import factory.FactoryBoy;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import models.consumer.User;
import models.consumer.UserVote;
import models.consumer.UserVoteCondition;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;

import java.util.List;

/**
 * @author wangjia
 * @date 2012-7-20 下午5:50:39
 */
public class OperateConsumersWinningInfoTest extends UnitTest {
    User user;
    VoteQuestion voteQuestion;
    UserVote userVote;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        user = FactoryBoy.create(User.class);
        voteQuestion = FactoryBoy.create(VoteQuestion.class);
        userVote = FactoryBoy.create(UserVote.class);
    }

    @Test
    public void testUserVote() {
        List<UserVote> userVoteResult = UserVote.findAll();
        assertEquals(1, userVoteResult.size());
        String answer = "D";
        String mobile = "15618096151";
        new UserVote(user, voteQuestion, answer, mobile).save();
        userVoteResult = UserVote.findAll();
        // 增加一条记录
        assertEquals(2, userVoteResult.size());
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
        voteQuestion.type = VoteType.QUIZ;
        voteQuestion.save();
        voteQuestion.refresh();

        UserVoteCondition userVoteCondition = new UserVoteCondition();
        userVoteCondition.type = VoteType.QUIZ;
        JPAExtPaginator<UserVote> userVotePage = UserVote.getPage(1, 15, userVoteCondition);
        // 匹配条件的记录数
        assertEquals(1, userVotePage.size());
    }

    @Test
    public void isVoted() {
        assertTrue(UserVote.isVoted(user, voteQuestion));
        user = FactoryBoy.create(User.class);
        assertFalse(UserVote.isVoted(user, voteQuestion));
    }

}
