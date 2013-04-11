package functional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.MockSMSProvider;
import models.sms.SMSMessage;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;

import org.junit.Test;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import util.DateHelper;
import util.mq.MockMQ;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;

import controllers.EnSmsReceivers;
import controllers.SmsReceiverUtil;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

/**
 * <p/>
 * User: yanjy
 * Date: 12-5-22
 * Time: 下午2:09
 */
public class ClerkSmsVerifyBaseTest extends FunctionalTest {

    User kfcUser;
    Goods kfcGoods;
    Supplier kfc;
    SupplierUser kfcClerk;
    Brand kfcBrand;
    Shop kfcShop;
    Order kfcOrder;
    OrderItems kfcOrderItem;
    ECoupon kfcECoupon;

    @Test
    public void 类型检查() {
        assertTrue(new EnSmsReceivers() instanceof Controller);
    }

    /**
     * 使用正则匹配结果.
     *
     * @param pattern
     * @param content
     */
    public static void assertSMSContentMatch(String pattern, String content) {
        assertSMSContentLength(content);
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(content).find();
        assertTrue("The content (" + content + ") does not match '" + pattern + "'【一百券】", ok);
    }

    protected static void assertSMSContentLength(String content) {
        assertTrue("短信内容(" + content + ")超过67字符, size:" + content.length(), content.length() <= 67);
    }

    protected SMSMessage getLastClerkSMSMessage() {
        return (SMSMessage) MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
    }

    protected SMSMessage getLastConsumerSMSMessage() {
        return (SMSMessage) MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
    }

    /**
     * 执行特定消息发送代码的接口.
     *
     * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
     */
    public interface MessageSender {
        public Response doMessageSend(String mobile, ECoupon ecoupon);
    }

    public interface InvalidMessageSender {
        public Response doMessageSend(String msg);
    }

    /**
     * 准备测试数据的公共方法。
     */
    protected void setupTestData() {
        FactoryBoy.deleteAll();
        kfcUser = FactoryBoy.create(User.class);
        kfc = FactoryBoy.create(Supplier.class, "KFC");
        kfcShop = FactoryBoy.create(Shop.class);
        kfcBrand = FactoryBoy.create(Brand.class, new BuildCallback<Brand>() {
            @Override
            public void build(Brand brand) {
                brand.name = "肯德基";
            }
        });
        kfcClerk = FactoryBoy.create(SupplierUser.class);

        kfcGoods = FactoryBoy.create(Goods.class);
        kfcOrder = FactoryBoy.create(Order.class);
        kfcOrderItem = FactoryBoy.create(OrderItems.class);
        kfcECoupon = FactoryBoy.create(ECoupon.class);

        // 测试验证涉及金额转账，所以要有初始资金.
        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("10000");
        account.save();

        MockMQ.clear();
    }

    /**
     * 测试正常验证过程
     *
     * @param sendMessage
     */
    public void testNormalClerkCheck(MessageSender messageSender) {
        assertEquals(ECouponStatus.UNCONSUMED, kfcECoupon.status);

        Http.Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);

