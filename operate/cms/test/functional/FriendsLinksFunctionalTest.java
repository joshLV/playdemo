package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.cms.Block;
import models.cms.FriendsLink;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Juno
 * Date: 12-7-26
 * Time: 上午11:25
 */
public class FriendsLinksFunctionalTest extends FunctionalTest {
    FriendsLink friendsLink;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Block.class);
        OperateUser operateUser = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
        friendsLink = FactoryBoy.create(FriendsLink.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/friendsLinks");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertEquals(1, ((ModelPaginator) renderArgs("friendsLinkList")).size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/friendsLinks/new");
        assertIsOk(response);
        assertContentMatch("添加友情链接", response);
    }

    @Test
    public void testCreate() {
        int oldSize = FriendsLink.findAll().size();
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("friendsLinks.linkName", "testLink");
        goodsParams.put("friendsLinks.link", "www.test.com");
        Http.Response response = POST("/friendsLinks", goodsParams);
        assertStatus(302, response);
        int newSize = FriendsLink.findAll().size();
        assertEquals(oldSize + 1, newSize);
    }

    @Test
    public void testCreateWithError() {
        int oldSize = FriendsLink.findAll().size();
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("friendLinks.linkName", "testLink");
        //缺少 链接 参数，添加不成功
        Http.Response response = POST("/friendsLinks", goodsParams);
        assertStatus(200, response);
        int newSize = FriendsLink.findAll().size();
        assertEquals(oldSize, newSize);

    }

    @Test
    public void testEdit() {
        Http.Response response = GET("/friendsLinks/" + friendsLink.id + "/edit");
        assertIsOk(response);
        assertContentMatch("修改友情链接", response);
        assertEquals(friendsLink, (FriendsLink) renderArgs("friendsLinks"));

    }

    @Test
    public void testUpdate() {
        String params = "friendsLinks.linkName=changed&friendsLinks.link=www.changed.com";
        Http.Response response = PUT("/friendsLinks/" + friendsLink.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        friendsLink.refresh();
        assertEquals("changed", friendsLink.linkName);
        assertEquals("www.changed.com", friendsLink.link);

    }

    @Test
    public void testUpdateWithError() {
        String params = "friendsLinks.linkName=changed";
        Http.Response response = PUT("/friendsLinks/" + friendsLink.id, "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        FriendsLink newLink = FriendsLink.findById(friendsLink.id);
        assertFalse((newLink.linkName).equals("changed"));

    }

    @Test
    public void testDelete() {
        Http.Response response = DELETE("/friendsLinks/" + friendsLink.id);
        assertStatus(302, response);
        friendsLink.refresh();
        FriendsLink link = FriendsLink.findById(friendsLink.id);
        assertEquals(DeletedStatus.DELETED, link.deleted);
    }


}
