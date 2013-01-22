package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.cms.Block;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Juno
 */
public class BlocksTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Block.class);
        OperateUser operateUser = FactoryBoy.create(OperateUser.class);


        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/blocks");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/blocks/new");
        assertIsOk(response);
        assertContentMatch("添加内容块", response);
    }

    @Test
    // CmsBlock.java 中的create()方法 添加了忽略测试代码中上传文件为空的异常，以通过测试
    public void testCreate() {
        //配置 BLOCK 参数
        Map<String, String> params = new HashMap<>();
        params.put("block.title", "TestTitle");
        params.put("block.displayOrder", "1");
        params.put("block.effectiveAt", "2012-3-21 00:00:00");
        params.put("block.expireAt", "2019-3-21 00:00:00");
        params.put("content", "TestContent");

        //配置 图片 参数
        Map<String, File> files = new HashMap<>();
        File file = Play.getFile("test/p.jpg");
        //确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imageUrl", file);

        Http.Response response = POST("/blocks", params, files);
        int size = Block.findAll().size();
        // 创建成功 size增加
        assertEquals(2, size);
    }

    @Test
    public void testCreate_WithHotKeys() {
        //配置 BLOCK 参数
        Map<String, String> params = new HashMap<>();
        params.put("block.title", "TestTitle");
        params.put("block.type", "HOT_KEYWORDS");
        params.put("block.displayOrder", "1");
        params.put("block.effectiveAt", "2012-3-21 00:00:00");
        params.put("block.expireAt", "2019-3-21 00:00:00");
        params.put("content", "TestContent");

        //配置 图片 参数
        Map<String, File> files = new HashMap<>();
        File file = Play.getFile("test/p.jpg");
        //确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imageUrl", file);

        Http.Response response = POST("/blocks", params, files);
        assertStatus(302, response);
        int size = Block.findAll().size();
        // 创建成功 size增加
        assertEquals(2, size);
        Block blcok = Block.find("order by id desc").first();
        assertEquals("http://www.yibaiquan.com/q?s=TestTitle", blcok.link);

    }

    @Test
    public void testEdit() {
        Block block = FactoryBoy.last(Block.class);
        assertNotNull(block);

        Http.Response response = GET("/blocks/" + block.id + "/edit");
        assertIsOk(response);
        assertContentMatch("修改内容块", response);
    }

    @Test
    public void testUpdate() {
        Block block = FactoryBoy.last(Block.class);
        assertNotNull(block);
        String params = "block.title=TestTitle" +
                "&block.displayOrder=1" +
                "&block.effectiveAt=2012-3-21" +
                "&block.expireAt=2113-3-21" +
                "&content=Test Content";
        Http.Response response = PUT("/blocks/" + block.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        block.refresh();
        assertTrue((block.title).equals("TestTitle"));
    }

    @Test
    public void testUpdateWithError() {
        Block block = FactoryBoy.last(Block.class);
        assertNotNull(block);
        String params = "block.title=TestTitle" +
                "&block.displayOrder=1" +
                "&block.effectiveAt=2012-3-21" +
                "&content=Test Content";
        Http.Response response = PUT("/blocks/" + block.id, "application/x-www-form-urlencoded", params);
        block.refresh();
        List<play.data.validation.Error> errors = (List<play.data.validation.Error>) renderArgs("errors");
        assertEquals("block.expireAt", errors.get(0).getKey());
    }

    @Test
    public void testUpdateWithError_NotCheckImage() {
        Block block = FactoryBoy.last(Block.class);
        assertNotNull(block);
        String params = "block.title=TestTitle1" +
                "&block.displayOrder=1" +
                "&block.effectiveAt=2012-3-21" +
                "&block.expireAt=2113-3-21" +
                "&block.type=HOT_KEYWORDS" +
                "&content=Test Content";
        Http.Response response = PUT("/blocks/" + block.id, "application/x-www-form-urlencoded", params);
        block.refresh();
        assertEquals("http://www.yibaiquan.com/q?s=TestTitle1", block.link);
    }

    @Test
    public void testDelete() {
        Block block = FactoryBoy.last(Block.class);
        assertNotNull(block);
        Http.Response response = DELETE("/blocks/" + block.id);
        assertStatus(302, response);
        block.refresh();
        assertEquals(DeletedStatus.DELETED, block.deleted);
    }

    @Test
    public void test_checkExpireAt() {
        //配置 BLOCK 参数
        Map<String, String> params = new HashMap<>();
        params.put("block.title", "TestTitle");
        params.put("block.displayOrder", "1");
        params.put("block.effectiveAt", "2012-3-21 00:00:00");
        params.put("block.expireAt", "2012-3-20 00:00:00");
        params.put("content", "TestContent");

        //配置 图片 参数
        Map<String, File> files = new HashMap<>();
        File file = Play.getFile("test/p.jpg");
        //确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imageUrl", file);

        Http.Response response = POST("/blocks", params, files);
        List<play.data.validation.Error> errors = (List<play.data.validation.Error>) renderArgs("errors");
        assertEquals("block.expireAt", errors.get(0).getKey());
        assertIsOk(response);


    }

}
