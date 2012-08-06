package functional;

import java.util.HashMap;
import java.util.Map;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.cms.FriendsLink;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-26
 * Time: 上午11:25
 * To change this template use File | Settings | File Templates.
 */
public class FriendsLinksFunctionalTest extends FunctionalTest {

    @org.junit.Before
    public void setup() {

    Fixtures.delete(FriendsLink.class);
    Fixtures.delete(OperateUser.class);
    Fixtures.delete(OperateRole.class);
    Fixtures.loadModels("fixture/FriendsLink.yml");
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
        Http.Response response = GET("/friendsLinks");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    @Test
    public void testAdd(){
        Http.Response response = GET("/friendsLinks/new");
        assertIsOk(response);
        assertContentMatch("添加友情链接", response);
    }

    @Test
    public void testCreate(){
        //long id = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        int oldSize = FriendsLink.findAll().size();
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("friendsLinks.linkName","testLink");
        goodsParams.put("friendsLinks.link","www.test.com");
        Http.Response response = POST("/friendsLinks",goodsParams);
        assertStatus(302,response);
        int newSize = FriendsLink.findAll().size();
        assertEquals(oldSize+1,newSize);
    }

    @Test
    public void testCreateWithError(){
        int oldSize = FriendsLink.findAll().size();
        Map<String,String> goodsParams = new HashMap<>();
        goodsParams.put("friendLinks.linkName","testLink");
        //缺少 链接 参数，添加不成功
        Http.Response response = POST("/friendsLinks",goodsParams);
        assertStatus(200,response);
        int newSize = FriendsLink.findAll().size();
        assertEquals(oldSize, newSize);

    }

    @Test
    public void testEdit(){
        long id = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        Http.Response response = GET("/friendsLinks/" + id + "/edit");
        assertIsOk(response);
        assertContentMatch("修改友情链接",response);

    }

    @Test
    public void testUpdate(){
        long id = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        String params = "friendsLinks.linkName=changed&friendsLinks.link=www.changed.com";
        Http.Response response =  PUT("/friendsLinks/"+id,"application/x-www-form-urlencoded",params);
        assertStatus(302,response);
        FriendsLink newLink = FriendsLink.findById(id);
        assertEquals("changed",newLink.linkName);
        assertEquals("www.changed.com",newLink.link);

    }

    @Test
    public void testUpdateWithError(){
        long id = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        String params = "friendsLinks.linkName=changed";
        Http.Response response =  PUT("/friendsLinks/"+id,"application/x-www-form-urlencoded",params);
        assertStatus(200,response);
        FriendsLink newLink = FriendsLink.findById(id);
        assertFalse((newLink.linkName).equals("changed"));

    }

    @Test
    public void testDelete(){
        long id = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        Http.Response response = DELETE("/friendsLinks/"+id);
        assertStatus(302,response);
        FriendsLink link = FriendsLink.findById(id);
        assertEquals(DeletedStatus.DELETED,link.deleted);
    }


}
