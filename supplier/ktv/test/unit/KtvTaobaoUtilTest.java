package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvTaobaoSku;
import models.ktv.KtvTaobaoUtil;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.junit.Test;
import play.test.UnitTest;

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

    @Test
    public void testDiffTaobaoSku() {
        java.util.List<KtvTaobaoSku> newSkuList = new ArrayList<>();
        KtvTaobaoSku newSku = new KtvTaobaoSku();
        FactoryBoy.batchCreate(5, KtvTaobaoSku.class, new BuildCallback<KtvTaobaoSku>() {
            @Override
            public void build(KtvTaobaoSku target) {
                target.goods = goods;
                target.setDate("5月11日");
                target.setRoomType("27426219:3374388");
                target.setTimeRange("18点至21点");

            }
        });

        List<KtvTaobaoSku> oldSkuList = new ArrayList<>();
        Map<String, List<KtvTaobaoSku>> diffResult = KtvTaobaoUtil.diffTaobaoSku(newSkuList, oldSkuList);


    }

}
