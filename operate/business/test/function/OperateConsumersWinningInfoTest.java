package function;

/**
 * @author wangjia
 * @date 2012-7-23 上午11:47:33 
 */

import java.util.List;

import javax.persistence.Query;

import models.admin.OperateRole;
import models.admin.OperateUser;
import models.cms.VoteQuestion;
import models.cms.VoteType;
import models.consumer.User;
import models.consumer.UserVote;
import operate.rbac.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import com.uhuila.common.constants.DeletedStatus;

import controllers.operate.cas.Security;

public class OperateConsumersWinningInfoTest extends FunctionalTest {

	@Before
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(VoteQuestion.class);
		Fixtures.delete(UserVote.class);
		Fixtures.delete(OperateUser.class);
		Fixtures.delete(OperateRole.class);

		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/votes.yml");
		Fixtures.loadModels("fixture/user_votes.yml");
		Fixtures.loadModels("fixture/roles.yml");
		Fixtures.loadModels("fixture/supplierusers.yml");

		// 重新加载配置文件
		VirtualFile file = VirtualFile.open("conf/rbac.xml");
		RbacLoader.init(file);

		Long id = (Long) Fixtures.idCache.get("models.admin.OperateUser-user3");
		OperateUser user = OperateUser.findById(id);
		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
	}

	@After
	public void tearDown() {
		// 清除登录Mock
		Security.cleanLoginUserForTest();
	}

	@Test
	public void testIndex() {
		Response response = GET("/consumers_votes");
		assertStatus(200, response);
		assertContentMatch("用户参加有奖问答一览",response);
		
		//记录数
		List<UserVote> userVoteResult = UserVote.findAll();
		assertEquals(3, userVoteResult.size());

		//判断查询的结果是否正确		
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);	
		Long voteId = (Long) Fixtures.idCache.get("models.cms.VoteQuestion-vote1");
		VoteQuestion votequestion = VoteQuestion.findById(voteId);		
		List<UserVote> userVoteResultLastest= UserVote.find("user=? and vote=?", user,votequestion).fetch();
		UserVote userVote = userVoteResultLastest.get(0);
		assertEquals("A", userVote.answer);
		
}

	@Test
	public void testDelete() {
		long userVoteId = (Long) Fixtures.idCache.get("models.consumer.UserVote-seq_001");
		Response response = DELETE("/consumers_votes/" + userVoteId);
		assertStatus(302, response);

		// 验证状态改为已删除状态
		UserVote userVoteDeleted = UserVote.findById(userVoteId);
		assertEquals(DeletedStatus.DELETED, userVoteDeleted.deleted);
	}

}
