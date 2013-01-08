package functional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import navigation.RbacLoader;
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
 * Date: 13-1-7
 * Time: 下午12:02
 */
public class UploadFilesTest extends FunctionalTest {

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
        // Play.configuration.setProperty("upload.imagepath", "/tmp");
        FactoryBoy.deleteAll();
        // f重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        SupplierUser user = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
    @Test
    public void testUploadImageWithOutFile() throws Exception {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/pic.jpg");
        Map<String, String> params = new HashMap<>();
        Map<String, File> fileParams = new HashMap<>();
        Http.Response response = POST(Router.reverse("UploadFiles.uploadImage").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(1, jsonObject.get("error").getAsInt());
    }

    @Test
    public void testUploadImage() throws Exception {
        VirtualFile vfImage = VirtualFile.fromRelativePath("test/pic.jpg");
        Map<String, String> params = new HashMap<>();
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("imgFile", vfImage.getRealFile());
        Http.Response response = POST(Router.reverse("UploadFiles.uploadImage").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json2=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(0, jsonObject.get("error").getAsInt());
        assertNotNull(jsonObject.get("url").getAsString());
    }
}
