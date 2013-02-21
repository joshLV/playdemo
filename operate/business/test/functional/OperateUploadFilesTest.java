package functional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.Goods;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-1-9
 * Time: 上午11:10
 */
public class OperateUploadFilesTest extends FunctionalTest {
    Goods goods;

    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp");
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
    }

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goods = FactoryBoy.create(Goods.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
    @Test
    public void testUploadJsonWithOutFile() throws Exception {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/leaves.jpg");
        Map<String, String> params = new HashMap<>();
        Map<String, File> fileParams = new HashMap<>();
        Http.Response response = POST(Router.reverse("OperateUploadFiles.uploadJson").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(1, jsonObject.get("error").getAsInt());
    }

    @Test
    public void testUploadJson() throws Exception {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/leaves.jpg");
        Map<String, String> params = new HashMap<>();
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("imgFile", vfImage.getRealFile());
        Http.Response response = POST(Router.reverse("OperateUploadFiles.uploadJson").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json2=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(0, jsonObject.get("error").getAsInt());
        assertNotNull(jsonObject.get("url").getAsString());
    }


    @Test
    public void testUploadImageWithOutFile() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        Map<String, File> fileParams = new HashMap<>();
        Http.Response response = POST(Router.reverse("OperateUploadFiles.uploadImages").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(1, jsonObject.get("error").getAsInt());
    }

    @Test
    public void testUploadImageWithGoodsId() throws Exception {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/leaves.jpg");
        Map<String, String> params = new HashMap<>();
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("imgFile", vfImage.getRealFile());
        Http.Response response = POST(Router.reverse("OperateUploadFiles.uploadImages").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(1, jsonObject.get("error").getAsInt());
    }

    @Test
    public void testUploadImage() throws Exception {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/leaves.jpg");
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("imgFile", vfImage.getRealFile());
        Http.Response response = POST(Router.reverse("OperateUploadFiles.uploadImages").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json2=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(0, jsonObject.get("error").getAsInt());
        assertNotNull(jsonObject.get("url").getAsString());
    }
}
