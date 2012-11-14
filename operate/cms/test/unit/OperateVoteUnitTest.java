package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-13
 * Time: 上午11:03
 */
public class OperateVoteUnitTest extends UnitTest {
    VoteQuestion vote;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        vote = FactoryBoy.create(VoteQuestion.class);
    }

    @Test
    public void testGetPage() {
        FactoryBoy.create(VoteQuestion.class, "now", new BuildCallback<VoteQuestion>() {
            @Override
            public void build(VoteQuestion target) {
                target.answer1 = "A";
            }
        });
        ModelPaginator page = VoteQuestion.getPage(1, 15, null);
        assertEquals(2, page.size());
        page = VoteQuestion.getPage(1, 15, VoteType.QUIZ);
        assertEquals(1, page.size());
        page = VoteQuestion.getPage(1, 15, VoteType.INQUIRY);
        assertEquals(1, page.size());

        vote.expireAt = new Date();
        vote.effectiveAt = new Date();
        vote.save();

        List<VoteQuestion> votesList = VoteQuestion.getPage(VoteType.QUIZ);
        assertEquals(1, votesList.size());
    }

    @Test
    public void testDelete() {
        VoteQuestion.delete(vote.id);
        vote.refresh();
        assertEquals(DeletedStatus.DELETED, vote.deleted);
    }

    @Test
    public void testGetAnswer() {
        vote.correctAnswer = "D";
        vote.save();
        vote.getAnswer();
        assertEquals("D", vote.getAnswer());

        vote.correctAnswer = "B";
        vote.save();
        vote.getAnswer();
        assertEquals("B", vote.getAnswer());

        vote.correctAnswer = "C";
        vote.save();
        vote.getAnswer();
        assertEquals("C", vote.getAnswer());

        vote.correctAnswer = "A";
        vote.save();
        vote.getAnswer();
        assertEquals("A", vote.getAnswer());
    }
}
