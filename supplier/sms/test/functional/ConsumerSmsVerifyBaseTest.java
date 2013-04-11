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
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class ConsumerSmsVerifyBaseTest extends FunctionalTest {

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
        assertTrue("The content (" + content + ") does not match '" + pattern + "'", ok);
    }

    protected static void assertSMSContentLength(String content) {
        assertTrue("短信内容(" + content + ")超过67字符, size:" + content.length(), content.length() <= 67);
    }

    /**
     * 执行特定消息发送代码的接口.
     *
     * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
     */
    public interface MessageSender {
        public Response doMessageSend(ECoupon ecoupon, String msg, String mobile);
    }

    ;

    public interface InvalidMessageSender {
        public Response doMessageSend(String msg);
    }

    protected SMSMessage getLastConsumerSMSMessage() {
        return (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
    }

    protected SMSMessage getLastClerkSMSMessage() {
        return (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
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
     * 测试正常验证过程中不再验证时间范围内
     *
     * @param sendMessage
     */
    public void testNotInVerifyTime(MessageSender messageSender) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());

        Goods goods = Goods.findById(kfcGoods.id);
        String week = "";
        String day = "";
        ca.add(Calendar.DAY_OF_MONTH, -1);
        week = String.valueOf(ca.get(Calendar.DAY_OF_WEEK));
        ca.add(Calendar.DAY_OF_MONTH, +1);
        week += "," + String.valueOf(ca.get(Calendar.DAY_OF_WEEK));
        goods.useWeekDay = week;
        ca.set(Calendar.HOUR_OF_DAY, 1);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 2);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();

        Http.Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);
        assertStatus(200, response);
        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
        day = ecoupon.getWeek();

        // 消费者短信
        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentMatch("对不起，该券只能在" + day.substring(0, day.length() - 1) + "的" + goods.useBeginTime + "~" + goods.useEndTime + "时间内使用！如有疑问请致电：4006865151", msg.getContent());

        goods = Goods.findById(kfcGoods.id);
        ca = Calendar.getInstance();
        ca.add(Calendar.DAY_OF_MONTH, -3);
        goods.useWeekDay = String.valueOf(ca.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 23);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 2);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();
        goods.refresh();

        response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);
        assertStatus(200, response);
        ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
        day = ecoupon.getWeek();
        // 消费者短信
        msg = getLastClerkSMSMessage();
        assertSMSContentMatch("对不起，该券只能在" + day.substring(0, day.length() - 1) + "的" + goods.useBeginTime + "~次日" + goods.useEndTime + "时间内使用！如有疑问请致电：4006865151", msg.getContent());

        goods = Goods.findById(kfcGoods.id);
        goods.useWeekDay = "1,2,3,4,5,6,7";
        ca.set(Calendar.HOUR_OF_DAY, 8);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 9);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();
        goods.refresh();
        response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);
        assertStatus(200, response);
        ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
        // 消费者短信
        msg = getLastClerkSMSMessage();
        assertSMSContentMatch("对不起，该券只能在每天的" + goods.useBeginTime + "~" + goods.useEndTime + "时间内使用！如有疑问请致电：4006865151", msg.getContent());
    }

    /**
     * 测试正常验证过程
     *
     * @param sendMessage
     */
    public void testNormalConsumerCheck(MessageSender messageSender) {
        assertEquals(0, MockMQ.size(SMSUtil.SMS_QUEUE));
        assertEquals(0, MockMQ.size(SMSUtil.SMS2_QUEUE));
        assertEquals(ECouponStatus.UNCONSUMED, kfcECoupon.status);
        Http.Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);

        assertStatus(200, response);
        assertEquals(1, MockMQ.size(SMSUtil.SMS_QUEUE));
        assertEquals(1, MockMQ.size(SMSUtil.SMS2_QUEUE));

        // 消费者短信
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch("您尾号" + getLastString(kfcECoupon.eCouponSn, 4) + "券于\\d+月\\d+日\\d+时\\d+分成功消费，门店：" + kfcShop.name + "。客服4006865151",
                msg.getContent());
        // 店员短信
        SMSMessage msg2 = getLastClerkSMSMessage();
        assertSMSContentMatch( getBeginString(kfcECoupon.orderItems.phone, 3) + "\\*\\*\\*\\*\\*" +
                getLastString(kfcECoupon.orderItems.phone, 3) + "尾号" + getLastString(kfcECoupon.eCouponSn, 4) + "券（面值" +
                kfcECoupon.faceValue + "元）于\\d+月\\d+日\\d+时\\d+分在" + kfcShop.name + "验证成功。客服4006865151", msg2.getContent());

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);
        assertEquals(0, MockMQ.size(SMSUtil.SMS_QUEUE));
    }

    /**
     * 券格式无效的测试
     *
     * @param messageSender
     */
    public void testInvalidFormatMessage(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(kfcECoupon, "abc", null);
        assertEquals("Unsupport Message", response.out.toString());
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch("不支持的命令，券验证请回复店员数字工号；或店员数字工号\\*验证金额，如299412\\*200",
                msg.getContent());
    }

    /**
     * 店员工号不存在.
     *
     * @param messageSender
     */
    public void testNotExistsJobNumber(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(kfcECoupon, "998788", null);

        assertStatus(200, response);

        // 消费者短信
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch("店员工号无效，请核实工号是否正确或是否是" + kfc.fullName + "门店。如有疑问请致电：4006865151",
                msg.getContent());

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
    }

    /**
     * 商户不存在.
     *
     * @param messageSender
     */
    public void testInvalidSupplier(MessageSender messageSender) {
        kfc.deleted = DeletedStatus.DELETED;
        kfc.save();

        Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);
        assertContentEquals( kfc.fullName + "未在一百券登记使用", response);

        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch( kfc.fullName + "未在一百券登记使用，请致电4006865151咨询",
                msg.getContent());
    }

    /**
     * 商户被冻结.
     */
    public void testLockedSupplier(MessageSender messageSender) {
        kfc.deleted = DeletedStatus.UN_DELETED;
        kfc.status = SupplierStatus.FREEZE;
        kfc.save();

        Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);
        assertContentEquals( kfc.fullName + "已被一百券锁定", response);

        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentMatch( kfc.fullName + "已被一百券锁定，请致电4006865151咨询",
                msg.getContent());
    }

    /**
     * 不是商户品牌的券号
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
				supplierUser.jobNumber = "9831017";    //指定不同的工号
				supplierUser.shop = mcdonaldsShop;
			}
		});

        Response response = messageSender.doMessageSend(kfcECoupon, mcdonaldsClerk.jobNumber, null);

        assertContentEquals("店员工号无效，请核实工号是否正确或是否是" + kfc.fullName + "门店", response);
        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("店员工号无效，请核实工号是否正确或是否是" + kfc.fullName + "门店。如有疑问请致电：4006865151【一百券】", msg.getContent());
    }

    /**
     * 测试已经消费的券重复验证
     */
    public void testConsumeredECoupon(MessageSender messageSender) {
        kfcECoupon.status = ECouponStatus.CONSUMED;
        kfcECoupon.consumedAt = DateHelper.beforeDays(1);
        kfcECoupon.save();

        Http.Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);

        assertStatus(200, response);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        assertContentEquals("您的券号已消费，无法再次消费。如有疑问请致电：4006865151", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("您尾号" + getLastString(kfcECoupon.eCouponSn, 4) + "券不能重复消费，已于" + df.format(kfcECoupon.consumedAt) + "在" + kfcShop.name + "消费过【一百券】", msg.getContent());

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

    }

    /**
     * 券冻结的测试
     *
     * @param messageSender
     */
    public void testFreezedECoupon(MessageSender messageSender) {
        kfcECoupon.isFreeze = 1;
        kfcECoupon.save();

        Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);
        assertContentEquals("该券已被冻结", response);

        SMSMessage msg = getLastClerkSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("该券已被冻结,如有疑问请致电：4006865151【一百券】", msg.getContent());
    }

    /**
     * 测试券过期的情况
     */
    public void testExpiredECoupon(MessageSender messageSender) {
        kfcECoupon.expireAt = DateUtil.getYesterday();
        kfcECoupon.save();

        assertEquals(ECouponStatus.UNCONSUMED, kfcECoupon.status);
        Http.Response response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber, null);

        assertContentEquals("您的券号已过期，无法进行消费。如有疑问请致电：4006865151", response);

        SMSMessage msg = getLastConsumerSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("您的券号已过期，无法进行消费。如有疑问请致电：4006865151【一百券】", msg.getContent());
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
