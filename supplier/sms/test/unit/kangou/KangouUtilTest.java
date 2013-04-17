package unit.kangou;

import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.RandomNumberUtil;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.kangou.KangouCard;
import models.kangou.KangouCardStatus;
import models.kangou.KangouUtil;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.OrderItems;
import models.sales.Goods;
import org.junit.Before;
import org.junit.Test;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.UnitTest;
import util.DateHelper;
import util.ws.MockWebServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-4-16
 * Time: 下午6:57
 */
public class KangouUtilTest extends UnitTest {

    Goods goods;
    OrderItems orderItems;

    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        MockWebServiceClient.clear();
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods target) {
                target.supplierGoodsId = 161l;
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class);
    }

    @Test
    public void testGetCardId未设置外部商品ID返回NULL() throws Exception {
        goods.supplierGoodsId = null;
        goods.save();
        assertNull(KangouUtil.getCardId(orderItems));
    }

    @Test
    public void testGetCardId购买一张券返回出错信息() throws Exception {
        mockErrorResponse();
        assertEquals(0, KangouUtil.getCardId(orderItems).size());
    }

    @Test
    public void testGetCardId购买一张券() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        String cardNumber = RandomNumberUtil.generateRandomNumber(10);

        mockGetCardIdResponse(cardId, cardNumber);

        List<KangouCard> kangouCards = KangouUtil.getCardId(orderItems);
        assertEquals(1, kangouCards.size());
        assertEquals(cardId, kangouCards.get(0).cardId);
        assertEquals(cardNumber, kangouCards.get(0).cardNumber);
    }

    @Test
    public void testGetCardId购买3张券() throws Exception {
        orderItems.buyNumber = 3l;
        orderItems.save();
        String cardId0 = RandomNumberUtil.generateRandomNumber(12);
        String cardNumber0 = RandomNumberUtil.generateRandomNumber(10);
        mockGetCardIdResponse(cardId0, cardNumber0);
        mockGetCardIdResponse(RandomNumberUtil.generateRandomNumber(12), RandomNumberUtil.generateRandomNumber(10));
        mockGetCardIdResponse(RandomNumberUtil.generateRandomNumber(12), RandomNumberUtil.generateRandomNumber(10));

        List<KangouCard> kangouCards = KangouUtil.getCardId(orderItems);
        assertEquals(3, kangouCards.size());
        assertEquals(cardId0, kangouCards.get(0).cardId);
        assertEquals(cardNumber0, kangouCards.get(0).cardNumber);
    }

    @Test
    public void testSetCardUseAndSend_1标识为已启用() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        ECoupon eCoupon = mockSetCardUseAndSendResponse(cardId, 1);
        KangouCardStatus status = KangouUtil.setCardUseAndSend(eCoupon);
        assertEquals(KangouCardStatus.AVAIABLE, status);
    }

    @Test
    public void testSetCardUseAndSend_0标识为未启用_发送失败() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        ECoupon eCoupon = mockSetCardUseAndSendResponse(cardId, 0);
        KangouCardStatus status = KangouUtil.setCardUseAndSend(eCoupon);
        assertEquals(KangouCardStatus.UNAVAIABLE, status);
    }

    @Test
    public void testSetCardUseAndSend_9标识为已使用_不可能出现状态() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        ECoupon eCoupon = mockSetCardUseAndSendResponse(cardId, 9);
        KangouCardStatus status = KangouUtil.setCardUseAndSend(eCoupon);
        assertEquals(KangouCardStatus.USED, status);
    }

    @Test
    public void testGetCardStatus_1可用() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        ECoupon eCoupon = mockGetCardStatusResponse(cardId, 1);
        eCoupon.status = ECouponStatus.REFUND;
        ECoupon updatedECoupon = KangouUtil.getCardStatus(eCoupon);
        assertEquals(ECouponStatus.UNCONSUMED, updatedECoupon.status);
    }

    @Test
    public void testGetCardStatus_9已使用() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        ECoupon eCoupon = mockGetCardStatusResponse(cardId, 9);
        eCoupon.status = ECouponStatus.UNCONSUMED;
        ECoupon updatedECoupon = KangouUtil.getCardStatus(eCoupon);
        assertEquals(ECouponStatus.CONSUMED, updatedECoupon.status);
    }


    @Test
    public void testSetCardUseless() throws Exception {
        String cardId = RandomNumberUtil.generateRandomNumber(12);
        ECoupon eCoupon = mockSetCardUselessResponse(cardId);
        KangouCardStatus status = KangouUtil.setCardUseless(eCoupon);
        assertEquals(KangouCardStatus.USED_REFUND, status);
    }

    /**
     * 生成出错信息响应.
     */
    private void mockErrorResponse() {
        Template template = TemplateLoader.load("test/data/kangou/ErrorResponse.xml");
        Map<String, Object> params = new HashMap<>();
        String responseBody = template.render(params);
        MockWebServiceClient.addMockHttpRequest(200, responseBody);
    }

    /**
     * 生成GetCardId的Mock响应数据.
     *
     * @param cardId
     * @param cardNumber
     */
    private void mockGetCardIdResponse(String cardId, String cardNumber) {
        Template template = TemplateLoader.load("test/data/kangou/GetCardIdResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", orderItems.order.orderNumber);
        params.put("cardId", cardId);
        params.put("cardNumber", cardNumber);
        String responseBody = template.render(params);
        MockWebServiceClient.addMockHttpRequest(200, responseBody);
    }

    /**
     * 生成SetCardUseAndSend的Mock响应数据.
     *
     * @param cardId
     * @param cardStatus
     */
    private ECoupon mockSetCardUseAndSendResponse(final String cardId, Integer cardStatus) {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.eCouponSn = cardId;
                target.eCouponPassword = RandomNumberUtil.generateRandomNumber(10);
            }
        });
        Template template = TemplateLoader.load("test/data/kangou/SetCardUseAndSendResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", eCoupon.order.orderNumber);
        params.put("cardId", eCoupon.eCouponSn);
        params.put("cardStatus", cardStatus);
        String responseBody = template.render(params);
        MockWebServiceClient.addMockHttpRequest(200, responseBody);
        return eCoupon;
    }

    private ECoupon mockGetCardStatusResponse(final String cardId, Integer cardStatus) {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.eCouponSn = cardId;
                target.eCouponPassword = RandomNumberUtil.generateRandomNumber(10);
            }
        });
        Template template = TemplateLoader.load("test/data/kangou/GetCardStatusResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", eCoupon.order.orderNumber);
        params.put("cardId", eCoupon.eCouponSn);
        params.put("cardNumber", eCoupon.eCouponPassword);
        params.put("cardStatus", cardStatus);
        params.put("cardDateEnd", DateUtil.dateToString(DateHelper.afterDays(30), "yyyy-MM-dd"));
        params.put("ticketCount", 1);
        params.put("ticketRemainCount", 1);
        params.put("ticketMonth", 1);
        params.put("cardKind", 162);
        String responseBody = template.render(params);
        MockWebServiceClient.addMockHttpRequest(200, responseBody);
        return eCoupon;
    }


    /**
     * 生成SetCardUseAndSend的Mock响应数据.
     *
     * @param cardId
     */
    private ECoupon mockSetCardUselessResponse(final String cardId) {
        ECoupon eCoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.eCouponSn = cardId;
                target.eCouponPassword = RandomNumberUtil.generateRandomNumber(10);
            }
        });
        Template template = TemplateLoader.load("test/data/kangou/SetCardUselessResponse.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", eCoupon.order.orderNumber);
        params.put("cardId", eCoupon.eCouponSn);
        params.put("cardStatus", 9);  //废止
        String responseBody = template.render(params);
        MockWebServiceClient.addMockHttpRequest(200, responseBody);
        return eCoupon;
    }

}
