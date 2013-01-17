package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.admin.OperateUser;
import models.cms.CmsQuestion;
import models.consumer.User;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-28
 * Time: 上午11:26
 */
public class OperateQuestionsTest extends FunctionalTest {
    OperateUser operateUser;
    CmsQuestion cmsQuestion;
    User user;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        // f重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);

        user = FactoryBoy.create(User.class);
        goods = FactoryBoy.create(Goods.class);
        cmsQuestion = FactoryBoy.create(CmsQuestion.class, new BuildCallback<CmsQuestion>() {
            @Override
            public void build(CmsQuestion cmsQuestion) {
                cmsQuestion.userId = user.id;
                cmsQuestion.goodsId = goods.id;
                cmsQuestion.operateUserId = operateUser.id;
                cmsQuestion.visible = true;
            }
        });


    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }


    @Test
    public void testIndex() {
        Http.Response response = GET("/questions");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertEquals(1, ((JPAExtPaginator<CmsQuestion>) renderArgs("questions")).size());

    }

    @Test
    public void testDelete() {
        assertEquals(1, CmsQuestion.count());
        Http.Response response = DELETE("/questions/" + cmsQuestion.id);
        assertStatus(302, response);
        assertEquals(0, CmsQuestion.count());
    }


    @Test
    public void testEdit() {
        Http.Response response = GET("/questions/" + cmsQuestion.id + "/edit");
        assertIsOk(response);
        assertEquals(cmsQuestion, (CmsQuestion) renderArgs("question"));
    }

    @Test
    public void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("question.reply", "test-reply");
        Http.Response response = POST("/questions/" + cmsQuestion.id, params);
        cmsQuestion.refresh();
        assertEquals("test-reply", cmsQuestion.reply);
    }

    @Test
    public void testHide() {
        assertEquals(true, cmsQuestion.visible);
        Http.Response response = PUT("/questions/" + cmsQuestion.id + "/hide", "text/html", "");
        cmsQuestion.refresh();
        assertEquals(false, cmsQuestion.visible);
    }

    @Test
    public void testShow() {
        cmsQuestion = FactoryBoy.create(CmsQuestion.class, new BuildCallback<CmsQuestion>() {
            @Override
            public void build(CmsQuestion cmsQuestion) {
                cmsQuestion.userId = user.id;
                cmsQuestion.goodsId = goods.id;
                cmsQuestion.operateUserId = operateUser.id;
                cmsQuestion.visible = false;
            }
        });
        assertEquals(false, cmsQuestion.visible);
        Http.Response response = PUT("/questions/" + cmsQuestion.id + "/show", "text/html", "");
        cmsQuestion.refresh();
        assertEquals(true, cmsQuestion.visible);
    }
}
