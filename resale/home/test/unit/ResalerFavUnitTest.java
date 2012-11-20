package unit;

import factory.FactoryBoy;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ResalerFavUnitTest extends UnitTest {
    Resaler resaler;
    ResalerFav resalerFav;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resalerFav = FactoryBoy.create(ResalerFav.class);
        resaler = resalerFav.resaler;
        goods = resalerFav.goods;
    }

    @Test
    public void testFindAll() {
        List<ResalerFav> favList = ResalerFav.findAll();
        assertEquals(1, favList.size());
    }

    @Test
    public void testfindFavs() {
        Date createdAtBegin = DateHelper.beforeDays(3);
        Date createdAtEnd = new Date();
        String goodsName = "Product";
        List<ResalerFav> favList = ResalerFav.findFavs(resaler, createdAtBegin, createdAtEnd, goodsName);
        assertEquals(1, favList.size());
    }

    @Test
    public void testDel() {
        List<Long> goodsIds = new ArrayList();
        goodsIds.add(goods.id);
        int delfav = ResalerFav.delete(resaler, goodsIds);
        assertEquals(1, delfav);
    }

    @Test
    public void testCheckGoods() {
        Long[] goodsIds = new Long[1];
        goodsIds[0] = goods.id;
        Map<String, String> map = ResalerFav.checkGoods(resaler, goodsIds);

        assertEquals("1", map.get("isExist"));
        assertEquals(goods.id.toString(), map.get("goodsId"));
    }
}
