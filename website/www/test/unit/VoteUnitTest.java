package unit;

import models.cms.VoteQuestion;
import models.cms.VoteType;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-6-13
 * Time: 上午11:18
 */
public class VoteUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(VoteQuestion.class);
        Fixtures.loadModels("fixture/votes.yml");
    }

    @Test
    public void testGetPage() {

        long id = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
        VoteQuestion vote = VoteQuestion.findById(id);
        vote.expireAt = new Date();
        vote.effectiveAt = new Date();
        vote.save();
        List<VoteQuestion> votesList = VoteQuestion.getPage(VoteType.QUIZ);
        assertEquals(1, votesList.size());

    }


}
