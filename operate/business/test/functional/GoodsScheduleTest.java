package functional;

import com.uhuila.common.util.DateUtil;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.Goods;
import models.sales.GoodsSchedule;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-11
 * Time: 下午4:08
 */
public class GoodsScheduleTest extends FunctionalTest {
    GoodsSchedule goodsSchedule;

    /**
     * 测试数据准备
     */
    @Before
    public void setup() {
        // 重新加载配置文件
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        goodsSchedule = FactoryBoy.create(GoodsSchedule.class);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }


    // 测试添加
    @Test
    public void testCreate() {
        List<GoodsSchedule> list = GoodsSchedule.findAll();
        int count = list.size();
        Goods goods = FactoryBoy.create(Goods.class);
        Map<String, String> params = new HashMap<>();
        params.put("goodsSchedule.goods.id", goods.id.toString());
        params.put("goodsSchedule.effectiveAt", DateUtil.dateToString(new Date(), 0));
        params.put("goodsSchedule.expireAt", DateUtil.dateToString(new Date(), 30));
        Http.Response response = POST("/goods-schedule", params);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(302, response);
        list = GoodsSchedule.findAll();
        assertEquals(count + 1, list.size());
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/goods-schedule/new");
        assertStatus(200, response);
        assertContentType("text/html", response);
        assertContentMatch("添加商品排期", response);
    }

    // 测试能否进入排期页面
    @Test
    public void testDisplay() {
        Http.Response response = GET("/goods-schedule");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertNotNull(renderArgs("goodsPage"));
    }

    // 测试能否修改排期商品信息
    @Test
    public void testEdit() {
        Http.Response response = GET("/goods-schedule/" + goodsSchedule.id + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertContentMatch("修改商品排期", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertEquals(goodsSchedule, (GoodsSchedule) renderArgs("goodsSchedule"));
        assertEquals("商品名：" + goodsSchedule.goods.shortName, (String) renderArgs("goodsName"));
    }

    @Test
    public void testGetName() {
        Goods goods = FactoryBoy.create(Goods.class);
        Http.Response response = GET("/goods-name/" + goods.id);
        assertStatus(200, response);
        assertContentMatch("Product Name", response);
    }

    @Test
    public void testDelete() {
        assertEquals(1, GoodsSchedule.count());
        Http.Response response = DELETE("/goods-schedule/" + goodsSchedule.id);
        assertStatus(302, response);
        assertEquals(0, GoodsSchedule.count());
    }
}
