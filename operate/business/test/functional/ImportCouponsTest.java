package functional;

import controllers.ImportCoupons;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.operator.OperateUser;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.ImportedCoupon;
import models.sales.ImportedCouponStatus;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-1-6
 * Time: 上午11:38
 */
public class ImportCouponsTest extends FunctionalTest {

    Goods goods;

    ImportedCoupon importedCoupon;

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

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override 
            public void build(Goods g) {
                g.couponType = GoodsCouponType.IMPORT;
                g.cumulativeStocks = 0l;
            }
        });

        importedCoupon = FactoryBoy.create(ImportedCoupon.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() throws Exception {
        Http.Response response = GET("/import-coupons");
        assertIsOk(response);
        List<Goods> goodsList = (List<Goods>) renderArgs("goodsList");
        assertEquals(1, goodsList.size());
    }

    @Test
    public void 追加方式上传() throws Exception {
        VirtualFile dataFile = VirtualFile.fromRelativePath("test/data/import-coupons.txt");
        assertTrue(dataFile.exists());
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        params.put("action", "append");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("couponfile", dataFile.getRealFile());
        Http.Response response = POST("/import-coupons", params, fileParams);
        assertStatus(302, response);

        goods.refresh();
        assertEquals(new Long(2), goods.cumulativeStocks);
        assertEquals(3, ImportedCoupon.count());
    }

    @Test
    public void 覆盖上传时删除未使用的券() throws Exception {
        VirtualFile dataFile = VirtualFile.fromRelativePath("test/data/import-coupons.txt");
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        params.put("action", "overwrite");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("couponfile", dataFile.getRealFile());
        Http.Response response = POST("/import-coupons", params, fileParams);
        assertStatus(302, response);

        goods.refresh();
        assertEquals(new Long(2), goods.cumulativeStocks);
        assertEquals(2, ImportedCoupon.count());
    }

    @Test
    public void 覆盖上传时不会删除已使用的券() throws Exception {
        FactoryBoy.create(ImportedCoupon.class, new BuildCallback<ImportedCoupon>() {
            @Override
            public void build(ImportedCoupon target) {
                target.status = ImportedCouponStatus.USED;
            }
        });

        VirtualFile dataFile = VirtualFile.fromRelativePath("test/data/import-coupons.txt");
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        params.put("action", "overwrite");
        Map<String, File> fileParams = new HashMap<>();
        fileParams.put("couponfile", dataFile.getRealFile());
        Http.Response response = POST("/import-coupons", params, fileParams);
        assertStatus(302, response);

        goods.refresh();
        assertEquals(new Long(2), goods.cumulativeStocks);
        assertEquals(3, ImportedCoupon.count());
    }

    @Test
    public void 无商品ID() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "append");
        Http.Response response = POST("/import-coupons", params);
        assertStatus(302, response);
    }

    @Test
    public void 无Action() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        Http.Response response = POST("/import-coupons", params);
        assertStatus(302, response);
    }

    @Test
    public void 无上传文件() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", goods.id.toString());
        params.put("action", "append");
        Http.Response response = POST("/import-coupons", params);
        assertStatus(302, response);
    }

    @Test
    public void testInstance() throws Exception {
        assertTrue((new ImportCoupons()) instanceof Controller);
    }

}
