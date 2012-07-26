import models.cms.VoteQuestion;
import models.consumer.User;
import models.consumer.UserVote;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

/**
 * @author wangjia
 * @date 2012-7-26 下午6:44:48 
 */
public class VoteQuestionTest  extends UnitTest {

	
	@Before
	public void setup() {
		Fixtures.delete(VoteQuestion.class);
		Fixtures.loadModels("fixture/votes.yml");
	}
	
	 @Test
	    public void TestUpdate(){
			Long voteId2 = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
			VoteQuestion votequestion2 = VoteQuestion.findById(voteId2);	
			Long voteId3 = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote3");
			VoteQuestion votequestion3 = VoteQuestion.findById(voteId3);	
			assertEquals("优惠啦网站改版了，改成什么名字？3",votequestion3.content);
			//3-->2
			votequestion2.update(voteId3,votequestion2);		
			assertEquals("优惠啦网站改版了，改成什么名字？2",votequestion3.content);		 
	 }
}
