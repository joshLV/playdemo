package unit;

import factory.FactoryBoy;
import models.sales.Goods;
import models.sales.GoodsPublishedPlatformType;
import models.sales.GoodsUnPublishedPlatform;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-10-22
 * Time: 下午7:29
 */
public class GoodsUnPublishedPlatformTest extends UnitTest {

    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testUnPublishPlatform() {
        assertEquals(0, GoodsUnPublishedPlatform.count());
        List<GoodsPublishedPlatformType> publishedPlatforms = new ArrayList<>();
        publishedPlatforms.add(GoodsPublishedPlatformType.DANGDANG);
        publishedPlatforms.add(GoodsPublishedPlatformType.TAOBAO);
        publishedPlatforms.add(GoodsPublishedPlatformType.YIHAODIAN);
        goods.setPublishedPlatforms(publishedPlatforms);
        goods.save();
        assertEquals(0, GoodsUnPublishedPlatform.count());

        publishedPlatforms = null;
        goods.setPublishedPlatforms(publishedPlatforms);
        goods.save();
        assertEquals(3, GoodsUnPublishedPlatform.count());

        publishedPlatforms = new ArrayList<>();
        publishedPlatforms.add(GoodsPublishedPlatformType.DANGDANG);
        publishedPlatforms.add(GoodsPublishedPlatformType.TAOBAO);
        goods.setPublishedPlatforms(publishedPlatforms);
        goods.save();
        assertEquals(1, GoodsUnPublishedPlatform.count());

    }

    @Test
    public void testPublishPlatform() {
        goods.setPublishedPlatforms(null);
        goods.save();
        assertEquals(3, GoodsUnPublishedPlatform.count());

        List<GoodsPublishedPlatformType> publishedPlatforms = new ArrayList<>();
        publishedPlatforms.add(GoodsPublishedPlatformType.DANGDANG);
        publishedPlatforms.add(GoodsPublishedPlatformType.TAOBAO);
        goods.setPublishedPlatforms(publishedPlatforms);

        Goods.update(goods.id, goods);

        Goods target = Goods.findById(goods.id);
        assertEquals(2, target.getPublishedPlatforms().size());


        assertEquals(1, GoodsUnPublishedPlatform.count());


    }
}
