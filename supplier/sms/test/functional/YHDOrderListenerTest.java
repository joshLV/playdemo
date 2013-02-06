package functional;

import factory.FactoryBoy;
import models.job.yihaodian.listener.OrderListener;
import models.order.OuterOrder;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.ws.MockWebServiceClient;

/**
 * @author likang
 *         Date: 13-2-5
 */
public class YHDOrderListenerTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testJob() {
        VirtualFile vf =  VirtualFile.fromRelativePath("test/data/yhd.orders.detail.get.xml");
        String mockOrderDetail = vf.contentAsString();
        vf = VirtualFile.fromRelativePath("test/data/yhd.orders.get.xml");
        String mockOrderList = vf.contentAsString();

        MockWebServiceClient.addMockHttpRequest(200, mockOrderList);
        MockWebServiceClient.addMockHttpRequest(200, mockOrderDetail);

        OrderListener orderListener = new OrderListener();
        Long outerOrderSize = OuterOrder.count();

        orderListener.doJob();

        assertEquals(outerOrderSize + 2, OuterOrder.count());
        OuterOrder outerOrder = (OuterOrder)OuterOrder.findAll().get(0);
        System.out.println("======= sssss\n" + outerOrder.message);
    }
}
