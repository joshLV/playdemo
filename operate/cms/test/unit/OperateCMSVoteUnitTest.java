package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

import factory.FactoryBoy;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-6-13
 * Time: 上午11:03
 */
public class OperateCMSVoteUnitTest extends UnitTest {
    VoteQuestion voteQuestion1;
    VoteQuestion voteQuestion2;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        voteQuestion1 = FactoryBoy.create(VoteQuestion.class);
        voteQuestion2 = FactoryBoy.create(VoteQuestion.class, "now");
    }

    @Test
    public void testGetPage() {
        ModelPaginator page = VoteQuestion.getPage(1, 15, null);
        assertEquals(2, page.size());
        page = VoteQuestion.getPage(1, 15, VoteType.QUIZ);
        assertEquals(1, page.size());
        page = VoteQuestion.getPage(1, 15, VoteType.INQUIRY);
        assertEquals(1, page.size());

        VoteQuestion vote = VoteQuestion.findById(voteQuestion2.id);
        vote.expireAt = new Date();
        vote.effectiveAt = new Date();
        vote.save();

        List<VoteQuestion> votesList = VoteQuestion.getPage(VoteType.QUIZ);
        assertEquals(1, votesList.size());
    }

    @Test
    public void testDelete() {
        VoteQuestion.delete(voteQuestion1.id);

        VoteQuestion vote = VoteQuestion.findById(voteQuestion1.id);

        assertNotNull(vote);
        assertEquals(DeletedStatus.DELETED, vote.deleted);
    }

    @Test
    public void testGetAnswer() {
        voteQuestion2.getAnswer();
        assertEquals("A", voteQuestion2.getAnswer());

        voteQuestion2.correctAnswer = "B";
        voteQuestion2.save();

        voteQuestion2.getAnswer();
        assertEquals("B", voteQuestion2.getAnswer());

        voteQuestion2.correctAnswer = "C";
        voteQuestion2.save();

        voteQuestion2.getAnswer();
        assertEquals("C", voteQuestion2.getAnswer());

        voteQuestion2.correctAnswer = "A";
        voteQuestion2.save();

        voteQuestion2.getAnswer();
        assertEquals("A", voteQuestion2.getAnswer());
    }
}
