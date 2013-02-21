package functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.operator.OperateUser;
import models.cms.VoteQuestion;
import operate.rbac.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.data.validation.Error;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import com.uhuila.common.constants.DeletedStatus;

import controllers.operate.cas.Security;
import factory.FactoryBoy;

public class WebVotesTest extends FunctionalTest {

    VoteQuestion vote;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        vote = FactoryBoy.create(VoteQuestion.class);

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
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
        Response response = GET("/votes");
        assertIsOk(response);
        ModelPaginator votePage = (ModelPaginator) renderArgs("votePage");
        assertNotNull(votePage);
        assertEquals(1, votePage.getRowCount());
    }

    @Test
    public void testAdd() {
        Response response = GET("/votes/new");
        assertIsOk(response);
    }

    @Test
    public void testEdit() {
        Response response = GET("/votes/" + vote.id + "/edit");
        assertIsOk(response);
        VoteQuestion v = (VoteQuestion) renderArgs("vote");
        assertEquals(vote.id, v.id);
    }

    @Test
    public void testDelete() {
        Response response = DELETE("/votes/" + vote.id);
        assertStatus(302, response);
        vote.refresh();
        assertEquals(DeletedStatus.DELETED, vote.deleted);
    }

    /**
     * #{layout_operate.selectField name:'vote.type', value:vote?.type, error:'vote.type',
     * class:'span2'}
     * #{option models.cms.VoteType.QUIZ}&{'vote.QUIZ'}#{/option}
     * #{option models.cms.VoteType.INQUIRY}&{'vote.INQUIRY'}#{/option}
     * #{/layout_operate.selectField}
     * #{layout_operate.dateScopeField name:'vote.effective', begin:'vote.effectiveAt',end:'vote.expireAt',
     * beginValue:vote?.effectiveAt?.format(), endValue:vote?.expireAt?.format(), required:true/}
     * #{layout_operate.textareaField name:'vote.content', value:vote?.content, required:true/}
     * #{layout_operate.textField name:'vote.answer1', value:vote?.answer1, required:true/}
     * #{layout_operate.textField name:'vote.answer2', value:vote?.answer2, required:true/}
     * #{layout_operate.textField name:'vote.answer3', value:vote?.answer3,required:true/}
     * #{layout_operate.textField name:'vote.answer4', value:vote?.answer4,required:true/}
     * #{layout_operate.textField name:'vote.correctAnswer', value:vote?.correctAnswer,required:true/}     *
     */

    @Test
    public void testCreate() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vote.type", "QUIZ");
        params.put("vote.effectiveAt", "2012-10-31");
        params.put("vote.expireAt", "2012-12-31");
        params.put("vote.content", "Test");
        params.put("vote.answer1", "A");
        params.put("vote.answer2", "B");
        params.put("vote.answer3", "C");
        params.put("vote.answer4", "D");
        params.put("vote.correctAnswer", "C");
        Response response = POST("/votes", params);
        assertStatus(302, response);
        assertEquals(2, VoteQuestion.count());
    }

    @Test
    public void testUpdate() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vote.type", "QUIZ");
        params.put("vote.effectiveAt", "2012-10-31");
        params.put("vote.expireAt", "2012-12-31");
        params.put("vote.content", "Test");
        params.put("vote.answer1", "Hi");
        params.put("vote.answer2", "B");
        params.put("vote.answer3", "C");
        params.put("vote.answer4", "D");
        params.put("vote.correctAnswer", "C");

        Response response = POST("/votes/" + vote.id + "?x-http-method-override=PUT",
                params); //PUT
        assertStatus(302, response);
        vote.refresh();
        assertEquals("Hi", vote.answer1);
    }

    @Test
    public void testUpdateInvalid() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vote.type", "QUIZ");
        params.put("vote.expireAt", "2012-12-31");
        params.put("vote.content", "Test");
        params.put("vote.answer1", "Hi");
        params.put("vote.answer2", "B");
        params.put("vote.answer3", "C");
        params.put("vote.answer4", "D");
        params.put("vote.correctAnswer", "C");

        Response response = POST("/votes/" + vote.id + "?x-http-method-override=PUT",
                params); //PUT
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("vote.effectiveAt", errors.get(0).getKey());
        assertStatus(200, response);

    }

    @Test
    public void testCheckExpireAt() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vote.type", "QUIZ");
        params.put("vote.effectiveAt", "2012-10-31");
        params.put("vote.expireAt", "2012-5-31");
        params.put("vote.content", "Test");
        params.put("vote.answer1", "Hi");
        params.put("vote.answer2", "B");
        params.put("vote.answer3", "C");
        params.put("vote.answer4", "D");
        params.put("vote.correctAnswer", "C");

        Response response = POST("/votes/" + vote.id + "?x-http-method-override=PUT",
                params); //PUT
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("vote.expireAt", errors.get(0).getKey());
        assertStatus(200, response);

    }

    @Test
    public void testCreateInvalid() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("vote.type", "QUIZ");
        params.put("vote.expireAt", "2012-12-31");
        params.put("vote.content", "Test");
        params.put("vote.answer1", "A");
        params.put("vote.answer2", "B");
        params.put("vote.answer3", "C");
        params.put("vote.answer4", "D");
        params.put("vote.correctAnswer", "C");
        Response response = POST("/votes", params);
        List<play.data.validation.Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("vote.effectiveAt", errors.get(0).getKey());
        assertStatus(200, response);
    }


}
