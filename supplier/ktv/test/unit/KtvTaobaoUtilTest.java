package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomType;
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
    /**
     * 数据库中：
     *
     * A    五月3日    9点至11点
     * B    五月3日    13点至15点
     * C    五月4日    9点至11点
     * D    五月4日    13点至15点
     */

    /**
     * 测试新的为 C D
     * 因为5月3日的都没了，因此 5月3日的都被删除
     */
    @Test
    public void testDiffCaseCD() {
        List<KtvTaobaoSku> oldKtvTaobaoSkuList = KtvTaobaoSku.findAll();

        List<KtvTaobaoSku> newKtvTaobaoSkuList = new ArrayList<>();
        newKtvTaobaoSkuList.add(setSkuC(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuD(new KtvTaobaoSku()));

        Map<String, List<KtvTaobaoSku>> result = KtvTaobaoUtil.diffTaobaoSku(newKtvTaobaoSkuList, oldKtvTaobaoSkuList);
        assertEquals(0, result.get("add").size());
        assertEquals(0, result.get("update").size());
        assertEquals(2, result.get("delete").size());
    }

    /**
     * 测试新的为 A B C
     * 因为 两种日期和两种欢唱时间都还存在，因此没有人被删除，待删除的只是应该数量被更新为0
     */
    @Test
    public void testDiffCaseABC() {
        List<KtvTaobaoSku> oldKtvTaobaoSkuList = KtvTaobaoSku.findAll();

        List<KtvTaobaoSku> newKtvTaobaoSkuList = new ArrayList<>();
        newKtvTaobaoSkuList.add(setSkuA(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuB(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuC(new KtvTaobaoSku()));

        Map<String, List<KtvTaobaoSku>> result = KtvTaobaoUtil.diffTaobaoSku(newKtvTaobaoSkuList, oldKtvTaobaoSkuList);
        assertEquals(0, result.get("add").size());
        assertEquals(1, result.get("update").size());
        assertEquals(0, result.get("delete").size());
    }

    /**
     * 测试新的为 A C
     * 因为 13点至15点的都不在了，因此删除了两条，剩余两条
     */
    @Test
    public void testDiffCaseAC() {
        List<KtvTaobaoSku> oldKtvTaobaoSkuList = KtvTaobaoSku.findAll();

        List<KtvTaobaoSku> newKtvTaobaoSkuList = new ArrayList<>();
        newKtvTaobaoSkuList.add(setSkuA(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuC(new KtvTaobaoSku()));

        Map<String, List<KtvTaobaoSku>> result = KtvTaobaoUtil.diffTaobaoSku(newKtvTaobaoSkuList, oldKtvTaobaoSkuList);
        assertEquals(0, result.get("add").size());
        assertEquals(0, result.get("update").size());
        assertEquals(2, result.get("delete").size());
    }

    /**
     * 测试新的为 A
     * 因为 13点至15点的都不在了，因此删除了两条
     * 5月4日的也都不在了，因此又删除了两条，
     * 这四条中有两个是重叠的，就是B
     * 因此最后只剩下A
     */
    @Test
    public void testDiffCaseA() {
        List<KtvTaobaoSku> oldKtvTaobaoSkuList = KtvTaobaoSku.findAll();

        List<KtvTaobaoSku> newKtvTaobaoSkuList = new ArrayList<>();
        newKtvTaobaoSkuList.add(setSkuA(new KtvTaobaoSku()));

        Map<String, List<KtvTaobaoSku>> result = KtvTaobaoUtil.diffTaobaoSku(newKtvTaobaoSkuList, oldKtvTaobaoSkuList);
        assertEquals(0, result.get("add").size());
        assertEquals(0, result.get("update").size());
        assertEquals(3, result.get("delete").size());
    }
    /**
     * 测试新的为 A E F
     * 因为 13点至15点的都不在了，因此删除了两条
     * 5月4日的也都不在了，因此又删除了两条，
     * 这四条中有两个是重叠的，就是B
     * 因此最后只剩下A
     * 再加新的 E F
     */
    @Test
    public void testDiffCaseAEF() {
        List<KtvTaobaoSku> oldKtvTaobaoSkuList = KtvTaobaoSku.findAll();

        List<KtvTaobaoSku> newKtvTaobaoSkuList = new ArrayList<>();
        newKtvTaobaoSkuList.add(setSkuA(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuE(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuF(new KtvTaobaoSku()));

        Map<String, List<KtvTaobaoSku>> result = KtvTaobaoUtil.diffTaobaoSku(newKtvTaobaoSkuList, oldKtvTaobaoSkuList);
        assertEquals(2, result.get("add").size());
        assertEquals(0, result.get("update").size());
        assertEquals(3, result.get("delete").size());
    }

    /**
     * 测试通过将quantity设置为0，而达到删除的目的
     */
    @Test
    public void testDiffDeleteByUpdate() {
        List<KtvTaobaoSku> oldKtvTaobaoSkuList = KtvTaobaoSku.findAll();
        List<KtvTaobaoSku> newKtvTaobaoSkuList = new ArrayList<>();
        newKtvTaobaoSkuList.add(setSkuA(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.add(setSkuC(new KtvTaobaoSku()));
        newKtvTaobaoSkuList.get(0).quantity = 0;
        newKtvTaobaoSkuList.get(1).quantity = 0;


        Map<String, List<KtvTaobaoSku>> result = KtvTaobaoUtil.diffTaobaoSku(newKtvTaobaoSkuList, oldKtvTaobaoSkuList);
        assertEquals(0, result.get("add").size());
        assertEquals(0, result.get("update").size());
        assertEquals(2, result.get("delete").size());
    }

    KtvPriceSchedule schedule;
    ResalerProduct resalerProduct;
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);

        FactoryBoy.create(KtvTaobaoSku.class, new BuildCallback<KtvTaobaoSku>() {
            @Override
            public void build(KtvTaobaoSku target) { setSkuA(target); }
        });
        FactoryBoy.create(KtvTaobaoSku.class, new BuildCallback<KtvTaobaoSku>() {
            @Override
            public void build(KtvTaobaoSku target) { setSkuB(target); }
        });
        FactoryBoy.create(KtvTaobaoSku.class, new BuildCallback<KtvTaobaoSku>() {
            @Override
            public void build(KtvTaobaoSku target) { setSkuC(target); }
        });
        FactoryBoy.create(KtvTaobaoSku.class, new BuildCallback<KtvTaobaoSku>() {
            @Override
            public void build(KtvTaobaoSku target) { setSkuD(target); }
        });
    }

    private KtvTaobaoSku setSkuA(KtvTaobaoSku target) {
        target.setRoomType(KtvRoomType.MIDDLE.getTaobaoId());
        target.setDate("5月3日");
        target.setTimeRange("9点至11点");
        target.price = BigDecimal.TEN;
        target.quantity = 10;
        return target;
    }
    private KtvTaobaoSku setSkuB(KtvTaobaoSku target) {
        target.setRoomType(KtvRoomType.MIDDLE.getTaobaoId());
        target.setDate("5月3日");
        target.setTimeRange("13点至15点");
        target.price = BigDecimal.TEN;
        target.quantity = 10;
        return target;
    }
    private KtvTaobaoSku setSkuC(KtvTaobaoSku target) {
        target.setRoomType(KtvRoomType.MIDDLE.getTaobaoId());
        target.setDate("5月4日");
        target.setTimeRange("9点至11点");
        target.price = BigDecimal.TEN;
        target.quantity = 10;
        return target;
    }
    private KtvTaobaoSku setSkuD(KtvTaobaoSku target) {
        target.setRoomType(KtvRoomType.MIDDLE.getTaobaoId());
        target.setDate("5月4日");
        target.setTimeRange("13点至15点");
        target.price = BigDecimal.TEN;
        target.quantity = 10;
        return target;
    }
    private KtvTaobaoSku setSkuE(KtvTaobaoSku target) {
        target.setRoomType(KtvRoomType.MIDDLE.getTaobaoId());
        target.setDate("5月5日");
        target.setTimeRange("9点至11点");
        target.price = BigDecimal.TEN;
        target.quantity = 10;
        return target;
    }
    private KtvTaobaoSku setSkuF(KtvTaobaoSku target) {
        target.setRoomType(KtvRoomType.MIDDLE.getTaobaoId());
        target.setDate("5月5日");
        target.setTimeRange("13点至15点");
        target.price = BigDecimal.TEN;
        target.quantity = 10;
        return target;
    }
}
