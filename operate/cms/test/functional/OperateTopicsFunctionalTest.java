package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.cms.Topic;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Juno
 * Date: 12-7-27
 * Time: 上午11:25
 */
public class OperateTopicsFunctionalTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);


        OperateUser user = FactoryBoy.create(OperateUser.class);
        FactoryBoy.create(Topic.class);


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
        Http.Response response = GET("/topics");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/topics/new");
        assertIsOk(response);
        assertContentMatch("添加公告", response);
    }

    @Test
    public void testCreate() {
        //配置 Topic 参数
        Map<String, String> params = new HashMap<>();
        params.put("topic.title", "TestTitle123456789");
        params.put("topic.displayOrder", "1");
        params.put("topic.content", "TestContent");
        params.put("topic.effectiveAt", "2012-3-21");
        params.put("topic.expireAt", "2013-3-21");

        Http.Response response = POST("/topics", params);
        assertStatus(302, response);
        int size = Topic.findAll().size();
        // 创建成功 size增加
        assertEquals(2, size);
    }

    //@Test
    public void testCreateWithError() {
        //配置 Topic 参数
        Map<String, String> params = new HashMap<>();

        Http.Response response = POST("/topics", params);
        assertStatus(200, response);
        int size = Topic.findAll().size();
        // 创建失败 size不变
        assertEquals(1, size);
    }

    @Test
    public void testEdit() {
        Topic topic = FactoryBoy.last(Topic.class);
        assertNotNull(topic);

        Http.Response response = GET("/topics/" + topic.id + "/edit");
        assertIsOk(response);
        assertContentMatch("修改公告", response);
    }

    @Test
    public void testUpdate() {
        Topic topic = FactoryBoy.last(Topic.class);
        assertNotNull(topic);

        String params = "topic.title=TestTitle123456789" +
                "&topic.displayOrder=1" +
                "&topic.effectiveAt=2012-3-21" +
                "&topic.expireAt=2013-3-21" +
                "&topic.content=Test Content";
        Http.Response response = PUT("/topics/" + topic.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        topic.refresh();
        assertTrue((topic.title).equals("TestTitle123456789"));
    }

    @Test
    public void testUpdateWithError() {
        Topic topic = FactoryBoy.last(Topic.class);
        assertNotNull(topic);

        String params = "topic.title=TestTitle";
        // 传一个空的参数给UPDATE，更新不成功
        Http.Response response = PUT("/topics/" + topic.id, "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        topic.refresh();
        assertFalse((topic.title).equals("TestTitle"));
    }

    @Test
    public void testDelete() {
        Topic topic = FactoryBoy.last(Topic.class);
        assertNotNull(topic);

        Http.Response response = DELETE("/topics/" + topic.id);
        assertStatus(302, response);
        topic.refresh();
        assertEquals(DeletedStatus.DELETED, topic.deleted);
    }
}
