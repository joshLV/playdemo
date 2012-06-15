package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-6-13
 * Time: 上午11:03
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
        ModelPaginator page = VoteQuestion.getPage(1, 15, null);
        assertEquals(2, page.size());
        page = VoteQuestion.getPage(1, 15, VoteType.QUIZ);
        assertEquals(1, page.size());
        page = VoteQuestion.getPage(1, 15, VoteType.INQUIRY);
        assertEquals(1, page.size());

        long id = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");


        VoteQuestion vote = VoteQuestion.findById(id);
        vote.expireAt = new Date();
        vote.effectiveAt = new Date();
        vote.save();

        List<VoteQuestion> votesList = VoteQuestion.getPage(VoteType.QUIZ);
        assertEquals(1, votesList.size());
    }

    @Test
    public void testDelete() {
        long id = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");

        VoteQuestion.delete(id);

        VoteQuestion vote = VoteQuestion.findById(id);

        assertNotNull(vote);
        assertEquals(DeletedStatus.DELETED, vote.deleted);
    }

    @Test
    public void testGetAnswer() {
        long id = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote2");
        VoteQuestion vote = VoteQuestion.findById(id);
        vote.getAnswer();
        assertEquals("D.一百券", vote.getAnswer());

        vote.correctAnswer = "B";
        vote.save();
        vote = VoteQuestion.findById(id);
        vote.getAnswer();
        assertEquals("B.原优惠啦", vote.getAnswer());

        vote.correctAnswer = "C";
        vote.save();
        vote = VoteQuestion.findById(id);
        vote.getAnswer();
        assertEquals("C.不知道", vote.getAnswer());

        vote.correctAnswer = "A";
        vote.save();
        vote = VoteQuestion.findById(id);
        vote.getAnswer();
        assertEquals("A.优惠拉", vote.getAnswer());
    }
}
