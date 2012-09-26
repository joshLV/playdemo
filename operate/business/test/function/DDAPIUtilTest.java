package function;

import factory.FactoryBoy;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.dangdang.DDOrderItem;
import models.dangdang.ErrorCode;
import models.dangdang.HttpProxy;
import models.dangdang.Response;
import models.order.ECoupon;
import models.sales.Goods;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import play.mvc.Before;
import play.test.FunctionalTest;

import java.io.ByteArrayInputStream;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 2:58 PM
 */
public class DDAPIUtilTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void tesSyncSellCount() {
        Goods goods = FactoryBoy.create(Goods.class);

        try {
            DDAPIUtil.proxy = new HttpProxy() {
                @Override
                public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                    Response response = new Response();
                    response.errorCode = ErrorCode.SUCCESS;
                    return response;
                }
            };
            DDAPIUtil.syncSellCount(goods);
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

    @Test
    public void testIsRefund() throws Exception{
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException{
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><status_code>0</status_code><error_code>0</error_code>" +
                        "<desc><![CDATA[成功]]></desc><spid>3000003</spid><ver>1.0</ver>" +
                        "<data><ddgid>256</ddgid><spgid>256</spgid><state>0</state></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.errorCode = ErrorCode.SUCCESS;
                return response;
            }
        };

        try {
            boolean isRefund = DDAPIUtil.isRefund(ecoupon);
            assertFalse(isRefund);
        } catch (DDAPIInvokeException e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void testNotifyVerified() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException{
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><ver>1.0</ver><spid>1</spid><error_code>0</error_code>" +
                        "<desc>success</desc><data><ddgid>100</ddgid><spgid>100</spgid>" +
                        "<ddsn>1344555</ddsn></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.errorCode = ErrorCode.SUCCESS;
                return response;
            }
        };

        try {
            DDAPIUtil.notifyVerified(ecoupon);
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

}
