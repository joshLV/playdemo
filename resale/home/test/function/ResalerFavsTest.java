package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import util.DateHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 13-1-7
 * Time: 上午9:43
 */
public class ResalerFavsTest extends FunctionalTest {
    Resaler resaler;
    Goods goods;
    ResalerFav resalerFav;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class);
        Security.setLoginUserForTest(resaler.loginName);
        goods = FactoryBoy.create(Goods.class);
        resalerFav = FactoryBoy.create(ResalerFav.class);
    }

    @Test
    public void testIndex() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateBegin = sdf.format(new Date());
        String dateEnd = sdf.format(DateHelper.afterDays(10));
        Http.Response response = GET("/library?createdAtBegin=" + dateBegin + "&createdAtEnd="
                + dateEnd + "&goodsName=" + goods.shortName + "&goodsId=" + goods.id);
        assertStatus(200, response);
        assertEquals(resaler, ((Resaler) renderArgs("resaler")));
    }

    @Test
    public void testOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("goodsIds", goods.id.toString());
        Http.Response response = POST("/library", params);
        assertStatus(200, response);
        Map<String, String> result = new HashMap<>();
        assertEquals("{\"goodsId\":\"" + goods.id + "\"}", getContent(response));
    }

    @Test
    public void testDelete() {
        List<ResalerFav> favList = ResalerFav.find("deleted=?", com.uhuila.common.constants.DeletedStatus.UN_DELETED).fetch();
        assertEquals(1, favList.size());
        Http.Response response = DELETE("/library/" + goods.id);
        assertStatus(200, response);
        favList = ResalerFav.find("deleted=?", com.uhuila.common.constants.DeletedStatus.DELETED).fetch();
        assertEquals(0, favList.size());
    }
}
