package functional.order;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-1-16
 * Time: 下午6:24
 */
public class JingDongGroupByRequestTest extends FunctionalTest {
    ResalerProduct product;
    Resaler resaler;
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.JD_LOGIN_NAME;
            }
        });
        product = FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.partner = OuterOrderPartner.JD;
                target.partnerProductId = "1234";
                target.resaler = resaler;
            }
        });

        MockWebServiceClient.clear();
    }

    @Test
    public void testCheckMockSwitch() throws Exception {
        Play.configuration.setProperty("mock.api.ui", "disable");
        Http.Response response = GET(Router.reverse("mock.JingDongGroupByRequest.sendOrder").url);
        assertStatus(404, response);
        Play.configuration.setProperty("mock.api.ui", "enabled");
    }

    @Test
    public void testSendOrder() throws Exception {
        Http.Response response = GET(Router.reverse("mock.JingDongGroupByRequest.sendOrder").url);
        assertIsOk(response);
        List<ResalerProduct> products = (List<ResalerProduct>) renderArgs("products");
        assertEquals(1, products.size());
    }

    @Test
    public void testDoSendOrder() throws Exception {
        MockWebServiceClient.addMockHttpRequest(200, "Success!");

        Map<String, String> params = new HashMap<>();
        params.put("url", "http://localhost:7402/api/v1/jd/gb/send-order");
        params.put("productId", String.valueOf(product.goodsLinkId));
        params.put("mobile", "15028812881");
        params.put("buyNumber", "1");
        Http.Response response = POST(Router.reverse("mock.JingDongGroupByRequest.sendOrder").url, params);
        assertIsOk(response);
        //String result = (String) renderArgs("result");
        //assertEquals("Success!", result);
    }
}
