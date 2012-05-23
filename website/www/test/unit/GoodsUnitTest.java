package unit;

import com.uhuila.common.constants.DeletedStatus;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.GoodsLevelPrice;
import models.sales.GoodsPublishedPlatformType;
import models.sales.GoodsStatus;
import models.sales.GoodsUnPublishedPlatform;
import models.sales.Shop;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 商品Model的单元测试.
 * <p/>
 * User: sujie
 * Date: 2/27/12
 * Time: 5:59 PM
 */
public class GoodsUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Supplier.class);
        Fixtures.loadModels("fixture/supplier_unit.yml");
        Fixtures.loadModels("fixture/areas_unit.yml");
        Fixtures.loadModels("fixture/categories_unit.yml");
        Fixtures.loadModels("fixture/brands_unit.yml");
        Fixtures.loadModels("fixture/shops_unit.yml");
        Fixtures.loadModels("fixture/goods_unit.yml");
        Fixtures.loadModels("fixture/orders.yml");
    }

    @Test
    public void testGetImageBySizeType() {
        models.sales.Goods goods = new Goods();
        goods.imagePath = "/1/1/1/3.jpg";
        String path = goods.getImageLargePath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/3_large.jpg", path);

        path = goods.getImageTinyPath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/3_tiny.jpg", path);

        path = goods.getImageMiddlePath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/3_middle.jpg", path);

        path = goods.getImageSmallPath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/3_small.jpg", path);
    }

    @Test
    public void testGetDiscountExpress() {
        models.sales.Goods goods = new Goods();
        goods.setDiscount(BigDecimal.TEN);
        assertEquals("无优惠", goods.getDiscountExpress());

        goods.setDiscount(new BigDecimal("9.8"));
        assertEquals("9.8折", goods.getDiscountExpress());

        goods.setDiscount(new BigDecimal("1"));
        assertEquals("1折", goods.getDiscountExpress());


        goods.setDiscount(new BigDecimal("12"));
        assertEquals("无优惠", goods.getDiscountExpress());

        goods.setDiscount(new BigDecimal("-1"));
        assertEquals("0折", goods.getDiscountExpress());

        goods.setDiscount(BigDecimal.ZERO);
        assertEquals("0折", goods.getDiscountExpress());
    }

    @Test
    public void testSetDiscount() {
        models.sales.Goods goods = new Goods();
        goods.setDiscount(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, goods.getDiscount());

        goods.setDiscount(new BigDecimal("9.8"));
        assertEquals(new BigDecimal("9.8"), goods.getDiscount());

        goods.setDiscount(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, goods.getDiscount());

        goods.setDiscount(new BigDecimal("-1"));
        assertEquals(BigDecimal.ZERO, goods.getDiscount());

        goods.setDiscount(new BigDecimal("100"));
        assertEquals(BigDecimal.TEN, goods.getDiscount());
    }

    @Test
    public void testGetDiscount() {
        models.sales.Goods goods = new Goods();
        goods.faceValue = new BigDecimal(100);
        goods.salePrice = new BigDecimal(100);
        assertEquals(BigDecimal.TEN, goods.getDiscount());

        goods.faceValue = new BigDecimal(200);
        goods = new Goods();
        goods.faceValue = new BigDecimal(100);
        goods.salePrice = new BigDecimal("1.00");
        assertEquals(new BigDecimal("0.10"), goods.getDiscount());

        goods = new Goods();
        goods.faceValue = new BigDecimal(100);
        goods.salePrice = new BigDecimal(10000);
        assertEquals(BigDecimal.TEN, goods.getDiscount());

        goods = new Goods();
        goods.faceValue = new BigDecimal(100);
        goods.salePrice = new BigDecimal(10);
        assertEquals(new BigDecimal("1.00"), goods.getDiscount());

        goods = new Goods();
        goods.faceValue = new BigDecimal(100);
        goods.salePrice = null;
        assertEquals(BigDecimal.ZERO, goods.getDiscount());

        goods = new Goods();
        goods.faceValue = null;
        goods.salePrice = new BigDecimal(100);
        assertEquals(BigDecimal.ZERO, goods.getDiscount());
    }

    /**
     * 测试各种查询条件都指定的情况.
     */
    @Test
    public void testFindByCondition() {
        String condition = "0-021-0-0-0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(condition);

        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByCondition
                (goodsCond, 1, 50);
        assertEquals(17, goodsPage.size());
        models.sales.Goods goods = goodsPage.get(0);
        goods.getDiscountExpress();
    }

    @Test
    public void testUpdateStatus() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        models.sales.Goods goods = Goods.findById(goodsId);
        goods.status = GoodsStatus.ONSALE;
        Goods.updateStatus(GoodsStatus.OFFSALE, goodsId);
        assertEquals(GoodsStatus.OFFSALE, goods.status);
    }

    @Test
    public void testDelete() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Goods.delete(goodsId);
        models.sales.Goods goods = Goods.findById(goodsId);
        assertEquals(DeletedStatus.DELETED, goods.deleted);

        Long errGoodsId = -1L;
        Goods.delete(errGoodsId);
        goods = Goods.findById(errGoodsId);
        assertNull(goods);
    }

    @Test
    public void testFindTopByCategory() {
        Long categoryId = (Long) Fixtures.idCache.get("models.sales.Category-Category_1");
        List<Goods> goodsList = Goods.findTopByCategory(categoryId, 1);
        assertEquals(1, goodsList.size());
        Set<Category> categories = goodsList.get(0).categories;
        assertEquals(categoryId, categories.iterator().next().id);
    }

    @Test
    public void testGetSupplierId() {
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier1");
        models.sales.Goods goods = new Goods();
        goods.supplierId = supplierId;
        Supplier supplier = goods.getSupplier();
        assertEquals("来一份", supplier.fullName);
    }

    @Test
    public void testSetLevelPrices() {
        models.sales.Goods goods = new Goods();
        BigDecimal[] prices = new BigDecimal[3];
        goods.originalPrice = BigDecimal.TEN;
        prices[0] = BigDecimal.ONE;
        prices[1] = BigDecimal.ONE;
        prices[2] = BigDecimal.ONE;
        goods.setLevelPrices(prices);
        List<GoodsLevelPrice> priceList = goods.getLevelPrices();
        assertEquals(4, priceList.size());
        assertEquals(1, priceList.get(0).price.intValue());
        assertEquals(1, priceList.get(1).price.intValue());
        assertEquals(1, priceList.get(2).price.intValue());
        assertEquals(0, priceList.get(3).price.intValue());
    }

    @Test
    public void testCreate() {
        models.sales.Goods goods = new Goods();
        goods.no = "11";
        goods.name = "test111";
        goods.faceValue = new BigDecimal(200);
        goods.create();
    }

    @Test
    public void testUpdate() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-Supplier2");
        models.sales.Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.no = "11";
        goods.name = "test111";
        goods.faceValue = new BigDecimal(200);
        goods.updatedBy = "sujie";
        goods.update(goodsId, goods);
        assertEquals("sujie", goods.updatedBy);
    }

    @Test
    public void testFindByResaleCondition() {
        Resaler resaler = new Resaler();
        resaler.level = ResalerLevel.VIP1;
        GoodsCondition condition = new GoodsCondition("0-0-0");
        JPAExtPaginator<Goods> goodsList = models.sales.Goods.findByResaleCondition(resaler, condition, 1, 10);
        assertEquals(17, goodsList.size());
    }

    @Test
    public void testGetResalePrice() {
        models.sales.Goods goods = new Goods();
        goods.faceValue = BigDecimal.TEN;
        goods.originalPrice = BigDecimal.ONE;
        BigDecimal resalePrice = goods.getResalePrice(models.resale.ResalerLevel.NORMAL);
        assertEquals(BigDecimal.ONE, resalePrice);
        BigDecimal[] prices = new BigDecimal[4];
        prices[0] = BigDecimal.ONE;
        prices[1] = BigDecimal.ONE;
        prices[2] = BigDecimal.ONE;
        goods.setLevelPrices(prices);
        assertEquals(2, goods.getResalePrice(ResalerLevel.VIP1).intValue());
    }

    @Test
    public void testPreview() throws IOException {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        models.sales.Goods goods = Goods.findById(goodsId);
        models.sales.Goods updatedGoods = new Goods();
        updatedGoods.name = "abcd";
        updatedGoods.imagePath = "abc.jpg";
        updatedGoods.salePrice = new BigDecimal(99);
        String uuid = Goods.preview(goodsId, updatedGoods, null, "/nfs/images/o");
        Goods cacheGoods = Goods.getPreviewGoods(uuid);
        assertEquals(goods.imagePath, cacheGoods.imagePath);
        assertEquals("abcd", cacheGoods.name);
        assertEquals(99, cacheGoods.salePrice.longValue());
    }

    @Test
    public void testGetLevelPriceArray() {
        models.sales.Goods goods = new Goods();
        BigDecimal[] prices = goods.getLevelPriceArray();
        assertEquals(ResalerLevel.values().length, prices.length);
        assertEquals(0, prices[0].intValue());
        assertEquals(0, prices[1].intValue());
        assertEquals(0, prices[2].intValue());
        assertEquals(0, prices[3].intValue());

        GoodsLevelPrice priceObj = new GoodsLevelPrice(goods, ResalerLevel.NORMAL, BigDecimal.TEN);
        List<GoodsLevelPrice> priceList = new ArrayList<>();
        priceList.add(priceObj);
        goods.setLevelPrices(priceList);
        BigDecimal[] updatedPrices = goods.getLevelPriceArray();
        assertEquals(10, updatedPrices[0].intValue());
        assertEquals(0, updatedPrices[1].intValue());
        assertEquals(0, updatedPrices[2].intValue());
        assertEquals(0, updatedPrices[3].intValue());
    }

    @Test
    public void testFindTradeGoodsRecently() {
        List<Goods> goodsList = Goods.findTradeRecently(3);
        assertEquals(1, goodsList.size());
        assertEquals("哈根达斯100元抵用券", goodsList.get(0).name);
    }

    @Test
    public void testAddRecommend() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        models.sales.Goods goods = Goods.findById(goodsId);
        Goods.addRecommend(goods, true);
        assertEquals(100, goods.recommend.intValue());

        Goods.addRecommend(goods, false);
        assertEquals(101, goods.recommend.intValue());
    }

    @Test
    public void testSetPublishedPlatform() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        models.sales.Goods goods = Goods.findById(goodsId);
        assertEquals(0, goods.unPublishedPlatforms.size());

        List<GoodsPublishedPlatformType> platforms = new ArrayList<>();
        platforms.add(GoodsPublishedPlatformType.DANGDANG);
        goods.setPublishedPlatforms(platforms);
        Goods.update(goodsId,goods);

        List<GoodsUnPublishedPlatform> unPublishedPlatforms = GoodsUnPublishedPlatform.find("goods.id is null").fetch();
        assertEquals(0, unPublishedPlatforms.size());

        models.sales.Goods goods2 = Goods.findById(goodsId);
        assertEquals(2, goods2.unPublishedPlatforms.size());

        platforms = new ArrayList<>();
        platforms.add(GoodsPublishedPlatformType.TAOBAO);
        platforms.add(GoodsPublishedPlatformType.YIHAODIAN);
        goods2.setPublishedPlatforms(platforms);
        Goods.update(goodsId,goods);

        goods2 = Goods.findById(goodsId);
        assertEquals(1, goods2.unPublishedPlatforms.size());

        unPublishedPlatforms = GoodsUnPublishedPlatform.find("goods.id is null")
                .fetch();
        assertEquals(0, unPublishedPlatforms.size());
    }
}
