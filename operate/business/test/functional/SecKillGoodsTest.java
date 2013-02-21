package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.SecKillGoods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-8-21
 * Time: 下午3:34
 */
public class SecKillGoodsTest extends FunctionalTest {

    @BeforeClass
    public static void setUpClass() {
        Play.tmpDir = new File("/tmp");
    }

    @AfterClass
    public static void tearDownClass() {
        Play.tmpDir = null;
    }

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @After
    public void tearDown() throws Exception {
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/seckill_goods");
        assertStatus(200, response);
        assertContentMatch("秒杀活动一览", response);

    }


    @Test
    public void testAdd() {
        Http.Response response = GET("/seckill_goods/new");
        assertStatus(200, response);
        assertContentMatch("添加秒杀活动", response);

    }

    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();

        SecKillGoods goods = FactoryBoy.build(SecKillGoods.class);

        params.put("secKillGoods.goods.id", goods.goods.id.toString());

        params.put("secKillGoods.goods.name", goods.goods.name);
        params.put("secKillGoods.personLimitNumber", goods.personLimitNumber.toString());
        params.put("secKillGoods.createdAt", "2012-12-15 23:50");

        //配置 图片 参数
        Map<String, File> files = new HashMap<>();
        File file = Play.getFile("test/creative.jpg");

        //确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imagePath", file);

        Http.Response response = POST("/seckill_goods", params, files);


        assertStatus(302, response);

        assertEquals(1, SecKillGoods.count());

    }


    @Test
    public void testEdit() {
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
        Http.Response response = GET("/seckill_goods/" + goods.id + "/edit");
        assertStatus(200, response);

    }


    @Test
    public void testUpdate() {

        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);

        Http.Response response = PUT("/seckill_goods/" + goods.id, "text/html", "");

        assertStatus(302, response);


    }


    @Test
    public void testDelete() {
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);

        Http.Response response = DELETE("/seckill_goods/" + goods.id);
        assertStatus(302, response);


    }


}
