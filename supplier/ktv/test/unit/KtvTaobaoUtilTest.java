package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvTaobaoSku;
import models.ktv.KtvTaobaoUtil;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-5-9
 * Time: 下午2:31
 */
public class KtvTaobaoUtilTest extends UnitTest {

    KtvPriceSchedule schedule;
    ResalerProduct resalerProduct;
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testDiffTaobaoSku_tobeAddSku() {
        List<KtvTaobaoSku> newSkuList = new ArrayList<>();
        KtvTaobaoSku newSku = FactoryBoy.create(KtvTaobaoSku.class);
        newSkuList.add(newSku);
        List<KtvTaobaoSku> oldSkuList = new ArrayList<>();
        Map<String, List<KtvTaobaoSku>> diffResult = KtvTaobaoUtil.diffTaobaoSku(newSkuList, oldSkuList);

        List<KtvTaobaoSku> tobeAddList = diffResult.get("add");
        assertEquals(1, tobeAddList.size());

    }

    @Test
    public void testDiffTaobaoSku_tobeUpdateSku() {
        List<KtvTaobaoSku> newSkuList = new ArrayList<>();
        KtvTaobaoSku newSku = FactoryBoy.create(KtvTaobaoSku.class);
        newSkuList.add(newSku);

        List<KtvTaobaoSku> oldSkuList = new ArrayList<>();
        KtvTaobaoSku ktvTaobaoSku = FactoryBoy.create(KtvTaobaoSku.class, "update");
        oldSkuList.add(ktvTaobaoSku);

        Map<String, List<KtvTaobaoSku>> diffResult = KtvTaobaoUtil.diffTaobaoSku(newSkuList, oldSkuList);
        List<KtvTaobaoSku> tobeUpdateList = diffResult.get("update");
        assertEquals(1, tobeUpdateList.size());

        KtvTaobaoSku s = tobeUpdateList.get(0);
        assertEquals(BigDecimal.TEN, s.price);
    }

    @Test
    public void testDiffTaobaoSku_tobeDeleteSku() {
        List<KtvTaobaoSku> newSkuList = new ArrayList<>();
        KtvTaobaoSku newSku = FactoryBoy.create(KtvTaobaoSku.class);

        newSkuList.add(newSku);

        List<KtvTaobaoSku> oldSkuList = new ArrayList<>();
        KtvTaobaoSku ktvTaobaoSku = FactoryBoy.create(KtvTaobaoSku.class);
        ktvTaobaoSku.setTimeRange("8点至9点");
        ktvTaobaoSku.save();

        KtvTaobaoSku sku = new KtvTaobaoSku();
        sku.setDate("5月12");
        sku.setRoomType("27426219:3442354");
        sku.setTimeRange("8点至10点");
        sku.goods = FactoryBoy.lastOrCreate(Goods.class);
        sku.price = BigDecimal.TEN;
        sku.quantity = 10;
        sku.save();
        oldSkuList.add(sku);
        oldSkuList.add(ktvTaobaoSku);

        Map<String, List<KtvTaobaoSku>> diffResult = KtvTaobaoUtil.diffTaobaoSku(newSkuList, oldSkuList);
        List<KtvTaobaoSku> tobeDeleteList = diffResult.get("delete");
        assertEquals(2, tobeDeleteList.size());


    }
}
