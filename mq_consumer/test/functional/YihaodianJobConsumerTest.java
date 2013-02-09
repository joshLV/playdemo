package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.yihaodian.YihaodianJobConsumer;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;

/**
 * @author likang
 *         Date: 13-2-6
 */
public class YihaodianJobConsumerTest extends FunctionalTest {
    String orderId = "109082538DU4";
    @Before
    public void setup() {
        VirtualFile vf =  VirtualFile.fromRelativePath("test/data/order.detail.xml");
        final String orderDetail = vf.contentAsString();
        FactoryBoy.create(OuterOrder.class, new BuildCallback<OuterOrder>() {
            @Override
            public void build(OuterOrder target) {
                target.partner = OuterOrderPartner.YHD;
                target.message = orderDetail;
                target.orderId = orderId;
                target.createdAt = new Date(System.currentTimeMillis() - 600000);
            }
        });
    }
    @Test
    public void testConsumer() {
        YihaodianJobConsumer consumer = new YihaodianJobConsumer();
        consumer.consumeWithTx(orderId);
        OuterOrder order = FactoryBoy.last(OuterOrder.class);
        order.refresh();

        assertEquals(OuterOrderStatus.ORDER_DONE, order.status);
    }
}
