package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import models.consumer.User;
import models.consumer.UserVote;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-11
 * Time: 下午4:29
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class Votes extends Controller {
    /**
     * 问卷调查
     */
    @SkipCAS
    public static void index() {
        List<VoteQuestion> votes = VoteQuestion.getPage(VoteType.QUIZ);
        User user = SecureCAS.getUser();
        for (VoteQuestion vote : votes) {
            List<VoteQuestion> voteList = UserVote.find("user=? and vote=?", user, vote).fetch();
            if (voteList.size() > 0) {
                viewAnswer();
                break;
            }
        }
        render(votes, user);
    }

    /**
     * 更新
     *
     * @param answers 答案
     */
    public static void update(String answers, String mobile) {

        User user = SecureCAS.getUser();
        String[] answerSplits = answers.split(",");
        for (String split : answerSplits) {
            String[] voteSplits = split.split("-");
            VoteQuestion vote = VoteQuestion.findById(Long.parseLong(voteSplits[0]));
            UserVote userVote = new UserVote(user, vote, voteSplits[1], mobile);
            userVote.save();
            renderArgs.put("answer", vote.getAnswer());
        }

        render("/Votes/vote_success.html");
    }

    @SkipCAS
    public static void viewAnswer() {
        User user = SecureCAS.getUser();
        VoteQuestion vote = VoteQuestion.getPage(VoteType.QUIZ).get(0);
        renderArgs.put("answer", vote.getAnswer());
        render("/Votes/vote_success.html");
    }

    /**
     * 判断是否已经参与过
     *
     * @param answers 答案
     */
    @SkipCAS
    public static void isVoted(String answers) {

        User user = SecureCAS.getUser();
        String[] answerSplits = answers.split(",");
        for (String split : answerSplits) {
            String[] voteSplits = split.split("-");
            VoteQuestion vote = VoteQuestion.findById(Long.parseLong(voteSplits[0]));
            List<VoteQuestion> voteList = UserVote.find("user=? and vote=?", user, vote).fetch();
            if (voteList.size() > 0) {
                renderJSON("voted");
            }
        }
    }
}
