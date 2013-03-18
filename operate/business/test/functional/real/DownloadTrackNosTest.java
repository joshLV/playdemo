package functional.real;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.OrderItems;
import models.order.OrderShippingInfo;
import models.sales.Goods;
import models.sales.Sku;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;

/**
 * User: tanglq
 * Date: 13-3-13
 * Time: 下午6:26
 */
public class DownloadTrackNosTest extends FunctionalTest {

    Supplier seewi, supplier1;
    Goods seewiGoods, goods1;
    Sku seewiSku, sku1;

    OrderItems seewiOrderItems, orderItems1;
    OrderShippingInfo seewiShippingInfo, shippingInfo1;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        // 视惠产品
        seewi = FactoryBoy.create(Supplier.class, "seewi");
        seewiSku = FactoryBoy.create(Sku.class);
        seewiGoods = FactoryBoy.create(Goods.class, "Real");
        seewiGoods.sku = seewiSku;
        seewiGoods.skuCount = 1;
        seewiGoods.save();
        seewiOrderItems = FactoryBoy.create(OrderItems.class);
        seewiShippingInfo = FactoryBoy.create(OrderShippingInfo.class);

        // 第三方产品
        supplier1 = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier target) {

            }
        });
        goods1 = FactoryBoy.create(Goods.class, "Real");
        sku1 = FactoryBoy.create(Sku.class);
        goods1 = FactoryBoy.create(Goods.class, "Real");
        goods1.sku = sku1;
        goods1.skuCount = 1;
        goods1.save();
        orderItems1 = FactoryBoy.create(OrderItems.class);
        shippingInfo1 = FactoryBoy.create(OrderShippingInfo.class);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIndex() throws Exception {
        //Http.Response response = GET(Router.reverse("DownloadTrackNos.index").url);
        //assertIsOk(response);
    }

    @Test
    public void testDownload() throws Exception {
        //Http.Response response = GET(Router.reverse("DownloadTrackNos.download").url);
        //assertIsOk(response);
    }
}
