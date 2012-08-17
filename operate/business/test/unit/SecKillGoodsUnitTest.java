package unit;


import factory.FactoryBoy;
import factory.SequenceCallBack;
import models.order.DiscountCode;
import models.sales.Goods;
import models.sales.SecKillGoods;
import models.sales.SecKillGoodsCondition;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
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
        FactoryBoy.delete(SecKillGoods.class);
    }

    @Test
    public void 取得所有秒杀的商品信息() {
        secKillGoods = FactoryBoy.create(SecKillGoods.class);

        FactoryBoy.batchCreate(5, SecKillGoods.class, new SequenceCallBack<SecKillGoods>() {
            @Override
            public void sequence(SecKillGoods target, int seq) {
                target.goods.name = "TEST" + seq;
                target.goods.save();
                target.personLimitNumber = seq;
            }

        });

        secKillGoods.personLimitNumber = 1;
        secKillGoods.save();
        assertEquals(1, secKillGoods.personLimitNumber.intValue());

        SecKillGoodsCondition condition = new SecKillGoodsCondition();
        JPAExtPaginator<SecKillGoods> secKillGoodsList = SecKillGoods.findByCondition(condition, 0, 10);
        assertEquals(6, secKillGoodsList.size());

        condition.goodsTitle = "TEST";
        secKillGoodsList = SecKillGoods.findByCondition(condition, 0, 10);
        assertEquals(5, secKillGoodsList.size());

    }

    @Test
    public void testUpdateSecKillGoods() {
        secKillGoods = FactoryBoy.create(SecKillGoods.class);
        secKillGoods.personLimitNumber = 20;
        secKillGoods.setPrompt("asd");
        secKillGoods.goods = FactoryBoy.create(Goods.class);
        secKillGoods.save();

        SecKillGoods secKillGoods1 = SecKillGoods.findById(secKillGoods.id);
        assertEquals(20, secKillGoods1.personLimitNumber.intValue());
        assertEquals("asd", secKillGoods1.getPrompt());
        assertTrue(secKillGoods1.goods.name.contains("Product"));
    }
}
