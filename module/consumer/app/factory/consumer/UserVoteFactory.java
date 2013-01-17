package factory.consumer;

import models.cms.VoteQuestion;
import models.consumer.User;
import models.consumer.UserVote;
import factory.ModelFactory;
import factory.FactoryBoy;

/**
 * User: wangjia
 * Date: 12-10-30
 * Time: 下午3:51
 */
public class UserVoteFactory extends ModelFactory<UserVote> {
    @Override
    public UserVote define() {
        User user = FactoryBoy.lastOrCreate(User.class);
        VoteQuestion voteQuestion = FactoryBoy.lastOrCreate(VoteQuestion.class);
        UserVote userVote = new UserVote(user, voteQuestion, "A", user.mobile);
        return userVote;
    }
}
