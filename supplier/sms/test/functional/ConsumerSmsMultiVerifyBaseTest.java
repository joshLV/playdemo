package functional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.MockSMSProvider;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;

import org.junit.Test;

import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;

import controllers.EnSmsReceivers;

public class ConsumerSmsMultiVerifyBaseTest extends FunctionalTest {

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

    /**
     * 测试用店员.
     */
    SupplierUser kfcClerk = null;

    /**
     * 测试用门店.
     */
    Shop kfcShop = null;

    Supplier supplierKFC = null;

    ECoupon ecouponKFC2 = null;
    ECoupon ecouponKFC2m = null;

    /**
     * 准备测试数据的公共方法。
     */
    protected void setupTestData() {
        Play.configuration.setProperty(
                        ECoupon.KEY_USE_PRODUCT_SERIAL_REPLYCODE, "true");

        
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.delete(Order.class);
        Fixtures.delete(OrderItems.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(User.class);
        Fixtures.delete(ECoupon.class);
        Fixtures.delete(SupplierRole.class);
        Fixtures.delete(Supplier.class);
        Fixtures.delete(SupplierUser.class);
        Fixtures.loadModels("fixture/roles.yml", "fixture/shop.yml",
                        "fixture/supplierusers.yml", "fixture/goods_base.yml",
                        "fixture/user.yml", "fixture/accounts.yml",
                        "fixture/goods.yml",
                        "fixture/orders.yml",
                        "fixture/orderItems_multicheck.yml");

        Long kfcId = (Long) Fixtures.idCache
                        .get("models.supplier.Supplier-kfc");
        Long goodsId = (Long) Fixtures.idCache
                        .get("models.sales.Goods-Goods_002");

        supplierKFC = Supplier.findById(kfcId);

        Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierKFC.id;
        goods.groupCode = "group1";
        goods.save();

        Long kfc1Id = (Long) Fixtures.idCache
                        .get("models.supplier.Supplier-kfc1");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        goods = Goods.findById(goodsId);
        goods.supplierId = kfc1Id;
        goods.save();

        Long kfc2Id = (Long) Fixtures.idCache
                        .get("models.supplier.Supplier-kfc2");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");
        goods = Goods.findById(goodsId);
        goods.supplierId = kfc2Id;
        goods.save();

        Long kfc3Id = (Long) Fixtures.idCache
                        .get("models.supplier.Supplier-kfc3");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        goods = Goods.findById(goodsId);
        goods.supplierId = kfc3Id;
        goods.save();

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("99999");
        account.save();

        Long goods2Id = (Long) Fixtures.idCache
                        .get("models.sales.Goods-Goods_002");
        Goods goods2 = Goods.findById(goods2Id);
        goods2.supplierId = supplierKFC.id;
        goods2.groupCode = "group1";
        goods2.save();

        Long brandId = (Long) play.test.Fixtures.idCache
                        .get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplierKFC;
        brand.save();

        Long shopId = (Long) play.test.Fixtures.idCache
                        .get("models.sales.Shop-Shop_4");
        kfcShop = Shop.findById(shopId);
        kfcShop.supplierId = kfcId;
        kfcShop.save();

        Long kfcClerkId = (Long) play.test.Fixtures.idCache
                        .get("models.admin.SupplierUser-user2");
        kfcClerk = SupplierUser.findById(kfcClerkId);
        kfcClerk.deleted = DeletedStatus.UN_DELETED;
        kfcClerk.shop = kfcShop;
        kfcClerk.save();

        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ecouponKFC2 = ECoupon.findById(id);

        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2m");
        ecouponKFC2m = ECoupon.findById(id);

    }

    /**
     * 测试正常验证过程 消费者有2张券，共20元，通过输入20元验证掉全部券.
     * 
     * @param sendMessage
     */
    public void testNormalConsumerCheckAllEcoupon(MessageSender messageSender) {
        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC2.goods.supplierId, kfcClerk.supplier.id);

        assertEquals(ECouponStatus.UNCONSUMED, ecouponKFC2.status);
        Http.Response response = messageSender.doMessageSend(ecouponKFC2,
                        kfcClerk.jobNumber, null);

        assertStatus(200, response);
        // 有相同的replyCode，告诉客户需要录入金额

        SMSMessage checkMsg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(checkMsg.getContent());
        assertEquals("【一百券】您有多张可用券(总面值20元)，请回复店员数字工号*使用金额，如\"100112*200\"，系统自动选择合适的券验证",
                        checkMsg.getContent());

        SMSMessage checkMsgClerk = MockSMSProvider.getLastSMSMessage();
        // 店员短信
        assertSMSContentLength(checkMsgClerk.getContent());
        assertEquals("【一百券】" + getBeginString(ecouponKFC2.orderItems.phone, 3)
                        + "*****" +
                        getLastString(ecouponKFC2.orderItems.phone, 3)
                        + "有多张可用券，请指导回复数字工号*使用金额，如\"100112*200\"",
                        checkMsgClerk.getContent());

        response = messageSender.doMessageSend(ecouponKFC2, kfcClerk.jobNumber
                        + "*20", null);
        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch(
                        "【一百券】您2张券尾号\\d+/\\d+(总面值20元)于\\d+月\\d+日\\d+时\\d+分成功消费，门店：优惠拉。客服4006262166",
                        msg.getContent());
        // 店员短信
        msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch(
                        "【一百券】"
                                        + getBeginString(
                                                        ecouponKFC2.orderItems.phone,
                                                        3)
                                        + "\\*\\*\\*\\*\\*"
                                        +
                                        getLastString(ecouponKFC2.orderItems.phone,
                                                        3)
                                        + "尾号\\d+/\\d+券（总面值20元）于\\d+月\\d+日\\d+时\\d+分在优惠拉验证成功。客服4006262166",
                        msg.getContent());

        ECoupon ecoupon = ECoupon.findById(ecouponKFC2.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        ECoupon ecoupon2 = ECoupon.findById(ecouponKFC2m.id);
        ecoupon2.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon2.status);
    }

