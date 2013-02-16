package functional;

import factory.FactoryBoy;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.order.ECoupon;
import models.sales.Goods;
import models.sales.ResalerProduct;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import play.test.FunctionalTest;
import util.ws.MockWebServiceClient;

/**
 * 测试当当的部分接口(所有我们这边调用当当的接口).
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 2:58 PM
 */
public class DDAPIUtilTest extends FunctionalTest {
    ResalerProduct product;
    Goods goods;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        product = FactoryBoy.create(ResalerProduct.class);
        goods = product.goods;
        MockWebServiceClient.clear();
    }

    @Test
    public void tesSyncSellCount() {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><status_code>0</status_code><error_code>0</error_code>" +
                "<desc><![CDATA[成功]]></desc><spid>3000003</spid><ver>1.0</ver>" +
                "<data></data></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, response);
        assertTrue(DDGroupBuyUtil.syncSellCount(product));
    }

    @Test
    public void 测试针对在当当退款的券验证券状态的当当接口() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><status_code>0</status_code><error_code>0</error_code>" +
                "<desc><![CDATA[成功]]></desc><spid>3000003</spid><ver>1.0</ver>" +
                "<data><ddgid>256</ddgid><spgid>256</spgid><state>2</state></data></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, data);
        assertEquals(2, DDGroupBuyUtil.couponStatus(ecoupon));
    }

    @Test
    public void 测试验证券状态的当当接口返回调用失败的情况() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><status_code>0</status_code><error_code>3003</error_code>" +
                "<desc><![CDATA[序列号或验证码不存在，请检查序列号或验证码是否输入正确]]></desc></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, data);
        assertEquals(-1, DDGroupBuyUtil.couponStatus(ecoupon));
    }

    @Test
    public void 测试验证券后通知当当的接口() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><ver>1.0</ver><spid>1</spid><error_code>0</error_code>" +
                "<desc>success</desc><data><ddgid>100</ddgid><spgid>100</spgid>" +
                "<ddsn>1344555</ddsn></data></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, data);
        assertTrue(DDGroupBuyUtil.verifyOnDangdang(ecoupon));
    }

    @Test
    public void 测试验证券后通知当当的接口出错的情况() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        String data = "<?xml version=\"1.0\" encoding=\"utf8\" standalone=\"yes\" ?>" +
                "<resultObject><ver>1.0</ver><spid>3000003</spid><status_code>0</status_code>" +
                "<error_code>3003</error_code><desc><![CDATA[您输入的验证码不存在，请检查输入是否正确]]></desc>" +
                "<data><ddgid>57</ddgid><spgid><![CDATA[]]></spgid><ddsn><![CDATA[]]></ddsn></data></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, data);
        assertFalse(DDGroupBuyUtil.verifyOnDangdang(ecoupon));
    }

    @Test
    public void 测试查询当当项目接口_正常情况() {
        String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                "<resultObject><status_code>0</status_code>" +
                "<error_code>0</error_code><desc><![CDATA[成功]]></desc>" +
                "<data><row><name>aaaa</name><ddgid>1800495901</ddgid>" +
                "<spgid>15477</spgid><status>0</status></row>" +
                "<row><name>bbbb</name><ddgid>1800495902</ddgid>" +
                "<spgid>15478</spgid><status>0</status></row></data></resultObject>";
        MockWebServiceClient.addMockHttpRequest(200, data);
        Node node = DDGroupBuyUtil.getJustUploadedTeam(15477L);
        assertNotNull(node);
    }
}
