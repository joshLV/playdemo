package unit;

import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import models.PointGoodsCmsQuestion;
import models.cms.QuestionCondition;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-6
 * Time: 下午1:33
 * To change this template use File | Settings | File Templates.
 */
public class PointGoodsCmsQuestionUnitTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.delete(PointGoodsCmsQuestion.class);
        Fixtures.loadModels("Fixture/pointgoodsquestions.yml");
    }

    @Test
    public void testGetQuestionList(){
        QuestionCondition condition = new QuestionCondition();
        List<PointGoodsCmsQuestion> list = PointGoodsCmsQuestion.getQuestionList(condition,1,1);
        assertEquals(1,list.size());

    }

    @Test
    public void testFindOnGoodsShow(){
        long id = (Long) Fixtures.idCache.get("models.PointGoodsCmsQuestion-question1");
        PointGoodsCmsQuestion question = PointGoodsCmsQuestion.findById(id);
        List<PointGoodsCmsQuestion> list = PointGoodsCmsQuestion.findOnGoodsShow(question.userId,question.cookieId,question.pointGoodsId,0,5);
        assertEquals(1,list.size());

    }
}
