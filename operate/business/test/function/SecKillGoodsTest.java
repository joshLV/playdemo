package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.admin.OperateUser;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.PointGoods;
import models.sales.SecKillGoods;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.db.jpa.JPAPlugin;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import factory.callback.SequenceCallback;
import factory.callback.BuildCallback;
import factory.FactoryBoy;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-21
 * Time: 下午3:34
 * To change this template use File | Settings | File Templates.
 */
public class SecKillGoodsTest extends FunctionalTest {

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


    @Test
    public void testIndex() {
        Http.Response response = GET("/seckill_goods");
        assertStatus(200, response);
        assertContentMatch("秒杀活动一览",response);

    }


    @Test
    public void testAdd() {
        Http.Response response = GET("/seckill_goods/new");
        assertStatus(200, response);
        assertContentMatch("添加秒杀活动",response);

    }

    @Test
    public void testCreate() {
        Map<String,String> params = new HashMap<>();

        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);

        params.put("secKillGoods.goods.id",goods.goods.id.toString());

        params.put("secKillGoods.goods.name", goods.goods.name);
        params.put("secKillGoods.personLimitNumber",goods.personLimitNumber.toString());
        params.put("secKillGoods.createdAt",goods.createdAt.toString());

        //配置 图片 参数
        Map<String,File> files = new HashMap<>();
        File file = Play.getFile("test/creative.jpg");

        //确认正确获得图片路径
        assertTrue(file.exists());
        files.put("imagePath", file);

        Http.Response response = POST("/seckill_goods",params,files);


        assertStatus(302, response);


    }


    @Test
    public void testEdit() {
         SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
         Http.Response response = GET("/seckill_goods/"+goods.id+"/edit");
         assertStatus(200, response);

    }


    @Test
    public void testUpdate() {

        final SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);

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
