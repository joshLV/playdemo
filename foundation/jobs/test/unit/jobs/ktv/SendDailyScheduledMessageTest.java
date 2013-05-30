package unit.jobs.ktv;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import jobs.ktv.SendDailyScheduledMessage;
import models.ktv.KtvOrderStatus;
import models.ktv.KtvProduct;
import models.ktv.KtvRoomOrderInfo;
import models.order.Order;
import models.sales.Shop;
import models.sms.SMSMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;
import util.mq.MockMQ;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: yan
 * Date: 13-5-27
 * Time: 下午5:30
 */
public class SendDailyScheduledMessageTest extends UnitTest {
    SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);
    Shop shop;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockMQ.clear();
        shop = FactoryBoy.create(Shop.class);
    }

    @Test
    public void testJob_相同包间() throws Exception {
        create1HourOrderInfo();
        create2HourOrderInfo();
        SendDailyScheduledMessage job = new SendDailyScheduledMessage();
        job.doJobWithHistory();
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);

        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】【15026682165中包(1间)9点至11点】", msg.getContent());
    }

    @Test
    public void testJob_1个包间() throws Exception {
        create1HourOrderInfo();
        SendDailyScheduledMessage job = new SendDailyScheduledMessage();
        job.doJobWithHistory();
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);

        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】", msg.getContent());
    }

    @Test
    public void testJob_不同包间() throws Exception {
        create1HourOrderInfo();
        create3HourOrderInfo();
        SendDailyScheduledMessage job = new SendDailyScheduledMessage();
        job.doJobWithHistory();
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "test店预订【15026682165中包(1间)9点至13点】", msg.getContent());
        msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】", msg.getContent());

    }

    @Test
    public void testJob_不同包间_2相同1个不同() throws Exception {
        create1HourOrderInfo();
        create2HourOrderInfo();
        create3HourOrderInfo();
        SendDailyScheduledMessage job = new SendDailyScheduledMessage();
        job.doJobWithHistory();
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "test店预订【15026682165中包(1间)9点至13点】", msg.getContent());
        msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】【15026682165中包(1间)9点至11点】", msg.getContent());

    }

    @Test
    public void testJob_不同包间_相同门店顺序错乱的() throws Exception {
        create1HourOrderInfo();
        create3HourOrderInfo();
        create2HourOrderInfo();
        SendDailyScheduledMessage job = new SendDailyScheduledMessage();
        job.doJobWithHistory();
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "test店预订【15026682165中包(1间)9点至13点】", msg.getContent());
        msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);

        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】【15026682165中包(1间)9点至11点】", msg.getContent());
    }

    @Test
    @Ignore
    public void testJob_超过12个包间() throws Exception {
        create12OrderInfo();
        create4HourOrderInfo();
        SendDailyScheduledMessage job = new SendDailyScheduledMessage();
        job.doJobWithHistory();
        SMSMessage msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】【15026682165中包(1间)9点至12点】", msg.getContent());
        msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "测试店预订【15026682165中包(1间)9点至12点】【15026682165中包(1间)9点至12点】【15026682165中包(1间)9点至12点】", msg.getContent());
        msg = (SMSMessage) MockMQ.getLastMessage(SMSMessage.SMS_QUEUE);
        assertSMSContentEquals(dateFormat.format(new Date()) + "徐汇店预订【15026682165中包(1间)9点至14点】", msg.getContent());
    }

    private void create12OrderInfo() {
        FactoryBoy.batchCreate(5, KtvRoomOrderInfo.class, new BuildCallback<KtvRoomOrderInfo>() {
            @Override
            public void build(KtvRoomOrderInfo target) {
                target.status = KtvOrderStatus.DEAL;
                target.product = FactoryBoy.create(KtvProduct.class, "threeHours");
                target.shop = shop;
                target.save();
            }
        });
    }

    private void create1HourOrderInfo() {
        KtvRoomOrderInfo ktvRoomOrderInfo = FactoryBoy.create(KtvRoomOrderInfo.class);
        ktvRoomOrderInfo.status = KtvOrderStatus.DEAL;
        ktvRoomOrderInfo.product = FactoryBoy.create(KtvProduct.class, "threeHours");
        ktvRoomOrderInfo.shop = shop;
        ktvRoomOrderInfo.save();
    }

    private void create2HourOrderInfo() {
        KtvRoomOrderInfo ktvRoomOrderInfo = FactoryBoy.create(KtvRoomOrderInfo.class);
        ktvRoomOrderInfo.status = KtvOrderStatus.DEAL;
        ktvRoomOrderInfo.shop = shop;
        ktvRoomOrderInfo.product = FactoryBoy.create(KtvProduct.class, "twoHours");
        ktvRoomOrderInfo.save();
    }

    private void create3HourOrderInfo() {
        Shop shop1 = FactoryBoy.create(Shop.class, new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.name = "test店";
            }
        });
        KtvRoomOrderInfo ktvRoomOrderInfo = FactoryBoy.create(KtvRoomOrderInfo.class);
        ktvRoomOrderInfo.status = KtvOrderStatus.DEAL;
        ktvRoomOrderInfo.shop = shop1;
        ktvRoomOrderInfo.product = FactoryBoy.create(KtvProduct.class, "fourHours");
        ktvRoomOrderInfo.save();
    }

    private void create4HourOrderInfo() {
        Shop shop1 = FactoryBoy.create(Shop.class, new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.name = "徐汇店";
                target.managerMobiles = "1300000000";
            }
        });
        KtvRoomOrderInfo ktvRoomOrderInfo = FactoryBoy.create(KtvRoomOrderInfo.class);
        ktvRoomOrderInfo.status = KtvOrderStatus.DEAL;
        ktvRoomOrderInfo.shop = shop1;
        ktvRoomOrderInfo.product = FactoryBoy.create(KtvProduct.class, "fiveHours");
        ktvRoomOrderInfo.save();
    }

    /**
     * 使用正则匹配结果.
     *
     * @param pattern
     * @param content
     */
    public static void assertSMSContentEquals(String pattern, String content) {
        assertEquals("The content (" + content + ") does not match (" + pattern
                + ")", pattern + "【一百券】", content);
    }
}
