package unit;

import java.util.List;

import models.cms.VoteQuestion;

import org.junit.Before;
import org.junit.Test;

import com.uhuila.common.constants.DeletedStatus;

import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class VoteQuestionTest extends UnitTest {

	VoteQuestion voteQuestion;

	@Before
	public void setup() {
		FactoryBoy.lazyDelete();
		voteQuestion = FactoryBoy.create(VoteQuestion.class);
	}

	@Test
	public void testUpdate() {

		VoteQuestion form = FactoryBoy.build(VoteQuestion.class,
				new BuildCallback<VoteQuestion>() {
					@Override
					public void build(VoteQuestion vq) {
						vq.content = "修改测试内容.";
					}
				});
		VoteQuestion.update(voteQuestion.id, form);
		assertEquals("修改测试内容.", voteQuestion.content);
	}

	@Test
	public void testGetPage() throws Exception {
	    ModelPaginator<VoteQuestion> page = VoteQuestion.getPage(1, 10, voteQuestion.type);
		assertEquals(1, page.getRowCount());
	}

	@Test
	public void testGetPage1() throws Exception {
		List<VoteQuestion> list = VoteQuestion.getPage(voteQuestion.type);
		assertEquals(1, list.size());
	}
	
	@Test
	public void testDelete() throws Exception {
		VoteQuestion.delete(voteQuestion.id);
		VoteQuestion vq = VoteQuestion.findById(voteQuestion.id);
		assertEquals(DeletedStatus.DELETED, vq.deleted);
	}
	
	@Test
	public void testGetAnswer() throws Exception {
		voteQuestion.correctAnswer = "A";
		assertEquals("A", voteQuestion.getAnswer());
		voteQuestion.correctAnswer = "B";
		assertEquals("B", voteQuestion.getAnswer());
		voteQuestion.correctAnswer = "C";
		assertEquals("C", voteQuestion.getAnswer());
		voteQuestion.correctAnswer = "D";
		assertEquals("D", voteQuestion.getAnswer());
	}
}
