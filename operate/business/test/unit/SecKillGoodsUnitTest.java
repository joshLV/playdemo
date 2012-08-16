package unit;


import factory.FactoryBoy;
import factory.SequenceCallBack;
import models.sales.Goods;
import models.sales.SecKillGoods;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-16
 * Time: 上午10:56
 */
public class SecKillGoodsUnitTest extends UnitTest {
    SecKillGoods secKillGoods = null;

    @Before
    public void setUp() {
//        FactoryBoy.delete(Goods.class, SecKillGoods.class);
        FactoryBoy.lazyDelete();
    }

    @Test
    public void 取得所有秒杀的商品信息() {
        secKillGoods = FactoryBoy.create(SecKillGoods.class);
        FactoryBoy.batchCreate(5, SecKillGoods.class, new SequenceCallBack<SecKillGoods>() {
            @Override
            public void sequence(SecKillGoods target, int seq) {
                target.= "TEST" + seq;
            }

        });
        ModelPaginator discountCodePage = DiscountCode.getDiscountCodePage(0, 10, null);
        assertEquals(1, discountCodePage.getPageCount());
        assertEquals(6, discountCodePage.getRowCount());
        ModelPaginator discountCodePage1 = DiscountCode.getDiscountCodePage(0, 5, null);
        assertEquals(2, discountCodePage1.getPageCount());
        assertEquals(6, discountCodePage1.getRowCount());
        discountCode.deleted = DeletedStatus.DELETED;
        discountCode.save();
        ModelPaginator discountCodePage2 = DiscountCode.getDiscountCodePage(0, 5, null);
        assertEquals(1, discountCodePage2.getPageCount());
        assertEquals(5, discountCodePage2.getRowCount());
    }

    @Test
    public void testUpdateSecKillGoods() {
        secKillGoods = FactoryBoy.create(SecKillGoods.class);
        secKillGoods.personLimitNumber = 2;
        secKillGoods.goods = FactoryBoy.create(Goods.class);

        secKillGoods.save();

        SecKillGoods secKillGoods1 = SecKillGoods.findById(secKillGoods.id);
        assertEquals(2, secKillGoods.personLimitNumber.intValue());
    }

//    @Test
//    public void testFindByName() {
//        Product product = FactoryBoy.create(Product.class, new BuildCallBack<Product>() {
//            @Override
//            public void build(Product target) {
//                target.name = "HHKB";
//            }
//        });
//
//        Product p = Product.find("byName", "HHKB").first();
//        assertEquals(product.id, p.id);
//    }

}
