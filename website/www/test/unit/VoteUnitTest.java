package unit;

import models.cms.VoteQuestion;
import models.cms.VoteType;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-6-13
 * Time: 上午11:18
 */
public class VoteUnitTest extends UnitTest {
    VoteQuestion vote;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        vote = FactoryBoy.create(VoteQuestion.class);
    }

    @Test
    public void testGetPage() {
        vote.expireAt = new Date();
        vote.effectiveAt = new Date();
        vote.type = VoteType.QUIZ;
        vote.save();
        List<VoteQuestion> votesList = VoteQuestion.getPage(VoteType.QUIZ);
        assertEquals(1, votesList.size());

    }


}
