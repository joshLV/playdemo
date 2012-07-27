package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.admin.OperateUser;
import models.cms.Topic;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-27
 * Time: 上午11:25
 * To change this template use File | Settings | File Templates.
 */
public class OperateTopicsFunctionalTest extends FunctionalTest {

    @Before
    public void setup() {
        Fixtures.delete(Topic.class);
        Fixtures.loadModels("fixture/topics.yml");

        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/users.yml");


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
    public void testIndex(){
        Http.Response response = GET("/topics");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    @Test
    public void testAdd(){
        Http.Response response = GET("/topics/new");
        assertIsOk(response);
        assertContentMatch("添加公告", response);
    }

    @Test
    public void testCreate(){
        //配置 Topic 参数
        Map<String,String> params = new HashMap<>();
        params.put("topic.title","TestTitle123456789");
        params.put("topic.displayOrder","1");
        params.put("topic.content","TestContent");
        params.put("topic.effectiveAt","2012-3-21");
        params.put("topic.expireAt","2013-3-21");

        Http.Response response = POST("/topics", params);
        assertStatus(302,response);
        int size = Topic.findAll().size();
        // 创建成功 size增加
        assertEquals(2,size);
    }

    //@Test
    public void testCreateWithError(){
        //配置 Topic 参数
        Map<String,String> params = new HashMap<>();

        Http.Response response = POST("/topics",params);
        assertStatus(200,response);
        int size = Topic.findAll().size();
        // 创建失败 size不变
        assertEquals(1,size);
    }

    @Test
    public void testEdit(){
        long id = (Long) Fixtures.idCache.get("models.cms.Topic-Test");
        assertNotNull(id);

        Http.Response response = GET("/topics/" + id + "/edit");
        assertIsOk(response);
        assertContentMatch("修改公告",response);
    }

    @Test
    public void testUpdate(){
        long id = (Long) Fixtures.idCache.get("models.cms.Topic-Test");
        String params = "topic.title=TestTitle123456789" +
                "&topic.displayOrder=1" +
                "&topic.effectiveAt=2012-3-21" +
                "&topic.expireAt=2013-3-21" +
                "&topic.content=Test Content";
        Http.Response response =  PUT("/topics/"+id,"application/x-www-form-urlencoded",params);
        assertStatus(302,response);
        Topic topic = Topic.findById(id);
        assertTrue((topic.title).equals("TestTitle123456789"));
    }

    @Test
    public void testUpdateWithError(){
        long id = (Long) Fixtures.idCache.get("models.cms.Topic-Test");
        String params = "topic.title=TestTitle";
        // 传一个空的参数给UPDATE，更新不成功
        Http.Response response =  PUT("/topics/"+id,"application/x-www-form-urlencoded",params);
        assertStatus(200,response);
        Topic topic = Topic.findById(id);
        assertFalse((topic.title).equals("TestTitle"));
    }

    @Test
    public void testDelete(){
        long id = (Long) Fixtures.idCache.get("models.cms.Topic-Test");
        Http.Response response = DELETE("/topics/"+id);
        assertStatus(302,response);
        Topic topic = Topic.findById(id);
        assertEquals(DeletedStatus.DELETED,topic.deleted);
    }

}
