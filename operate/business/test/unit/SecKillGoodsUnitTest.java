package unit;


import factory.FactoryBoy;
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
    public void testFindByCondition() {

    }

    @Test
    public void testUpdateSecKillGoods() {

        secKillGoods = FactoryBoy.create(SecKillGoods.class);
        secKillGoods.limitNumber = 2;
        secKillGoods.goods = FactoryBoy.create(Goods.class);

        secKillGoods.save();

        SecKillGoods secKillGoods1 = SecKillGoods.findById(secKillGoods.id);
        assertEquals(2, secKillGoods.limitNumber.intValue());
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
