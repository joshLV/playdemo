package functional;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import controllers.EnSmsReceivers;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
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
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import util.mq.MockMQ;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class ConsumerSmsMultiVerifyBaseTest extends FunctionalTest {

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
        assertTrue("The content (" + content + ") does not match '" + pattern
                        + "'", ok);
    }

    protected static void assertSMSContentLength(String content) {
        assertTrue("短信内容(" + content + ")超过67字符, size:" + content.length(),
                        content.length() <= 67);
    }

    /**
     * 执行特定消息发送代码的接口.
     * 
     * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
     * 
     */
    public interface MessageSender {
        Response doMessageSend(ECoupon ecoupon, String msg, String mobile);
    };

    public interface InvalidMessageSender {
        Response doMessageSend(String msg);
    }

    ECoupon kfcECouponm = null;

    /**
     * 准备测试数据的公共方法。
     */
    protected void setupTestData() {
        Play.configuration.setProperty(
                        ECoupon.KEY_USE_PRODUCT_SERIAL_REPLYCODE, "true");

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
     * 测试正常验证过程 消费者有2张券，共20元，通过输入20元验证掉全部券.
     * 
     * @param sendMessage
     */
    public void testNormalConsumerCheckAllEcoupon(MessageSender messageSender) {
        assertEquals(kfc.id, kfcClerk.supplier.id);
        assertEquals(kfcECoupon.goods.supplierId, kfcClerk.supplier.id);

        assertEquals(ECouponStatus.UNCONSUMED, kfcECoupon.status);
        Http.Response response = messageSender.doMessageSend(kfcECoupon,
                        kfcClerk.jobNumber, null);

        assertStatus(200, response);
        // 有相同的replyCode，告诉客户需要录入金额

        SMSMessage checkMsg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertSMSContentLength(checkMsg.getContent());
        assertEquals("您有多张可用券(总面值20元)，请回复店员数字工号*使用金额，如\"100112*200\"，系统自动选择合适的券验证【一百券】",
                        checkMsg.getContent());

        SMSMessage checkMsgClerk = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        // 店员短信
        assertSMSContentLength(checkMsgClerk.getContent());
        assertEquals(getBeginString(kfcECoupon.orderItems.phone, 3)
                        + "*****" +
                        getLastString(kfcECoupon.orderItems.phone, 3)
                        + "有多张可用券，请指导回复数字工号*使用金额，如\"100112*200\"【一百券】",
                        checkMsgClerk.getContent());

        response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber
                        + "*20", null);
        // 消费者短信
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch(
                        "您2张券尾号\\d+/\\d+(总面值20元)于\\d+月\\d+日\\d+时\\d+分成功消费，门店：优惠拉。客服4006865151",
                        msg.getContent());
        // 店员短信
        msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertSMSContentMatch(
                       getBeginString(
                                                        kfcECoupon.orderItems.phone,
                                                        3)
                                        + "\\*\\*\\*\\*\\*"
                                        +
                                        getLastString(kfcECoupon.orderItems.phone,
                                                        3)
                                        + "尾号\\d+/\\d+券（总面值20元）于\\d+月\\d+日\\d+时\\d+分在优惠拉验证成功。客服4006865151",
                        msg.getContent());

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        ECoupon ecoupon2 = ECoupon.findById(kfcECouponm.id);
        ecoupon2.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon2.status);
    }

    /**
     * 测试正常验证过程 消费者有2张券，共20元，通过输入11元验证掉其中一张券.
     * 
     * @param sendMessage
     */
    public void testNormalConsumerCheckOneEcoupon(MessageSender messageSender) {
        assertEquals(kfc.id, kfcClerk.supplier.id);
        assertEquals(kfcECoupon.goods.supplierId, kfcClerk.supplier.id);

        assertEquals(ECouponStatus.UNCONSUMED, kfcECoupon.status);
        Http.Response response = messageSender.doMessageSend(kfcECoupon,
                        kfcClerk.jobNumber, null);

        assertStatus(200, response);
        // 有相同的replyCode，告诉客户需要录入金额
        SMSMessage checkMsg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentLength(checkMsg.getContent());
        assertEquals("您有多张可用券(总面值20元)，请回复店员数字工号*使用金额，如\"100112*200\"，系统自动选择合适的券验证【一百券】",
                        checkMsg.getContent());

        SMSMessage checkMsgClerk = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        // 店员短信
        assertSMSContentLength(checkMsgClerk.getContent());
        assertEquals( getBeginString(kfcECoupon.orderItems.phone, 3)
                        + "*****" +
                        getLastString(kfcECoupon.orderItems.phone, 3)
                        + "有多张可用券，请指导回复数字工号*使用金额，如\"100112*200\"【一百券】",
                        checkMsgClerk.getContent());

        response = messageSender.doMessageSend(kfcECoupon, kfcClerk.jobNumber
                        + "*11", null);
        // 消费者短信
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch(
                        "您1张券尾号\\d+(总面值10元)于\\d+月\\d+日\\d+时\\d+分成功消费，门店：优惠拉。客服4006865151",
                        msg.getContent());
        // 店员短信
        msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS_QUEUE);
        assertSMSContentMatch(
                         getBeginString(
                                                        kfcECoupon.orderItems.phone,
                                                        3)
                                        + "\\*\\*\\*\\*\\*"
                                        +
                                        getLastString(kfcECoupon.orderItems.phone,
                                                        3)
                                        + "尾号\\d+券（总面值10元）于\\d+月\\d+日\\d+时\\d+分在优惠拉验证成功。客服4006865151",
                        msg.getContent());

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        ECoupon ecoupon1 = ECoupon.findById(kfcECouponm.id);
        ecoupon1.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon1.status);
    }

    /**
     * 券格式无效的测试
     * 
     * @param messageSender
     */
    public void testInvalidFormatMessage(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(kfcECoupon,
                        "abc", null);
        assertEquals("Unsupport Message", response.out.toString());
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch(
                        "不支持的命令，券验证请回复店员数字工号；或店员数字工号\\*验证金额，如299412\\*200",
                        msg.getContent());
    }

    /**
     * 店员工号不存在.
     * 
     * @param messageSender
     */
    public void testNotExistsJobNumber(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(kfcECoupon,
                        "998788*20", null);

        assertStatus(200, response);

        // 消费者短信
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch("店员工号无效，请核实工号是否正确或是否是"
                        + kfc.fullName + "门店。如有疑问请致电：4006865151",
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

        Response response = messageSender.doMessageSend(kfcECoupon,
                kfcClerk.jobNumber + "*20", null);
        assertContentEquals(kfc.fullName + "未在一百券登记使用【一百券】", response);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch( kfc.fullName
                        + "未在一百券登记使用，请致电4006865151咨询",
                        msg.getContent());
    }

    /**
     * 商户被冻结.
     */
    public void testLockedSupplier(MessageSender messageSender) {
        kfc.deleted = DeletedStatus.UN_DELETED;
        kfc.status = SupplierStatus.FREEZE;
        kfc.save();

        Response response = messageSender.doMessageSend(kfcECoupon,
                        kfcClerk.jobNumber + "*20", null);
        assertContentEquals(kfc.fullName + "已被一百券锁定【一百券】", response);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentMatch( kfc.fullName
                        + "已被一百券锁定，请致电4006865151咨询",
                        msg.getContent());
    }

    /**
     * 不是商户品牌的券号
     * 
     * @param messageSender
     */
    public void testTheGoodsFromOtherSupplier(MessageSender messageSender) {
        Response response = messageSender.doMessageSend(kfcECoupon,
                        kfcClerk.jobNumber + "*20", null);

        assertContentEquals("店员工号无效，请核实工号是否正确或是否是" + kfc.fullName
                        + "门店【一百券】", response);
        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentLength(msg.getContent());
        assertEquals("店员工号无效，请核实工号是否正确或是否是" + kfc.fullName
                        + "门店。如有疑问请致电：4006865151【一百券】",
                        msg.getContent());
    }

    /**
     * 测试已经消费的券重复验证
     */
    public void testConsumeredECoupon(MessageSender messageSender) {
        assertEquals(kfc.id, kfcClerk.supplier.id);
        assertEquals(kfcECoupon.goods.supplierId, kfcClerk.supplier.id);

        kfcECoupon.status = ECouponStatus.CONSUMED;
        kfcECoupon.save();

        Http.Response response = messageSender.doMessageSend(kfcECoupon,
                        kfcClerk.jobNumber + "*20", null);

        assertStatus(200, response);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        assertContentEquals("您的券号已消费，无法再次消费。如有疑问请致电：4006865151【一百券】",
                        response);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentLength(msg.getContent());
        assertEquals("您尾号" + getLastString(kfcECoupon.eCouponSn, 4)
                        + "券不能重复消费，已于" + df.format(kfcECoupon.consumedAt)
                        + kfcShop.name
                        + "消费过【一百券】", msg.getContent());

        ECoupon ecoupon = ECoupon.findById(kfcECoupon.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

    }

    /**
     * 测试券过期的情况
     */
    public void testExpiredECoupon(MessageSender messageSender) {
        assertEquals(kfc.id, kfcClerk.supplier.id);
        assertEquals(kfcECoupon.goods.supplierId, kfcClerk.supplier.id);

        kfcECoupon.expireAt = DateUtil.getYesterday();
        kfcECoupon.save();

        assertEquals(ECouponStatus.UNCONSUMED, kfcECoupon.status);
        Http.Response response = messageSender.doMessageSend(kfcECoupon,
                        kfcClerk.jobNumber + "*20", null);

        assertContentEquals("您的券号已过期，无法进行消费。如有疑问请致电：4006865151【一百券】",
                        response);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSUtil.SMS2_QUEUE);
        assertSMSContentLength(msg.getContent());
        assertEquals("您的券号已过期，无法进行消费。如有疑问请致电：4006865151【一百券】",
                        msg.getContent());
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
