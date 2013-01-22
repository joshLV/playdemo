package function;

import java.math.BigDecimal;

import models.order.ECoupon;
import models.order.ECouponCompensation;
import models.order.ECouponPartner;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OuterOrder;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;
import util.ws.MockWebServiceClient;
import controllers.ECouponCompensations;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class ECouponCompensationsTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        FactoryBoy.create(Order.class);
    }

    @Test
    public void testInvalidSecret() throws Exception {
        Response response = GET("/ecoupon-compensation/consumed");
        assertStatus(500, response);
    }

    @Test
    public void testConsumed() throws Exception {
        FactoryBoy.create(OuterOrder.class);
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.partner = ECouponPartner.JD;
                target.status = ECouponStatus.CONSUMED;
            }
        });
        FactoryBoy.create(ECouponCompensation.class);
        FactoryBoy.create(ECouponCompensation.class, new BuildCallback<ECouponCompensation>() {
            @Override
            public void build(ECouponCompensation target) {
                target.compensatedAt = DateHelper.beforeDays(1);
                target.result = "SUCCESS";
            }
        });

        // mock jingdong response
        String resultXml = VirtualFile.open("test/data/xml/JingDongVerifyECouponResponse.xml").contentAsString();
        MockWebServiceClient.addMockHttpRequest(200, resultXml);

        Response response = GET("/ecoupon-compensation/consumed?secret=" + ECouponCompensations.SECRET);
        assertIsOk(response);
        Integer count = (Integer)renderArgs("count");
        assertEquals(new Integer(1), count);
        assertEquals(ecoupon.salePrice.setScale(2), ((BigDecimal)renderArgs("amount")).setScale(2));
    }

    @Test
    public void testConsumeFailed() throws Exception {
        FactoryBoy.create(OuterOrder.class);
        FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.partner = ECouponPartner.JD;
                target.status = ECouponStatus.CONSUMED;
            }
        });
        FactoryBoy.create(ECouponCompensation.class);
        FactoryBoy.create(ECouponCompensation.class, new BuildCallback<ECouponCompensation>() {
            @Override
            public void build(ECouponCompensation target) {
                target.compensatedAt = DateHelper.beforeDays(1);
                target.result = "SUCCESS";
            }
        });

        // mock jingdong response
        String resultXml = VirtualFile.open("test/data/xml/JingDongVerifyECouponFailResponse.xml").contentAsString();
        MockWebServiceClient.addMockHttpRequest(200, resultXml);

        Response response = GET("/ecoupon-compensation/consumed?secret=" + ECouponCompensations.SECRET);
        assertIsOk(response);
        Integer count = (Integer)renderArgs("count");
        assertEquals(new Integer(0), count);
        assertEquals(BigDecimal.ZERO, ((BigDecimal)renderArgs("amount")));
    }

}
