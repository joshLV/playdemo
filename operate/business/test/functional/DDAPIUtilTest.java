package functional;

import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.GoodsHistory;
import org.junit.Test;
import play.test.FunctionalTest;

/**
 * 测试当当的部分接口(所有我们这边调用当当的接口).
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 2:58 PM
 */
public class DDAPIUtilTest extends FunctionalTest {
    GoodsHistory goodsHistory;
    Goods goods;
    GoodsDeployRelation deployRelation;

    /*
    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        deployRelation = FactoryBoy.create(GoodsDeployRelation.class);
        goods = deployRelation.goods;
        goodsHistory = FactoryBoy.create(GoodsHistory.class);
    }

    @Test
    public void tesSyncSellCount() {
        try {
            DDAPIUtil.proxy = new HttpProxy() {
                @Override
                public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                    Response response = new Response();
                    response.errorCode = DDErrorCode.SUCCESS;
                    return response;
                }
            };
            DDAPIUtil.syncSellCount(goods);
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

    @Test
    public void 测试未退款的券验证券状态的当当接口() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
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
                response.errorCode = DDErrorCode.SUCCESS;
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
    public void 测试针对在当当退款的券验证券状态的当当接口() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><status_code>0</status_code><error_code>0</error_code>" +
                        "<desc><![CDATA[成功]]></desc><spid>3000003</spid><ver>1.0</ver>" +
                        "<data><ddgid>256</ddgid><spgid>256</spgid><state>2</state></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
        };

        try {
            boolean isRefund = DDAPIUtil.isRefund(ecoupon);
            assertTrue(isRefund);

        } catch (DDAPIInvokeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void 测试验证券状态的当当接口返回调用失败的情况() throws Exception {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><status_code>0</status_code><error_code>3003</error_code>" +
                        "<desc><![CDATA[序列号或验证码不存在，请检查序列号或验证码是否输入正确]]></desc></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
        };

        try {
            boolean isRefund = DDAPIUtil.isRefund(ecoupon);
            fail();
        } catch (DDAPIInvokeException e) {
            assertTrue(e.getMessage().contains("序列号或验证码不存在，请检查序列号或验证码是否输入正确"));
        }
    }

    @Test
    public void 测试验证券后通知当当的接口() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
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
                return response;
            }
        };

        DDAPIUtil.notifyVerified(ecoupon);
    }

    @Test
    public void 测试验证券后通知当当的接口出错的情况() {
        ECoupon ecoupon = FactoryBoy.create(ECoupon.class);
        DDOrderItem item = FactoryBoy.create(DDOrderItem.class);
        item.ybqOrderItems = ecoupon.orderItems;
        item.save();

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                String data = "<?xml version=\"1.0\" encoding=\"utf8\" standalone=\"yes\" ?>" +
                        "<resultObject><ver>1.0</ver><spid>3000003</spid><status_code>0</status_code>" +
                        "<error_code>3003</error_code><desc><![CDATA[您输入的验证码不存在，请检查输入是否正确]]></desc>" +
                        "<data><ddgid>57</ddgid><spgid><![CDATA[]]></spgid><ddsn><![CDATA[]]></ddsn></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
        };

        DDAPIUtil.notifyVerified(ecoupon);

        DDFailureLog failureLog = DDFailureLog.find("byECouponId", ecoupon.id).first();
        assertNotNull(failureLog);
        assertEquals("您输入的验证码不存在，请检查输入是否正确", failureLog.desc);
        assertEquals(DDErrorCode.ECOUPON_NOT_EXITED, failureLog.errorCode);
        assertEquals(ecoupon.order.id, failureLog.orderId);
    }*/

    @Test
    public void 测试查询当当项目接口_正常情况() {
        /*

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><status_code>0</status_code>" +
                        "<error_code>0</error_code><desc><![CDATA[成功]]></desc>" +
                        "<data><row><name>aaaa</name><ddgid>1800495901</ddgid>" +
                        "<spgid>15477</spgid><status>0</status></row>" +
                        "<row><name>bbbb</name><ddgid>1800495902</ddgid>" +
                        "<spgid>15478</spgid><status>0</status></row></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
        };

        try {
            String ddGoodsId = DDAPIUtil.getItemList(15477l);
            assertEquals("1800495901", ddGoodsId);
        } catch (DDAPIInvokeException e) {
            e.fillInStackTrace();
        }
        */
    }

    /*
    @Test
    public void 测试查询当当项目接口_查询不到的情况() {

        DDAPIUtil.proxy = new HttpProxy() {
            @Override
            public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>" +
                        "<resultObject><status_code>0</status_code>" +
                        "<error_code>0</error_code><desc><![CDATA[成功]]></desc>" +
                        "<data><row><name>aaaa</name><ddgid>1800495901</ddgid>" +
                        "<spgid>15476</spgid><status>0</status></row>" +
                        "<row><name>bbbb</name><ddgid>1800495902</ddgid>" +
                        "<spgid>15478</spgid><status>0</status></row></data></resultObject>";
                Response response = new Response();
                try {
                    response = new Response(new ByteArrayInputStream(data.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return response;
            }
        };

        try {
            String ddGoodsId = DDAPIUtil.getItemList(15477l);
            assertEquals("", ddGoodsId);
        } catch (DDAPIInvokeException e) {
            e.fillInStackTrace();
        }
    }
    */
}
