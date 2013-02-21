package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.GoodsImages;
import models.sales.GoodsStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.*;
import java.util.HashMap;

public class OperateGoodsTest extends FunctionalTest {
    Goods goods;
    Shop shop;
    Supplier supplier;
    Brand brand;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        // f重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goods = FactoryBoy.create(Goods.class);
        shop = FactoryBoy.create(Shop.class);
        supplier = FactoryBoy.create(Supplier.class);
        brand = FactoryBoy.create(Brand.class);
        goods.supplierId = supplier.id;
        goods.brand = brand;
        goods.beginOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(goods.effectiveAt, 5));
        goods.endOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(goods.expireAt, 5));

        goods.save();
        shop.supplierId = supplier.id;
        shop.save();
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
        Response response = GET("/goods/" + goods.id);
        assertIsOk(response);
        assertContentType("text/html", response);
        Goods goods = (Goods) renderArgs("goods");
        assertTrue(goods.name.contains("Product Name "));
    }

    /**
     * 添加商品信息
     */
    // 同样存在 POST 图片文件 在测试环境 接受不了的问题，跳过
    @Test
    public void testCreate() {
        // TODO
    }

    /**
     * 删除商品信息
     */
    @Test
    public void testDelete() {

        goods.status = GoodsStatus.OFFSALE;
        goods.save();
        Response response = DELETE("/goods/" + goods.id);
        assertStatus(302, response);

        //验证状态改为已删除状态
        goods.refresh();
        assertEquals(DeletedStatus.DELETED, goods.deleted);
    }

    /**
     * 修改商品上架
     */
    @Test
    public void testONSale() {
        List<GoodsHistory> history1 = GoodsHistory.find("goodsId=?", goods.id).fetch();
        int cnt = history1.size();
        assertEquals(0, cnt);
        goods.status = GoodsStatus.OFFSALE;
        goods.save();
        assertEquals(GoodsStatus.OFFSALE, goods.status);
        Response response = PUT("/goods/" + goods.id + "/onSale", "text/html", "");
        assertStatus(302, response);
        goods.refresh();
        assertEquals(GoodsStatus.ONSALE, goods.status);
        List<GoodsHistory> history = GoodsHistory.find("goodsId=?", goods.id).fetch();
        assertEquals(cnt + 1, history.size());
    }

    /**
     * 商品操作历史
     */
    @Test
    public void testGoodsHistory() {
        GoodsHistory history = FactoryBoy.create(GoodsHistory.class);
        history.goodsId = goods.id;
        history.save();
        Response response = GET("/goods/" + goods.id + "/histories");
        assertStatus(200, response);
        String goodsName = (String) renderArgs("goodsName");
        String goodsNo = (String) renderArgs("goodsNo");
        assertEquals(goods.name, goodsName);
        assertEquals(goods.no, goodsNo);
    }

    /**
     * 修改商品下架
     */
    @Test
    public void testOFFSale() {
        List<GoodsHistory> history1 = GoodsHistory.find("goodsId=?", goods.id).fetch();
        int cnt = history1.size();
        assertEquals(0, cnt);
        goods.status = GoodsStatus.ONSALE;
        goods.save();
        assertEquals(GoodsStatus.ONSALE, goods.status);
        Response response = PUT("/goods/" + goods.id + "/offSale", "text/html", "");
        assertStatus(302, response);
        goods.refresh();
        assertEquals(GoodsStatus.OFFSALE, goods.status);
        List<GoodsHistory> history = GoodsHistory.find("goodsId=?", goods.id).fetch();
        assertEquals(cnt + 1, history.size());
    }

    /**
     * 测试拒绝上架
     */
    @Test
    public void testReject() {
        List<GoodsHistory> history1 = GoodsHistory.find("goodsId=?", goods.id).fetch();
        int cnt = history1.size();
        assertEquals(0, cnt);
        goods.status = GoodsStatus.ONSALE;
        goods.save();
        assertEquals(GoodsStatus.ONSALE, goods.status);
        Response response = PUT("/goods/" + goods.id + "/reject", "text/html", "");
        assertStatus(302, response);
        goods.refresh();
        assertEquals(GoodsStatus.REJECT, goods.status);
        List<GoodsHistory> history = GoodsHistory.find("goodsId=?", goods.id).fetch();
        assertEquals(cnt + 1, history.size());
    }

    /**
     * 设置精选
     */
    @Test
    public void testpriority() {
        List<GoodsHistory> history1 = GoodsHistory.find("goodsId=?", goods.id).fetch();
        int cnt = history1.size();
        assertEquals(0, cnt);
        Response response = PUT("/goods/" + goods.id + "/priority", "text/html", "");
        assertStatus(302, response);

        Goods goods1 = Goods.findById(goods.id);
        goods1.refresh();
        assertEquals(goods.keywords, goods1.keywords);
        assertEquals(goods.priority, goods1.priority);
        List<GoodsHistory> history = GoodsHistory.find("goodsId=?", goods.id).fetch();
        assertEquals(cnt + 1, history.size());
    }

    @Test
    public void testEdit() {
        Response response = GET("/goods/" + goods.id + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertTrue(goods.name.contains("Product Name "));
    }

    @Test
    public void testEdit2() {
        Response response = GET("/goods/" + goods.id + "/edit");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertTrue(goods.name.contains("Product Name "));
        assertEquals("prompt", goods.getPrompt());
        assertEquals("detail", goods.getDetails());
        assertEquals("des", goods.getSupplierDes());
        assertEquals("exhib", goods.getExhibition());
    }

    @Test
    public void testCopy() {
        Response response = GET("/goods/" + goods.id + "/copy");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(Play.defaultWebEncoding, response);
        assertTrue(goods.name.contains("Product Name "));
        assertEquals("prompt", goods.getPrompt());
        assertEquals("detail", goods.getDetails());
        assertEquals("des", goods.getSupplierDes());
        assertEquals("exhib", goods.getExhibition());
    }

    /**
     * 修改商品信息
     */
    @Test
    public void testUpdate() {
        // 获取categories的ID
        Category category = FactoryBoy.create(Category.class);
        Long cateId = category.id;
        // 生产更新参数
        goods.refresh();
        String params = "goods.name=testName&goods.no=001" +
                "&goods.supplierId=" + goods.supplierId +
                "&goods.originalPrice=100" +
                "&goods.salePrice=200" +
                "&goods.shortName=test1" +
                "&goods.details=test2222222222" +
                "&goods.title=title" +
                "&goods.exhibition=test311111111" +
                "&goods.supplierDes=des111111111" +
                "&goods.prompt=prompt111111" +
                "&goods.useWeekDay=1" +
                "&goods.categories[].id=" + cateId +
                "&goods.effectiveAt=" + goods.effectiveAt +
                "&goods.expireAt=" + goods.expireAt +
                "&goods.cumulativeStocks=100" +
                "&goods.faceValue=1000" +
                "&goods.brand.id=" + goods.brand.id.toString() +
                "&goods.beginOnSaleAt=" + goods.beginOnSaleAt +
                "&goods.endOnSaleAt=" + goods.endOnSaleAt;
        Response response = PUT("/goods/" + goods.id, "application/x-www-form-urlencoded", params);
        // 更新响应正确
        assertStatus(302, response);

        goods.refresh();
        // 测试更新信息是否正确
        assertEquals("testName", goods.name);
        assertEquals("title", goods.title);
        assertEquals("test2222222222", goods.getDetails());
        assertEquals("001", goods.no);
        assertEquals(goods.supplierId, goods.supplierId);
        assertEquals(0, goods.originalPrice.compareTo(new BigDecimal("100")));
        assertEquals(0, new BigDecimal("200").compareTo(goods.salePrice));
        assertEquals(Long.valueOf("100"), goods.cumulativeStocks);
    }

    /**
     * 修改商品信息
     */

    @Test
    public void testUpdate2() {
        // 获取categories的ID
        Category category = FactoryBoy.create(Category.class);
        Long cateId = category.id;
        // 生产更新参数
        goods.refresh();
        String params = "goods.name=testName&goods.no=001" +
                "&goods.supplierId=" + goods.supplierId +
                "&goods.originalPrice=100" +
                "&goods.salePrice=200" +
                "&goods.shortName=test1" +
                "&goods.details=test2222222222" +
                "&goods.title=title" +
                "&goods.exhibition=test311111111" +
                "&goods.supplierDes=des111111111" +
                "&goods.prompt=prompt111111" +
                "&goods.useWeekDay=1" +
                "&goods.categories[].id=" + cateId +
                "&goods.effectiveAt=" + goods.effectiveAt +
                "&goods.expireAt=" + goods.expireAt +
                "&goods.cumulativeStocks=100" +
                "&goods.faceValue=1000" +
                "&goods.brand.id=" + goods.brand.id.toString() +
                "&goods.beginOnSaleAt=" + goods.beginOnSaleAt +
                "&goods.endOnSaleAt=" + goods.endOnSaleAt;
        Response response = PUT("/goods2/" + goods.id, "application/x-www-form-urlencoded", params);
        // 更新响应正确
        assertStatus(302, response);

        goods.refresh();
        // 测试更新信息是否正确
        assertEquals("testName", goods.name);
        assertEquals("title", goods.title);
        assertEquals("test2222222222", goods.getDetails());
        assertEquals("001", goods.no);
        assertEquals("test311111111", goods.getExhibition());
        assertEquals("des111111111", goods.getSupplierDes());
        assertEquals(0, goods.originalPrice.compareTo(new BigDecimal("100")));
        assertEquals(0, new BigDecimal("200").compareTo(goods.salePrice));
        assertEquals(Long.valueOf("100"), goods.cumulativeStocks);

    }

    @Test
    public void testIndexOrderBy() {
        Http.Response response = GET("/?condition.supplierId=0&condition.name=&condition.no=&condition.brandId=0&condition.status=ONSALE&c" +
                "ondition.salePriceBegin=&condition.salePriceEnd=&condition.saleCountBegin=&condition.saleCountEnd=&desc=10000000000");
        assertIsOk(response);
        assertNotNull(renderArgs("goodsPage"));
        assertNotNull(renderArgs("desc"));
    }

    /**
     * 设为首页展示
     */
    @Test
    public void testSetImages() {
        GoodsImages images = FactoryBoy.create(GoodsImages.class);
        images.isDisplaySite = false;
        images.goods = goods;
        images.save();
        assertFalse(images.isDisplaySite);

        Map<String, String> params = new HashMap<>();
        params.put("id", images.id.toString());
        Http.Response response = POST("/goods_images/" + images.id + "?goodsId=" + goods.id);
        images.refresh();
        assertEquals("", response.out.toString());
        assertTrue(images.isDisplaySite);

    }

}
