package unit.supplier;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import jobs.dadong.DadongErSendToRequest;
import models.order.CouponHistory;
import models.order.ECoupon;
import models.order.OrderItems;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.supplier.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.mq.MockMQ;
import util.ws.MockWebServiceClient;

/**
 * User: tanglq
 * Date: 13-1-21
 * Time: 下午8:41
 */
public class DadongErSendToRequestTest extends UnitTest {

    Supplier dadong;

    OrderItems orderItems;

    ECoupon ecoupon;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();

        MockWebServiceClient.clear();
        dadong = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier target) {
                target.domainName = "dadong";
            }
        });

        FactoryBoy.create(Category.class, new BuildCallback<Category>() {
            @Override
            public void build(Category target) {
                target.name = "旅游票务";
            }
        });
        FactoryBoy.create(Brand.class);
        FactoryBoy.create(Goods.class);
        orderItems = FactoryBoy.create(OrderItems.class);
        ecoupon = FactoryBoy.create(ECoupon.class);
    }

    @After
    public void tearDown() {
        MockMQ.clear();
    }

    @Test
    public void testSendToOldPhone() throws Exception {
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/ErSendToResponse1.xml");

        DadongErSendToRequest.resend(orderItems, orderItems.phone);
        ecoupon.refresh();

        assertEquals(1, CouponHistory.count());
        CouponHistory couponHistory = CouponHistory.find("order by id desc").first();
        assertEquals("大东票务重发券:0000Success", couponHistory.remark);
    }

    @Test
    public void testSendToNewPhone() throws Exception {
        MockWebServiceClient.addMockHttpRequestFromFile(200, "test/data/dadong/ErSendToResponse1.xml");

        DadongErSendToRequest.resend(orderItems, "13188188818");
        ecoupon.refresh();

        assertEquals(1, CouponHistory.count());
        CouponHistory couponHistory = CouponHistory.find("order by id desc").first();
        assertEquals("大东票务重发券 发至新手机13188188818:0000Success", couponHistory.remark);
    }
}
