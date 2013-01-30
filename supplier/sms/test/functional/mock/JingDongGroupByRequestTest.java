package functional.mock;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.ChannelGoodsInfo;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
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
    ChannelGoodsInfo product;
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = "jingdong";
            }
        });
        final GoodsDeployRelation goodsDeployRelation = FactoryBoy.create(GoodsDeployRelation.class,
                new BuildCallback<GoodsDeployRelation>() {
            @Override
            public void build(GoodsDeployRelation target) {
                target.partner = OuterOrderPartner.JD;
            }
        });

        FactoryBoy.create(Goods.class);
        FactoryBoy.create(ResalerFav.class, new BuildCallback<ResalerFav>() {
            @Override
            public void build(ResalerFav target) {
                target.lastLinkId = goodsDeployRelation.linkId;
                target.thirdGroupbuyId = 1234l;
            }
        });
        product = FactoryBoy.create(ChannelGoodsInfo.class);
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
        List<ChannelGoodsInfo> products = (List<ChannelGoodsInfo>) renderArgs("products");
        assertEquals(1, products.size());
    }

    @Test
    public void testDoSendOrder() throws Exception {
        MockWebServiceClient.addMockHttpRequest(200, "Success!");

        Map<String, String> params = new HashMap<>();
        params.put("url", "http://localhost:7402/api/v1/jd/gb/send-order");
        params.put("productId", product.id.toString());
        params.put("mobile", "15028812881");
        params.put("buyNumber", "1");
        Http.Response response = POST(Router.reverse("mock.JingDongGroupByRequest.sendOrder").url, params);
        assertIsOk(response);
        //String result = (String) renderArgs("result");
        //assertEquals("Success!", result);
    }
}