        assertStatus(200, response);

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        // 消费者短信
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch("您尾号" + getLastString(ecoupon.eCouponSn, 4) + "券于\\d+月\\d+日\\d+时\\d+分成功消费，门店：" + kfcShop.name + "。客服4006865151",
                msg.getContent());
        // 店员短信
        msg = getLastClerkSMSMessage();
        assertSMSContentMatch(getBeginString(ecoupon.orderItems.phone, 3) + "\\*\\*\\*\\*\\*" +
                getLastString(ecoupon.orderItems.phone, 3) + "尾号" + getLastString(ecoupon.eCouponSn, 4) + "券（面值" +
                ecoupon.faceValue + "元）于\\d+月\\d+日\\d+时\\d+分在" + kfcShop.name + "验证成功。客服4006865151", msg.getContent());
    }


    /**
     * 测试正常验证过程中不再验证时间范围内
     *
     * @param sendMessage
     */
    public void testNotInVerifyTime(MessageSender messageSender) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());

        String week = "";
        String day = "";
        ca.add(Calendar.DAY_OF_MONTH, -1);
        week = String.valueOf(getWeek(ca.get(Calendar.DAY_OF_WEEK)));
        ca.add(Calendar.DAY_OF_MONTH, +1);
        week += "," + String.valueOf(getWeek(ca.get(Calendar.DAY_OF_WEEK)));
        kfcGoods.useWeekDay = week;
        ca.set(Calendar.HOUR_OF_DAY, 1);
        kfcGoods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 2);
        kfcGoods.useEndTime = df.format(ca.getTime());
        kfcGoods.save();
        kfcGoods.refresh();

        Http.Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);

        assertStatus(200, response);
        day = kfcECoupon.getWeek();
        // 消费者短信
        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentMatch("对不起，该券只能在" + day.substring(0, day.length() - 1) + "的" + kfcGoods.useBeginTime + "~" + kfcGoods.useEndTime + "时间内使用！如有疑问请致电：4006865151", msg.getContent());

        Goods goods = Goods.findById(kfcGoods.id);
        ca = Calendar.getInstance();
        ca.add(Calendar.DAY_OF_MONTH, -3);
        goods.useWeekDay = String.valueOf(ca.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 23);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 2);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();

        response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        day = kfcECoupon.getWeek();
        assertStatus(200, response);
        msg = getLastClerkSMSMessage();
        assertSMSContentMatch("对不起，该券只能在" + day.substring(0, day.length() - 1) + "的" + goods.useBeginTime + "~次日" + goods.useEndTime + "时间内使用！如有疑问请致电：4006865151", msg.getContent());


        goods = Goods.findById(kfcGoods.id);
        goods.useWeekDay = "1,2,3,4,5,6,7";
        ca.set(Calendar.HOUR_OF_DAY, 8);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 9);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();
        response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);

        assertStatus(200, response);
        msg = getLastClerkSMSMessage();
        assertSMSContentMatch("对不起，该券只能在每天的" + goods.useBeginTime + "~" + goods.useEndTime + "时间内使用！如有疑问请致电：4006865151", msg.getContent());
    }

    private int getWeek(int w) {
        if (w == 1) {
            return 7;
        } else if (w > 1 && w < 8) {
            return w - 1;
        } else {
            return -1;
        }
    }

    /**
     * 券格式无效的测试
     *
     * @param messageSender
     */
    public void testInvalidFormatMessage(InvalidMessageSender messageSender) {
        kfcClerk.mobile = "15900002342";
        kfcClerk.save();

        Http.Response response = messageSender.doMessageSend("abc");
        assertEquals("Unsupport Message", response.out.toString());
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch("券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：4006865151",
                msg.getContent());
    }

    /**
     * 券冻结的测试
     *
     * @param messageSender
     */
    public void testFreezedECoupon(MessageSender messageSender) {
        kfcECoupon.isFreeze = 1;
        kfcECoupon.save();
        Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        assertContentEquals("该券已被冻结", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("该券已被冻结,如有疑问请致电：4006865151【一百券】", msg.getContent());
    }

    /**
     * 券不存在.
     *
     * @param messageSender
     */
    public void testEcouponNotExists(InvalidMessageSender messageSender) {
        kfcClerk.mobile = "15900002342";
        kfcClerk.save();

        String couponNumber = "1123456700";
        Http.Response response = messageSender.doMessageSend("#" + couponNumber);
        assertContentEquals("您输入的券号" + couponNumber + "不存在，请确认！", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentMatch("您输入的券号" + couponNumber + "不存在，请与顾客确认，如有疑问请致电：4006865151",
                msg.getContent());
    }

    /**
     * 商户不存在.
     *
     * @param messageSender
     */
    public void testInvalidSupplier(MessageSender messageSender) {
        kfc.deleted = DeletedStatus.DELETED;
        kfc.save();

        Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        assertContentEquals(kfc.fullName + "未在一百券登记使用", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentMatch(kfc.fullName + "未在一百券登记使用，如有疑问请致电：4006865151",
                msg.getContent());
    }

    /**
     * 商户被冻结.
     */
    public void testLockedSupplier(MessageSender messageSender) {
        kfc.deleted = DeletedStatus.UN_DELETED;
        kfc.status = SupplierStatus.FREEZE;
        kfc.save();

        Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        assertContentEquals( kfc.fullName + "已被一百券锁定", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentMatch(kfc.fullName + "已被一百券锁定，如有疑问请致电：4006865151",
                msg.getContent());
    }

    /**
     * 店号工号无效.
     */
    public void testInvalidClerk(MessageSender messageSender) {
        kfcClerk.deleted = DeletedStatus.DELETED;
        kfcClerk.save();

        Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        assertContentEquals("请确认券号" + kfcECoupon.eCouponSn + "(" + kfc.fullName + ")是否本店商品，或店号手机号是否已在一百券登记【一百券】", response);

        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("请确认券号" + kfcECoupon.eCouponSn + "(" + kfc.fullName + ")是否本店商品，或店号手机号是否已在一百券登记。如有疑问请致电：4006865151【一百券】", msg.getContent());
    }

    /**
     * 不是商户品牌的券号
     * 麦当劳员工试图进行验证。。。
     *
     * @param messageSender
     */
    public void testTheGoodsFromOtherSupplier(MessageSender messageSender) {
        final Supplier mcdonalds = FactoryBoy.create(Supplier.class);
        final Shop mcdonaldsShop = FactoryBoy.create(Shop.class);
        FactoryBoy.create(Brand.class, new BuildCallback<Brand>() {
            @Override
            public void build(Brand brand) {
                brand.name = "麦当劳";
            }
        });
        SupplierUser mcdonaldsClerk = FactoryBoy.create(SupplierUser.class, new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser supplierUser) {
                supplierUser.supplier = mcdonalds;
                supplierUser.mobile = "13800010002";  //指定另一手机号
                supplierUser.shop = mcdonaldsShop;
            }
        });

        Response response = messageSender.doMessageSend(mcdonaldsClerk.mobile, kfcECoupon);
        assertContentEquals("请确认券号" + kfcECoupon.eCouponSn + "(" + kfc.fullName + ")是否本店商品，或店号手机号是否已在一百券登记", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("请确认券号" + kfcECoupon.eCouponSn + "(" + kfc.fullName + ")是否本店商品，或店号手机号是否已在一百券登记。如有疑问请致电：4006865151【一百券】", msg.getContent());
    }

    /**
     * 测试已经消费的券重复验证
     */
    public void testConsumeredECoupon(MessageSender messageSender) {
        kfcECoupon.status = ECouponStatus.CONSUMED;
        kfcECoupon.consumedAt = DateHelper.beforeDays(1);
        kfcECoupon.save();

        Http.Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        assertContentEquals("券号" + kfcECoupon.eCouponSn + "已消费，无法再次消费", response);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("" + SmsReceiverUtil.getMaskedMobile(kfcUser.mobile) + "尾号" + getLastString(ecoupon.eCouponSn, 4) + "券（" + ecoupon
                .faceValue + "元）不能重复消费，已于" + df.format(ecoupon.consumedAt) + "在" + kfcShop.name + "消费过【一百券】", msg.getContent());
    }

    /**
     * 测试券过期的情况
     */
    public void testExpiredECoupon(MessageSender messageSender) {
        //已过期的验证
        kfcECoupon.expireAt = DateUtil.getYesterday();
        kfcECoupon.save();

        Http.Response response = messageSender.doMessageSend(kfcClerk.mobile, kfcECoupon);
        assertContentEquals("券号" + kfcECoupon.eCouponSn + "已过期，无法进行消费", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("券号" + kfcECoupon.eCouponSn + "已过期，无法进行消费。如有疑问请致电：4006865151【一百券】", msg.getContent());
    }


    /**
     * 取后length位的字符.
     *
     * @param str
     * @param length
     * @return
     */
    protected String getLastString(String str, int length) {
        return str.substring(str.length() - length);
    }

    /**
     * 取前length位的字符.
     *
     * @param str
     * @param length
     * @return
     */
    protected String getBeginString(String str, int length) {
        return str.substring(0, length);
    }

}
