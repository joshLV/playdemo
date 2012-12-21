package unit;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import factory.FactoryBoy;
import factory.asserts.Callback;
import factory.asserts.ModelAssert;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.order.OrderItems;
import models.resale.Resaler;
import models.resale.ResalerLevel;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.GoodsPublishedPlatformType;
import models.sales.GoodsStatus;
import models.sales.GoodsUnPublishedPlatform;
import models.sales.MaterialType;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 商品Model的单元测试.
 * <p/>
 * User: sujie
 * Date: 2/27/12
 * Time: 5:59 PM
 */
public class WebGoodsUnitTest extends UnitTest {
    Goods goods;
    Category category;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        category = FactoryBoy.create(Category.class);
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.title = "哈根达斯100元抵用券";
                g.name = "哈根达斯100元抵用券";
                g.beginOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(g.effectiveAt, 5));
                g.endOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(g.expireAt, 5));
            }
        });
    }

    @Test
    public void testGetImageBySizeType() {
        models.sales.Goods goods = new Goods();
        goods.imagePath = "/1/1/1/3.jpg";
        String path = goods.getImageLargePath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/e11a9fe9_3_340x260.jpg", path);

        path = goods.getImageTinyPath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/8e3e76ba_3_60x46_nw.jpg", path);

        path = goods.getImageMiddlePath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/11df989a_3_234x178.jpg", path);

        path = goods.getImageSmallPath();
        assertEquals("http://img0.dev.uhcdn.com/p/1/1/1/69310e25_3_172x132.jpg", path);
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

        goods.setDiscount(new BigDecimal("8.00"));
        assertEquals("8折", goods.getDiscountExpress());

        goods.setDiscount(new BigDecimal("9.88"));
        assertEquals("9.9折", goods.getDiscountExpress());

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
        FactoryBoy.batchCreate(20, Goods.class, new SequenceCallback<Goods>() {
            @Override
            public void sequence(Goods g, int seq) {
                g.name = "G_" + seq;
                g.materialType = MaterialType.REAL;
            }
        });
        String condition = "0-021-0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(condition);

        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByCondition
                (goodsCond, 1, 50);
        assertEquals(21, goodsPage.size());
    }


    @Test
    public void 测试过滤掉隐藏上架的商品() {
        FactoryBoy.batchCreate(20, Goods.class, new SequenceCallback<Goods>() {
            @Override
            public void sequence(Goods g, int seq) {
                g.name = "G_" + seq;
                g.materialType = MaterialType.REAL;
                g.isHideOnsale = true;
            }
        });
        String condition = "0-021-0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(condition);
        goodsCond.isHideOnsale = true;

        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByCondition
                (goodsCond, 1, 50);
        assertEquals(1, goodsPage.size());
    }

    @Test
    public void 测试不过滤掉隐藏上架的商品() {
        FactoryBoy.batchCreate(20, Goods.class, new SequenceCallback<Goods>() {
            @Override
            public void sequence(Goods g, int seq) {
                g.name = "G_" + seq;
                g.materialType = MaterialType.REAL;
                g.isHideOnsale = true;
            }
        });
        String condition = "0-021-0-0-0-0-1";
        GoodsCondition goodsCond = new GoodsCondition(condition);
        goodsCond.isHideOnsale = false;

        JPAExtPaginator<Goods> goodsPage = models.sales.Goods.findByCondition
                (goodsCond, 1, 50);
        assertEquals(21, goodsPage.size());
    }

    @Test
    public void testUpdateStatus() {
        goods.status = GoodsStatus.ONSALE;
        Goods.updateStatus(GoodsStatus.OFFSALE, goods.id);
        assertEquals(GoodsStatus.OFFSALE, goods.status);
    }

    @Test
    public void testDelete() {
        Goods.delete(goods.id);
        models.sales.Goods g = Goods.findById(goods.id);
        assertEquals(DeletedStatus.DELETED, g.deleted);

        Long errGoodsId = -1L;
        Goods.delete(errGoodsId);
        g = Goods.findById(errGoodsId);
        assertNull(g);
    }

    @Test
    public void testFindTopByCategory() {
        List<Goods> goodsList = Goods.findTopByCategory(category.id, 1);
        assertEquals(1, goodsList.size());
        Set<Category> categories = goodsList.get(0).categories;
        assertEquals(category.id, categories.iterator().next().id);
    }

    @Test
    public void testGetSupplierId() {
        Supplier supplier = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier s) {
                s.fullName = "上海一百食品";
            }
        });
        goods.supplierId = supplier.id;
        goods.save();
        assertEquals(supplier.fullName, goods.getSupplier().fullName);
    }

    @Test
    public void testCreate() throws Exception {
        ModelAssert.assertDifference(Goods.class, 1, new Callback() {
            @Override
            public void run() {
                Goods g = FactoryBoy.build(Goods.class);
                g.no = "11";
                g.expireAt = new Date();
                g.create();
                assertEquals(DateUtil.getEndOfDay(new Date()), g.expireAt);
            }

        });
    }

    @Test
    public void testUpdate() {
        models.sales.Goods form = FactoryBoy.build(Goods.class);
        form.no = "11";
        form.name = "test111";
        form.faceValue = new BigDecimal(200);
        form.updatedBy = "sujie";
        form.update(goods.id, form);
        goods.refresh();
        assertEquals("sujie", goods.updatedBy);
    }

    @Test
    public void testFindByResaleCondition() {
        Resaler resaler = new Resaler();
        resaler.level = ResalerLevel.NORMAL;
        GoodsCondition condition = new GoodsCondition("0-0-0");
        JPAExtPaginator<Goods> goodsList = models.sales.Goods.findByResaleCondition(resaler, condition, 1, 10);
        assertEquals(1, goodsList.size());
    }

    @Test
    public void testGetResalePrice() {
        models.sales.Goods goods = new Goods();
        goods.salePrice = BigDecimal.TEN;
        goods.originalPrice = BigDecimal.ONE;
        BigDecimal resalePrice = goods.getResalePrice();
        assertEquals(BigDecimal.TEN, resalePrice);

        goods.resaleAddPrice = BigDecimal.ONE;
        resalePrice = goods.getResalePrice();
        assertEquals(new BigDecimal("2"), resalePrice);

    }

    @Test
    public void testPreview() throws IOException {
        goods.name = "abcd";
        goods.imagePath = "abc.jpg";
        goods.salePrice = new BigDecimal(99);
        String uuid = Goods.preview(goods.id, goods, null, "/nfs/images/o");
        Goods cacheGoods = Goods.findById(Long.parseLong(Cache.get(uuid.toString()).toString()));
        assertEquals(goods.imagePath, cacheGoods.imagePath);
        assertEquals("abcd", cacheGoods.name);
        assertEquals(99, cacheGoods.salePrice.longValue());
    }

    @Test
    public void testFindTradeGoodsRecently() {
        // 默认没有成交商品
        assertEquals(0, Goods.findTradeRecently(3).size());

        // 测试最近成交商品
        FactoryBoy.create(OrderItems.class);
        List<Goods> goodsList = Goods.findTradeRecently(3);
        assertEquals(1, goodsList.size());
        assertEquals("哈根达斯100元抵用券", goodsList.get(0).name);
    }

    @Test
    public void testAddRecommend() {
        Goods.addRecommend(goods, true);
        assertEquals(100, goods.recommend.intValue());

        Goods.addRecommend(goods, false);
        assertEquals(101, goods.recommend.intValue());
    }

    @Test
    public void testSetPublishedPlatform() {
        assertEquals(0, goods.unPublishedPlatforms.size());

        List<GoodsPublishedPlatformType> platforms = new ArrayList<>();
        platforms.add(GoodsPublishedPlatformType.DANGDANG);
        goods.setPublishedPlatforms(platforms);
        Goods.update(goods.id, goods);

        List<GoodsUnPublishedPlatform> unPublishedPlatforms = GoodsUnPublishedPlatform.find("goods.id is null").fetch();
        assertEquals(0, unPublishedPlatforms.size());

        models.sales.Goods goods2 = Goods.findById(goods.id);
        assertEquals(2, goods2.unPublishedPlatforms.size());

        platforms = new ArrayList<>();
        platforms.add(GoodsPublishedPlatformType.TAOBAO);
        platforms.add(GoodsPublishedPlatformType.YIHAODIAN);
        goods2.setPublishedPlatforms(platforms);
        Goods.update(goods.id, goods);

        goods2 = Goods.findById(goods.id);
        assertEquals(1, goods2.unPublishedPlatforms.size());

        unPublishedPlatforms = GoodsUnPublishedPlatform.find("goods.id is null")
                .fetch();
        assertEquals(0, unPublishedPlatforms.size());
    }


}