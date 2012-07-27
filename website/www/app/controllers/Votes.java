package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import models.consumer.User;
import models.consumer.UserVote;
import play.mvc.Controller;
import play.mvc.With;
import play.test.Fixtures;

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
        	//user.refresh();
        	//vote.refresh();
        	//System.out.println(user.loginName);
        	//System.out.println(vote.content);
        	//System.out.println();
        	//long userVoteId = (Long) Fixtures.idCache.get("models.consumer.UserVote-seq_003");
//    		UserVote userVote= UserVote.findById(userVoteId);
//    		System.out.println(userVote.vote.content);
//    		System.out.println(userVote.user.loginName);
            if (UserVote.isVoted(user, vote)) {
            //	System.out.println("111111111");
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
           // System.out.println("2222");
            if (!UserVote.isVoted(user, vote)) {
            	//System.out.println("111111111");
                new UserVote(user, vote, voteSplits[1], mobile).save();
            }
        }
       // System.out.println("3333");
        viewAnswer();
    }

    @SkipCAS
    public static void viewAnswer() {
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
            if (UserVote.isVoted(user, vote)) {
                renderJSON("voted");
            }
        }
    }
}
