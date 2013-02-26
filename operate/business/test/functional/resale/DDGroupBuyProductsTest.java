package functional.resale;

import factory.FactoryBoy;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-2-22
 */
@Ignore
public class DDGroupBuyProductsTest extends FunctionalTest{
    Goods goods;
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
        MockWebServiceClient.clear();
    }

    @Test
    public void testShowUpload() {
        Map<String, Object> params = new HashMap<>();
        params.put("goodsId", goods.id);
        Http.Response response = GET(Router.reverse("controllers.resale.DDGroupBuyProducts.showUpload", params));
        assertIsOk(response);
    }

    @Test
    public void testUpload() {
        String ddResponse = "<resultObject><error_code>0</error_code><desc>成功</desc></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, ddResponse);

        Map<String, String> params = new HashMap<>();
        Http.Response response = POST(Router.reverse("controllers.resale.DDGroupBuyProducts.showUpload"), params);
        assertIsOk(response);

        assertEquals(1, ResalerProduct.count("byPartner", OuterOrderPartner.DD));
    }
}
