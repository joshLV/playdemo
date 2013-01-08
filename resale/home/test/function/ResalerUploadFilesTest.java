package function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.Resaler;
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
 * Date: 13-1-8
 * Time: 下午5:36
 */
public class ResalerUploadFilesTest extends FunctionalTest {

    private Resaler resaler;

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
        resaler = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(resaler.loginName);
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
        Http.Response response = POST(Router.reverse("ResalerUploadFiles.uploadJson").url, params, fileParams);
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
        Http.Response response = POST(Router.reverse("ResalerUploadFiles.uploadJson").url, params, fileParams);
        assertIsOk(response);

        String jsonString = getContent(response);
        System.out.println("json2=" + jsonString);

        JsonElement jsonElement = new JsonParser().parse(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        assertEquals(0, jsonObject.get("error").getAsInt());
        assertNotNull(jsonObject.get("url").getAsString());
    }
}
