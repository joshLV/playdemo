package function;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import models.admin.OperateRole;
import models.admin.OperateUser;
import models.sales.*;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;

public class OperateGoodsTest extends FunctionalTest {

    @org.junit.Before
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

    /**
     * 查看商品信息
     */
    @Test
    public void testDetails() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");

        Response response = GET("/goods/" + goodsId);
        assertIsOk(response);
        assertContentType("text/html", response);
    }

    /**
     * 添加商品信息
     */
    // 同样存在 POST 图片文件 在测试环境 接受不了的问题，跳过
    @Test
    @Ignore
    public void testCreate() {
        // TODO

    }

    /**
     * 删除商品信息
     */
    @Test
    public void testDelete() {
        long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");

        Response response = DELETE("/goods/" + goodsId);
        assertStatus(302, response);

        //修改商品状态为下架状态
        response = PUT("/goods/" + goodsId + "/offSale", "text/html", "");

        Goods goods0 = Goods.findById(goodsId);
        goods0.refresh();
        assertEquals(GoodsStatus.OFFSALE, goods0.status);
        
        
        //再次删除
        response = DELETE("/goods/" + goodsId);
        assertStatus(302, response);

        //验证状态改为已删除状态
        Goods goods1 = Goods.findById(goodsId);
        goods1.refresh();
        assertEquals(DeletedStatus.DELETED, goods1.deleted);
    }

    /**
     * 修改商品上下架
     */
    @Test
    public void testOffSale() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        Response response = PUT("/goods/" + goodsId + "/onSale", "text/html", "");
        assertStatus(200, response);
        goods = Goods.findById(goodsId);
        goods.refresh();
        assertEquals(GoodsStatus.ONSALE, goods.status);
        
        response = PUT("/goods/" + goodsId + "/offSale", "text/html", "");
        assertStatus(302, response);
        goods = Goods.findById(goodsId);
        goods.refresh();
        assertEquals(GoodsStatus.OFFSALE, goods.status);
    }

    /**
     * 修改商品信息
     */
    @Test
    public void testEdit() {
        /*
        Long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_1");
        Long categoryId = (Long) Fixtures.idCache.get("models.sales.Category-Category_1");
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods goods = Goods.findById(goodsId);
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        goods.supplierId = supplierId;
        goods.save();
        Long shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_5");
        Shop shop = Shop.findById(shopId);
        shop.supplierId = supplierId;
        shop.save();

        Response response = GET("/goods/" + goodsId + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);

        String params = "goods.supplierId=" + supplierId + "&goods.name=test123&goods.title=test123&goods.faceValue=120&goods" +
                ".originalPrice=120&goods.details=abcdefgh&goods.salePrice=123&goods.categories.id=" +
                categoryId + "&goods.expireAt=2015-12-12&goods.effectiveAt=2012-03-12&goods.baseSale=1000" +
                "&levelPrices=1&goods.brand.id=" + brandId;
        response = PUT("/goods/" + goodsId, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        Goods updateGoods = Goods.findById(goodsId);
        assertEquals("test123", updateGoods.name);
        */

        // 将要更新成的目标ID
        Long targetId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");
        Goods targetGood = Goods.findById(targetId);
        // 需要更新的原始ID
        Long baseId = (Long) Fixtures.idCache.get("models.sales" +
                ".Goods-Goods_001");
        // 获取categories的ID
        Long cateId = (Long) Fixtures.idCache.get("models.sales.Category-Category_1");
        Long cateId2 = (Long) Fixtures.idCache.get("models.sales.Category-Category_2");
        System.out.println("Cate ID ===================  " + cateId.toString());
        System.out.println("Cate ID 2 ===================  " + cateId2.toString());
        // 连接到原始商品更新页面
        Http.Response response = GET("/goods/" + baseId + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);


        // 生产更新参数
        String params = "goods.name=testName&goods.no=001" +
                "&goods.supplierId=" + targetGood.supplierId +
                "&goods.originalPrice=100" +
                "&goods.salePrice=200" +
                "&goods.title=" + targetGood.title +
                "&levelPrices=4" +
                "&levelPrices=3" +
                "&levelPrices=2" +
                "&levelPrices=1" +
                "&goods.categories[].id=" + cateId +
                "&goods.categories[].id=" + cateId2 +
                "&goods.effectiveAt=2012-02-28T14:41:33" +
                "&goods.expireAt=2015-02-28T14:41:33" +
                "&goods.baseSale=" + targetGood.baseSale +
                "&goods.details= AAAAAAAAAAAAA" +
                "&goods.faceValue=1000" +
                "&goods.brand.id=" + targetGood.brand.id.toString();
        response = PUT("/goods/" + baseId, "application/x-www-form-urlencoded", params);
        // 更新响应正确
        assertStatus(302, response);
        // 获取更新后的商品信息
        Goods updatedGoods = Goods.findById(baseId);
        // 测试更新信息是否正确
        assertEquals("testName", updatedGoods.name);
        assertEquals("001", updatedGoods.no);
        assertEquals(targetGood.supplierId, updatedGoods.supplierId);
        assertEquals(0, updatedGoods.originalPrice.compareTo(new BigDecimal("100")));
        assertEquals(0, new BigDecimal("200").compareTo(updatedGoods.salePrice));
        assertEquals(targetGood.baseSale, updatedGoods.baseSale);
        assertEquals(targetGood.brand.id, updatedGoods.brand.id);

    }
}