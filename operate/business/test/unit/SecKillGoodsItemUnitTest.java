package unit;

import java.math.BigDecimal;
import java.util.Date;

import models.sales.Goods;
import models.sales.SecKillGoods;
import models.sales.SecKillGoodsCondition;
import models.sales.SecKillGoodsItem;
import models.sales.SecKillGoodsStatus;

import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import util.DateHelper;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;

/**
 * <p/>
 * User: yanjy Date: 12-8-16 Time: 上午10:56
 */
public class SecKillGoodsItemUnitTest extends UnitTest {
    SecKillGoodsItem secKillGoodsItem = null;

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void 取得所有的秒杀活动信息() {
        secKillGoodsItem = FactoryBoy.create(SecKillGoodsItem.class);
        final SecKillGoods secKillGoods = FactoryBoy.create(SecKillGoods.class);
        final Goods goods = FactoryBoy.create(Goods.class);
        secKillGoods.goods = goods;

        FactoryBoy.batchCreate(5, SecKillGoodsItem.class,
                        new SequenceCallback<SecKillGoodsItem>() {
                            @Override
                            public void sequence(SecKillGoodsItem target,
                                            int seq) {
                                target.goodsTitle = "第" + seq + "波" + seq;
                                target.salePrice = new BigDecimal(seq);
                                target.secKillBeginAt = new Date();
                                target.secKillEndAt = DateHelper.afterMinuts(
                                                new Date(), seq);
                                target.baseSale = 100l;
                                target.secKillGoods = secKillGoods;
                                target.secKillGoods.save();
                                target.status = SecKillGoodsStatus.ONSALE;

                            }
                        });

        secKillGoodsItem.status = SecKillGoodsStatus.ONSALE;
        secKillGoodsItem.save();
        assertEquals(SecKillGoodsStatus.ONSALE, secKillGoodsItem.status);

        SecKillGoodsCondition condition = new SecKillGoodsCondition();
        JPAExtPaginator<SecKillGoodsItem> secKillGoodsList = SecKillGoodsItem
                        .findByCondition(condition, secKillGoods.id, 0, 10);
        assertEquals(5, secKillGoodsList.size());

        condition.goodsTitle = "第";
        condition.status = SecKillGoodsStatus.ONSALE;
        secKillGoodsList = SecKillGoodsItem.findByCondition(condition,
                        secKillGoods.id, 0, 10);
        assertEquals(5, secKillGoodsList.size());

        condition.status = SecKillGoodsStatus.OFFSALE;
        secKillGoodsList = SecKillGoodsItem.findByCondition(condition,
                        secKillGoods.id, 0, 10);
        assertEquals(0, secKillGoodsList.size());
    }

    @Test
    public void testUpdateSecKillGoods() {
        secKillGoodsItem = FactoryBoy.create(SecKillGoodsItem.class);
        secKillGoodsItem.goodsTitle = "秒杀活动一";
        secKillGoodsItem.status = SecKillGoodsStatus.OFFSALE;
        secKillGoodsItem.save();

        SecKillGoodsItem secKillGoodsItem1 = SecKillGoodsItem
                        .findById(secKillGoodsItem.id);
        assertEquals("秒杀活动一", secKillGoodsItem1.goodsTitle);
        assertEquals(SecKillGoodsStatus.OFFSALE, secKillGoodsItem1.status);

        SecKillGoodsItem.updateStatus(SecKillGoodsStatus.ONSALE,
                        secKillGoodsItem.id);
        assertEquals(SecKillGoodsStatus.ONSALE, secKillGoodsItem1.status);
    }

    @Test
    public void testIsExpired() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class,
                        "expired", new BuildCallback<SecKillGoodsItem>() {
                            @Override
                            public void build(SecKillGoodsItem target) {
                                target.goodsTitle = "TTTTT";
                            }
                        });
        assertTrue(item.isExpired());
    }
}
