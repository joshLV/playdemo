package functional;

/**
 * @author wangjia
 * @date 2012-7-23 上午11:47:33 
 */

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.cms.VoteQuestion;
import models.consumer.User;
import models.consumer.UserVote;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

public class OperateConsumersWinningInfoTest extends FunctionalTest {
    User user;
    VoteQuestion voteQuestion;
    UserVote userVote;


    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
        user = FactoryBoy.create(User.class);
        voteQuestion = FactoryBoy.create(VoteQuestion.class);
        userVote = FactoryBoy.create(UserVote.class);
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
        assertContentMatch("用户参加有奖问答一览", response);

        //记录数
        List<UserVote> userVoteResult = UserVote.findAll();
        assertEquals(1, userVoteResult.size());

        //判断查询的结果是否正确
        List<UserVote> userVoteResultLastest = UserVote.find("user=? and vote=?", user, voteQuestion).fetch();
        UserVote userVote = userVoteResultLastest.get(0);
        assertEquals("A", userVote.answer);
        assertEquals(1, ((JPAExtPaginator<UserVote>) renderArgs("votePage")).size());

    }

    @Test
    public void testDelete() {
        Response response = DELETE("/consumers_votes/" + userVote.id);
        assertStatus(302, response);
        userVote.refresh();
        // 验证状态改为已删除状态
        UserVote userVoteDeleted = UserVote.findById(userVote.id);
        assertEquals(DeletedStatus.DELETED, userVoteDeleted.deleted);
    }

}
