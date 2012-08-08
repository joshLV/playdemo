package function;

import controllers.operate.cas.Security;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.sales.PointGoods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-8
 * Time: 上午10:58
 * To change this template use File | Settings | File Templates.
 */
public class OperatePointGoodsTest extends FunctionalTest {


    @org.junit.Before
    public void setup() {

        Fixtures.delete(PointGoods.class);


        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);
        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/supplierusers.yml");


        Fixtures.loadModels("fixture/pointGoods.yml");

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
    public void testCreate() {
        int size = PointGoods.findAll().size();
        // 初始值
        assertEquals(5,size);

        //配置 BLOCK 参数
        Map<String,String> params = new HashMap<>();
        params.put("pointGoods.name"," 测试积分商品6");
        params.put("pointGoods.effectiveAt","2012-09-20");
        params.put("pointGoods.expireAt","2012-09-29");
        params.put("pointGoods.faceValue","2000");
        params.put("pointGoods.pointPrice","10");
        params.put("pointGoods.details","test测试积分商品6");
        params.put("pointGoods.baseSale","10");
        params.put("pointGoods.status","OFFSALE");
        params.put("pointGoods.limitNumber","5");
        params.put("pointGoods.materialType","ELECTRONIC");



        //配置 图片 参数
        Map<String,File> files = new HashMap<>();
        File file = Play.getFile("test/creative.jpg");
        ///0/0/112/cat.jpg
        //确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imagePath", file);

        Http.Response response = POST("/pointgoods",params,files);
        size = PointGoods.findAll().size();
        // 创建成功 size增加一条
        assertEquals(6,size);

    }

}
