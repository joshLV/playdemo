package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.operator.Operator;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import models.weixin.WeixinUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import play.libs.WS;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Http;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-3-20
 */
public class WeixinAPITest extends FunctionalTest {

    Supplier supplier;
    SupplierUser supplierUser;
    Shop shop;
    ECoupon eCoupon;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        supplier = FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        eCoupon = FactoryBoy.create(ECoupon.class);
        supplierUser = FactoryBoy.create(SupplierUser.class, new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser target) {
                target.supplierUserType = SupplierUserType.HUMAN;
                target.idCode = "987623";
            }
        });

        // 测试验证涉及金额转账，所以要有初始资金.
        Account account = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        account.amount = new BigDecimal("10000");
        account.save();

        MockMQ.clear();
    }

    @Test
    public void testHeartBeat() {
        String timestamp = String.valueOf((int)(new Date().getTime()/1000));
        String nonce = "abc";
        String echostr = "大山的子孙哟";
        String signature = WeixinUtil.makeSignature(timestamp, nonce);

        String url = "/api/v1/weixin/message?timestamp=" + timestamp
                + "&nonce=" + nonce
                + "&echostr=" + WS.encode(echostr)
                + "&signature=" + signature;
        Http.Response response = GET(url);
        assertIsOk(response);
        assertContentEquals(echostr, response);
    }

    /**
     * 测试绑定用户
     * @throws Exception
     */
    @Test
    public void testBindSupplierUser() throws Exception {
        assertNull(supplierUser.weixinOpenId);  //没有绑定

        // 发送身份识别码
        String resultText = getWeiXinResponseContent(supplierUser.idCode);
        assertEquals("身份绑定成功！" + supplierUser.userName + ", 欢迎您使用一百券商家助手！", resultText);
        supplierUser.refresh();
        assertEquals("456", supplierUser.weixinOpenId);
        assertNull(supplierUser.idCode); //绑定成功后会清除idCode
    }

    /**
     * 同一个OpenId只能绑定一次.
     * @throws Exception
     */
    @Test
    public void testBindSameOpenId() throws Exception {
        SupplierUser bindedUser = FactoryBoy.create(SupplierUser.class, new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser target) {
                target.weixinOpenId = "456";
            }
        });
        assertNull(supplierUser.weixinOpenId);  //没有绑定

        // 发送身份识别码
        String resultText = getWeiXinResponseContent(supplierUser.idCode);
        assertEquals("您的微信已经绑定了用户『" + bindedUser.loginName +"』，不能重复绑定，请先登录到『" + bindedUser.loginName + "』解绑微信，或使用其它微信号。",
                resultText);
        supplierUser.refresh();
        assertNull(supplierUser.weixinOpenId); //还是没有绑定
        assertNotNull(supplierUser.idCode);

    }

    /**
     * 加好友后出现的消息.
     * @throws Exception
     */
    @Test
    public void testBindSuccessMessage() throws Exception {
        String resultText = getWeiXinResponseContent("Hello2BizUser");
        assertEquals("欢迎使用【一百券商家助手】，使用【一百券商家助手】可通过微信进行消费券验证。请输入商家后台所提供的『身份识别码』，绑定您的微信。", resultText);
    }

    @Test
    public void testBindInvalidSupplierUser() throws Exception {
        assertNull(supplierUser.weixinOpenId);  //没有绑定

        // 发送身份识别码
        String resultText = getWeiXinResponseContent("111111");
        assertEquals("没有找到身份绑定信息！", resultText);
        supplierUser.refresh();
        assertNull(supplierUser.weixinOpenId);
    }

    /**
     * 测试验证券
     * @throws Exception
     */
    @Test
    public void testVerifySuccess() throws Exception {
        assertEquals(ECouponStatus.UNCONSUMED, eCoupon.status);
        supplierUser.weixinOpenId = "456"; //使用test/data/wx.basicTest.xml中的fromUser值
        supplierUser.save();
        // 验证券
        String resultText = getWeiXinResponseContent(eCoupon.eCouponSn);
        assertTrue(resultText.contains("成功消费"));
        eCoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, eCoupon.status);
    }

    /**
     * 验证不存在的券号
     * @throws Exception
     */
    @Test
    public void testVerifyNotExistCoupon() throws Exception {
        assertEquals(ECouponStatus.UNCONSUMED, eCoupon.status);
        supplierUser.weixinOpenId = "456"; //使用test/data/wx.basicTest.xml中的fromUser值
        supplierUser.save();
        // 验证券
        String resultText = getWeiXinResponseContent("8821000000");
        assertEquals("您输入的券号8821000000不存在，请确认！", resultText);
        eCoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, eCoupon.status);
    }

    /**
     * 接入输入消息内容，处理请求报文，并返回对应的响应文本.
     * @param content 输入报文
     * @return 响应文本
     */
    private String getWeiXinResponseContent(String content) {
        Template template = TemplateLoader.load("test/data/wx.basicTest.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("content", content);
        params.put("timestamp", (new Date().getTime())/1000l);
        String requestBody = template.render(params);
        Http.Response response = POST("/api/v1/weixin/message", MULTIPART_FORM_DATA, requestBody);
        assertIsOk(response);
        String responseBody = getContent(response);
        Document responseXml = XML.getDocument(responseBody);
        return XPath.selectText("/xml/Content", responseXml);
    }

}
