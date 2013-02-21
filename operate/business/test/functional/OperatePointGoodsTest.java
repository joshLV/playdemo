package functional;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import models.operator.OperateUser;
import models.sales.PointGoods;
import operate.rbac.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import controllers.operate.cas.Security;
import factory.FactoryBoy;

/**
 */
public class OperatePointGoodsTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        OperateUser operateUser = FactoryBoy.create(OperateUser.class);

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(operateUser.loginName);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testCreate1() {
        FactoryBoy.create(PointGoods.class);
        int size = PointGoods.findAll().size();
        // 初始值
        assertEquals(1, size);

        // 配置 BLOCK 参数
        Map<String, String> params = new HashMap<>();
        params.put("pointGoods.name", " 测试积分商品6");
        params.put("pointGoods.effectiveAt", "2012-09-20");
        params.put("pointGoods.expireAt", "2012-09-29");
        params.put("pointGoods.faceValue", "2000");
        params.put("pointGoods.pointPrice", "10");
        params.put("pointGoods.details", "test测试积分商品6");
        params.put("pointGoods.baseSale", "10");
        params.put("pointGoods.status", "OFFSALE");
        params.put("pointGoods.limitNumber", "5");
        params.put("pointGoods.materialType", "ELECTRONIC");

        // 配置 图片 参数
        Map<String, File> files = new HashMap<>();
        File file = Play.getFile("test/creative.jpg");
        // /0/0/112/cat.jpg
        // 确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imagePath", file);

        Http.Response response = POST("/pointgoods", params, files);
        
        // FIXME: 不能提交通过..
        assertStatus(200, response);
        size = PointGoods.findAll().size();
        // 创建成功 size增加一条
        assertEquals(1, size);
    }

}