    /**
     * 测试正常验证过程 消费者有2张券，共20元，通过输入11元验证掉其中一张券.
     * 
     * @param sendMessage
     */
    public void testNormalConsumerCheckOneEcoupon(MessageSender messageSender) {
        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC2.goods.supplierId, kfcClerk.supplier.id);

        assertEquals(ECouponStatus.UNCONSUMED, ecouponKFC2.status);
        Http.Response response = messageSender.doMessageSend(ecouponKFC2,
                        kfcClerk.jobNumber, null);

        assertStatus(200, response);
        // 有相同的replyCode，告诉客户需要录入金额
        SMSMessage checkMsg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(checkMsg.getContent());
        assertEquals("【一百券】您有多张可用券(总面值20元)，请回复店员数字工号*使用金额，如\"100112*200\"，系统自动选择合适的券验证",
                        checkMsg.getContent());

        SMSMessage checkMsgClerk = MockSMSProvider.getLastSMSMessage();
        // 店员短信
        assertSMSContentLength(checkMsgClerk.getContent());
        assertEquals("【一百券】" + getBeginString(ecouponKFC2.orderItems.phone, 3)
                        + "*****" +
                        getLastString(ecouponKFC2.orderItems.phone, 3)
                        + "有多张可用券，请指导回复数字工号*使用金额，如\"100112*200\"",
                        checkMsgClerk.getContent());

        response = messageSender.doMessageSend(ecouponKFC2, kfcClerk.jobNumber
                        + "*11", null);
        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch(
                        "【一百券】您1张券尾号\\d+(总面值10元)于\\d+月\\d+日\\d+时\\d+分成功消费，门店：优惠拉。客服4006262166",
                        msg.getContent());
        // 店员短信
        msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch(
                        "【一百券】"
                                        + getBeginString(
                                                        ecouponKFC2.orderItems.phone,
                                                        3)
                                        + "\\*\\*\\*\\*\\*"
                                        +
                                        getLastString(ecouponKFC2.orderItems.phone,
                                                        3)
                                        + "尾号\\d+券（总面值10元）于\\d+月\\d+日\\d+时\\d+分在优惠拉验证成功。客服4006262166",
                        msg.getContent());

