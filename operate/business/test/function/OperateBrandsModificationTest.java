package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.sales.*;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-20
 * Time: 下午3:53
 * To change this template use File | Settings | File Templates.
 */

public class OperateBrandsModificationTest extends FunctionalTest {

    /**
     * 测试数据准备
     */
    @Before
    public void setup() {
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);

        Fixtures.delete(OperateUser.class);
        Fixtures.delete(OperateRole.class);

        Fixtures.loadModels("fixture/roles.yml");
        Fixtures.loadModels("fixture/supplierusers.yml");

        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");

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


    // 测试能否进入品牌页面
    @Test
    public void testBrandsDisplay() {
        Http.Response response = GET("/brands");
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    // 测试能否进入品牌编辑状态
    @Test
    public void testBrandsModificationRequest() {
        Long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");
        Http.Response response = GET("/brands/" + brandId + "/edit");
        assertIsOk(response);
        assertContentMatch("修改品牌", response);
    }

    // 测试能否修改商品信息
    @Test
    public void testBrandsModification() {
        Long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");
        Brand brand = Brand.findById(brandId);
        brand.save();

        Http.Response response = GET("/brands/" + brandId + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);

        String params = "brand.name=test&brand.displayOrder=0&brand.supplier=Supplier1&brand.logo=" +
                brand.logo + "&brand.deleted=" + brand.deleted + "brand.introduce=0";

        response = PUT("/brands/" + brandId, "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        assertContentMatch("test", response);
    }

    // 测试能否删除品牌信息
    @Test
    public void testBrandsDeletion() {
        Long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");

        Http.Response response = DELETE("/brands/" + brandId);
        assertStatus(302, response);
    }

    /**
     * 测试添加品牌
     */
    @Test
    public void testCreate() {
        Map<String, String> goodsParams = new HashMap<>();
        goodsParams.put("goods.name", "laiyifen1");
        goodsParams.put("goods.no", "20000000");
        goodsParams.put("goods.supplierId", "0");
        goodsParams.put("goods.title", "阿森发送发送分10元");
        goodsParams.put("goods.status", GoodsStatus.ONSALE.toString());
        goodsParams.put("goods.prompt", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        goodsParams.put("goods.details", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        goodsParams.put("goods.useWeekDay", "1,2,3,4,5,6,7");
        goodsParams.put("goods.promoterPrice", "2");
        goodsParams.put("goods.invitedUserPrice", "1");
//        goodsParams.put("goods.imagePath", "/opt/3.jpg");
        goodsParams.put("goods.deleted", DeletedStatus.DELETED.toString());
        goodsParams.put("goods.createdBy", "yanjy");
        Http.Response response = POST("/goods", goodsParams);
        response.setContentTypeIfNotSet("text/html; charset=GBK");
        assertStatus(200, response);
        List<Goods> list = Goods.findAll();
        assertNotNull(list);
    }

}
