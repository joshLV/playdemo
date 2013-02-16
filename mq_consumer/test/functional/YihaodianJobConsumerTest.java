package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
import models.yihaodian.YihaodianJobConsumer;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.ws.MockWebServiceClient;

import java.util.Date;

/**
 * @author likang
 *         Date: 13-2-6
 */
public class YihaodianJobConsumerTest extends FunctionalTest {
    String orderId = "109082538DU4";
    Long outerIdR = 123L;
    Long outerIdE = 234L;
    Goods goodsR, goodsE;
    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        //电子券商品
        goodsE = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.materialType = MaterialType.ELECTRONIC;
            }
        });
        //实物商品
        goodsR = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.materialType = MaterialType.REAL;
            }
        });
        //电子券的商品关系
        FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.goods = goodsE;
                target.partner = OuterOrderPartner.YHD;
                target.goodsLinkId = outerIdE;
            }
        });
        //实物商品的商品关系
        FactoryBoy.create(ResalerProduct.class, new BuildCallback<ResalerProduct>() {
            @Override
            public void build(ResalerProduct target) {
                target.goods = goodsR;
                target.partner = OuterOrderPartner.YHD;
                target.goodsLinkId = outerIdR;
            }
        });

        //外部订单
        FactoryBoy.create(OuterOrder.class, new BuildCallback<OuterOrder>() {
            @Override
            public void build(OuterOrder target) {
                target.partner = OuterOrderPartner.YHD;
                target.status = OuterOrderStatus.ORDER_COPY;
                target.orderId = orderId;
                target.createdAt = new Date(System.currentTimeMillis() - 600000);
                target.ybqOrder = null;
            }
        });

        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.YHD_LOGIN_NAME;
            }
        });

        AccountUtil.getCreditableAccount(resaler.id, AccountType.RESALER);
        MockWebServiceClient.clear();
    }
    @Test
    public void testConsumer() {
        //模拟订单详情查询
        VirtualFile vf =  VirtualFile.fromRelativePath("test/data/order.detail.xml");
        String orderDetail = vf.contentAsString();
        MockWebServiceClient.addMockHttpRequest(200, orderDetail);

        Long originalOrderCount = Order.count();


        YihaodianJobConsumer consumer = new YihaodianJobConsumer();
        consumer.consumeWithTx(orderId);
        OuterOrder order = FactoryBoy.last(OuterOrder.class);
        order.refresh();

        assertEquals(OuterOrderStatus.ORDER_DONE, order.status);
        assertEquals(originalOrderCount + 1, Order.count());
    }

    @Test
    public void testSyncWithYihaodian() {
        //将外部订单状态置为已完成
        OuterOrder outerOrder = FactoryBoy.last(OuterOrder.class);
        outerOrder.status = OuterOrderStatus.ORDER_DONE;
        outerOrder.save();

        //模拟订单发货
        VirtualFile vf =  VirtualFile.fromRelativePath("test/data/shipments.update.xml");
        String orderDetail = vf.contentAsString();
        MockWebServiceClient.addMockHttpRequest(200, orderDetail);

        YihaodianJobConsumer consumer = new YihaodianJobConsumer();
        consumer.consumeWithTx(orderId);

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.ORDER_SYNCED, outerOrder.status);
    }
    @Test
    public void testSyncWithYihaodianByRefreshOrder() {
        //将外部订单状态置为已完成
        OuterOrder outerOrder = FactoryBoy.last(OuterOrder.class);
        outerOrder.status = OuterOrderStatus.ORDER_DONE;
        outerOrder.save();

        //模拟订单发货错误
        VirtualFile vf =  VirtualFile.fromRelativePath("test/data/shipments.update.error.xml");
        String orderDetail = vf.contentAsString();
        MockWebServiceClient.addMockHttpRequest(200, orderDetail);

        //模拟订单详情状态为已出货
        vf =  VirtualFile.fromRelativePath("test/data/order.detail.synced.xml");
        orderDetail = vf.contentAsString();
        MockWebServiceClient.addMockHttpRequest(200, orderDetail);

        YihaodianJobConsumer consumer = new YihaodianJobConsumer();
        consumer.consumeWithTx(orderId);

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.ORDER_SYNCED, outerOrder.status);
    }
}