        ECoupon ecoupon = ECoupon.findById(ecouponKFC2.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        ECoupon ecoupon1 = ECoupon.findById(ecouponKFC2m.id);
        ecoupon1.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon1.status);
    }

    /**
     * 券格式无效的测试
     * 
     * @param messageSender
     */
    public void testInvalidFormatMessage(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(ecouponKFC2,
                        "abc", null);
        assertEquals("Unsupport Message", response.out.toString());
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch(
                        "【一百券】不支持的命令，券验证请回复店员数字工号；或店员数字工号\\*验证金额，如299412\\*200",
                        msg.getContent());
    }

    /**
     * 店员工号不存在.
     * 
     * @param messageSender
     */
    public void testNotExistsJobNumber(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(ecouponKFC2,
                        "998788*20", null);

        assertStatus(200, response);

        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【一百券】店员工号无效，请核实工号是否正确或是否是"
                        + supplierKFC.fullName + "门店。如有疑问请致电：4006262166",
                        msg.getContent());

        ECoupon ecoupon = ECoupon.findById(ecouponKFC2.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
    }

    /**
     * 商户不存在.
     * 
     * @param messageSender
     */
    public void testInvalidSupplier(MessageSender messageSender) {
        Supplier kfc = Supplier.findById(supplierKFC.id);
        kfc.deleted = DeletedStatus.DELETED;
        kfc.save();

        Response response = messageSender.doMessageSend(ecouponKFC2,
                kfcClerk.jobNumber + "*20", null);
        assertContentEquals("【一百券】" + kfc.fullName + "未在一百券登记使用", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【一百券】" + kfc.fullName
                        + "未在一百券登记使用，请致电4006262166咨询",
                        msg.getContent());
    }

    /**
     * 商户被冻结.
     */
    public void testLockedSupplier(MessageSender messageSender) {
        Supplier kfc = Supplier.findById(supplierKFC.id);
        kfc.deleted = DeletedStatus.UN_DELETED;
        kfc.status = SupplierStatus.FREEZE;
        kfc.save();

        Response response = messageSender.doMessageSend(ecouponKFC2,
                        kfcClerk.jobNumber + "*20", null);
        assertContentEquals("【一百券】" + kfc.fullName + "已被一百券锁定", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【一百券】" + kfc.fullName
                        + "已被一百券锁定，请致电4g00-6262-166咨询",
                        msg.getContent());
    }

    /**
     * 不是商户品牌的券号
     * 
     * @param messageSender
     */
    public void testTheGoodsFromOtherSupplier(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon5");
        ECoupon ecoupon = ECoupon.findById(id);

        Response response = messageSender.doMessageSend(ecoupon,
                        kfcClerk.jobNumber + "*20", null);

        assertContentEquals("【一百券】店员工号无效，请核实工号是否正确或是否是肯德基门店", response);
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【一百券】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：4006262166",
                        msg.getContent());
    }

    /**
     * 测试已经消费的券重复验证
     */
    public void testConsumeredECoupon(MessageSender messageSender) {
        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC2.goods.supplierId, kfcClerk.supplier.id);

        ecouponKFC2.status = ECouponStatus.CONSUMED;
        ecouponKFC2.save();

        Http.Response response = messageSender.doMessageSend(ecouponKFC2,
                        kfcClerk.jobNumber + "*20", null);

        assertStatus(200, response);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        assertContentEquals("【一百券】您的券号已消费，无法再次消费。如有疑问请致电：4006262166",
                        response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【一百券】您尾号" + getLastString(ecouponKFC2.eCouponSn, 4)
                        + "券不能重复消费，已于" + df.format(ecouponKFC2.consumedAt)
                        + "在优惠拉消费过", msg.getContent());

        ECoupon ecoupon = ECoupon.findById(ecouponKFC2.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

    }

    /**
     * 测试券过期的情况
     */
    public void testExpiredECoupon(MessageSender messageSender) {
        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC2.goods.supplierId, kfcClerk.supplier.id);

        ecouponKFC2.expireAt = DateUtil.getYesterday();
        ecouponKFC2.save();

        assertEquals(ECouponStatus.UNCONSUMED, ecouponKFC2.status);
        Http.Response response = messageSender.doMessageSend(ecouponKFC2,
                        kfcClerk.jobNumber + "*20", null);

        assertContentEquals("【一百券】您的券号已过期，无法进行消费。如有疑问请致电：4006262166",
                        response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【一百券】您的券号已过期，无法进行消费。如有疑问请致电：4006262166",
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
