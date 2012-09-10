package functional;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import controllers.EnSmsReceivers;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.consumer.User;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.*;
import models.sms.MockSMSProvider;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.junit.Test;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * <p/>
 * User: yanjy
 * Date: 12-5-22
 * Time: 下午2:09
 */
public class ClerkSmsVerifyBaseTest extends FunctionalTest {

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
        public Response doMessageSend(String mobile, ECoupon ecoupon);
    }

    ;

    public interface InvalidMessageSender {
        public Response doMessageSend(String msg);
    }

    /**
     * 准备测试数据的公共方法。
     */
    protected void setupTestData() {
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
                "fixture/orderItems.yml");


        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");

        Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc2");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");
        goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc3");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("99999");
        account.save();
    }

    /**
     * 测试正常验证过程
     *
     * @param sendMessage
     */
    public void testNormalClerkCheck(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ECoupon ecoupon = ECoupon.findById(id);

        Long supplierId = (Long) play.test.Fixtures.idCache.get("models.supplier.Supplier-kfc");
        Supplier supplier = Supplier.findById(supplierId);
        Long shopId = (Long) play.test.Fixtures.idCache.get("models.sales.Shop-Shop_4");
        Shop shop = Shop.findById(shopId);
        shop.supplierId = supplierId;
        shop.save();

        Long brandId = (Long) play.test.Fixtures.idCache.get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplier;
        brand.save();

        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
        Http.Response response = messageSender.doMessageSend("15900002342", ecoupon);

        assertStatus(200, response);

        ecoupon = ECoupon.findById(id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】您尾号" + getLastString(ecoupon.eCouponSn, 4) + "券于\\d+月\\d+日\\d+时\\d+分成功消费，门店：优惠拉。客服4006262166",
                msg.getContent());
        // 店员短信
        msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】" + getBeginString(ecoupon.orderItems.phone, 3) + "\\*\\*\\*\\*\\*" +
                getLastString(ecoupon.orderItems.phone, 3) + "尾号" + getLastString(ecoupon.eCouponSn, 4) + "券（面值" +
                ecoupon.faceValue + "元）于\\d+月\\d+日\\d+时\\d+分在优惠拉验证成功。客服4006262166", msg.getContent());
    }

    /**
     * 测试正常验证过程中不再验证时间范围内
     *
     * @param sendMessage
     */
    public void testNotInVerifyTime(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ECoupon ecoupon = ECoupon.findById(id);
        ecoupon.status = ECouponStatus.UNCONSUMED;
        ecoupon.save();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());

        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        Goods goods = Goods.findById(goodsId);
        String week = "";
        String day = "";
        ca.add(Calendar.DAY_OF_MONTH, -1);
        week = String.valueOf(getWeek(ca.get(Calendar.DAY_OF_WEEK)));
        ca.add(Calendar.DAY_OF_MONTH, +1);
        week += "," + String.valueOf(getWeek(ca.get(Calendar.DAY_OF_WEEK)));
        goods.useWeekDay = week;
        ca.set(Calendar.HOUR_OF_DAY, 1);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 2);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();
        goods.refresh();

        Long supplierId = (Long) play.test.Fixtures.idCache.get("models.supplier.Supplier-kfc");
        Supplier supplier = Supplier.findById(supplierId);
        Long shopId = (Long) play.test.Fixtures.idCache.get("models.sales.Shop-Shop_4");
        Shop shop = Shop.findById(shopId);
        shop.supplierId = supplierId;
        shop.save();

        Long brandId = (Long) play.test.Fixtures.idCache.get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplier;
        brand.save();

        Http.Response response = messageSender.doMessageSend("15900002342", ecoupon);

        assertStatus(200, response);
        day = ecoupon.getWeek();
        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】对不起，只能在" + day.substring(0, day.length() - 1) + "的" + goods.useBeginTime + "~" + goods.useEndTime + "时间内使用该券 !", msg.getContent());

        goods = Goods.findById(goodsId);
        ca = Calendar.getInstance();
        ca.add(Calendar.DAY_OF_MONTH, -3);
        goods.useWeekDay = String.valueOf(ca.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 23);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 2);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();
        response = messageSender.doMessageSend("15900002342", ecoupon);
        day = ecoupon.getWeek();
        assertStatus(200, response);
        msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】对不起，只能在" + day.substring(0, day.length() - 1) + "的" + goods.useBeginTime + "~次日" + goods.useEndTime + "时间内使用该券 !", msg.getContent());


        goods = Goods.findById(goodsId);
        goods.useWeekDay = "1,2,3,4,5,6,7";
        ca.set(Calendar.HOUR_OF_DAY, 8);
        goods.useBeginTime = df.format(ca.getTime());
        ca.set(Calendar.HOUR_OF_DAY, 9);
        goods.useEndTime = df.format(ca.getTime());
        goods.save();
        response = messageSender.doMessageSend("15900002342", ecoupon);

        assertStatus(200, response);
        msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】对不起，只能在每天的" + goods.useBeginTime + "~" + goods.useEndTime + "时间内使用该券 !", msg.getContent());
    }

    private int getWeek(int w){
        if (w == 1){
            return 7;
        }else if(w >1 && w < 8){
            return w -1;
        }else {
            return -1;
        }
    }

    /**
     * 券格式无效的测试
     *
     * @param messageSender
     */
    public void testInvalidFormatMessage(InvalidMessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend("abc");
        assertEquals("Unsupport Message", response.out.toString());
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                msg.getContent());
    }

    /**
     * 券冻结的测试
     *
     * @param messageSender
     */
    public void testFreezedECoupon(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        ECoupon ecoupon = ECoupon.findById(id);
        ecoupon.isFreeze = 1;
        ecoupon.save();
        Response response = messageSender.doMessageSend("15900002342", ecoupon);
        assertContentEquals("【券市场】该券已被冻结", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【券市场】该券已被冻结,如有疑问请致电：400-6262-166", msg.getContent());
    }

    /**
     * 券不存在.
     *
     * @param messageSender
     */
    public void testEcouponNotExists(InvalidMessageSender messageSender) {
        String couponNumber = "1123456700";
        Http.Response response = messageSender.doMessageSend("#" + couponNumber);
        assertContentEquals("【券市场】您输入的券号" + couponNumber + "不存在，请确认！", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】您输入的券号" + couponNumber + "不存在，请与顾客确认，如有疑问请致电：400-6262-166",
                msg.getContent());
    }

    /**
     * 商户不存在.
     *
     * @param messageSender
     */
    public void testInvalidSupplier(MessageSender messageSender) {
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.deleted = DeletedStatus.DELETED;
        supplier.save();
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon ecoupon = ECoupon.findById(id);

        Response response = messageSender.doMessageSend("15900002342", ecoupon);
        assertContentEquals("【券市场】" + supplier.fullName + "未在券市场登记使用", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】" + supplier.fullName + "未在券市场登记使用，如有疑问请致电：400-6262-166",
                msg.getContent());
    }

    /**
     * 商户被冻结.
     */
    public void testLockedSupplier(MessageSender messageSender) {
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.status = SupplierStatus.FREEZE;
        supplier.save();
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        ECoupon ecoupon = ECoupon.findById(id);

        Response response = messageSender.doMessageSend("15900002342", ecoupon);
        assertContentEquals("【券市场】" + supplier.fullName + "已被券市场锁定", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】" + supplier.fullName + "已被券市场锁定，如有疑问请致电：400-6262-166",
                msg.getContent());
    }

    /**
     * 店号工号无效.
     */
    public void testInvalidClerk(MessageSender messageSender) {
        //店员不符合的情况
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");

        ECoupon ecoupon = ECoupon.findById(id);

        Supplier supplier = Supplier.findById(supplierId);
        supplier.status = SupplierStatus.NORMAL;
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.save();

        Response response = messageSender.doMessageSend("15900002342", ecoupon);
        assertContentEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg.getContent());
    }

    /**
     * 不是商户品牌的券号
     *
     * @param messageSender
     */
    public void testTheGoodsFromOtherSupplier(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon5");
        ECoupon ecoupon = ECoupon.findById(id);

        Response response = messageSender.doMessageSend("15900002342", ecoupon);

        assertContentEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店", response);
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg.getContent());
    }

    /**
     * 测试已经消费的券重复验证
     */
    public void testConsumeredECoupon(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon4");

        Long supplierId = (Long) play.test.Fixtures.idCache.get("models.supplier.Supplier-kfc3");
        Supplier supplier = Supplier.findById(supplierId);
        Long shopId = (Long) play.test.Fixtures.idCache.get("models.sales.Shop-Shop_4");
        Shop shop = Shop.findById(shopId);
        shop.supplierId = supplierId;
        shop.save();

        Long brandId = (Long) play.test.Fixtures.idCache.get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplier;
        brand.save();

        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();
        ECoupon ecoupon = ECoupon.findById(id);

        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        Http.Response response = messageSender.doMessageSend("15800002341", ecoupon);
        assertContentEquals("【券市场】券号" + ecoupon.eCouponSn + "已消费，无法再次消费", response);

        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
        ecoupon = ECoupon.findById(id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【券市场】158*****341尾号" + getLastString(ecoupon.eCouponSn, 4) + "券（" + ecoupon
                .faceValue + "元）不能重复消费，已于" + df.format(ecoupon.consumedAt) + "在优惠拉消费过", msg.getContent());
    }

    /**
     * 测试券过期的情况
     */
    public void testExpiredECoupon(MessageSender messageSender) {

        //已过期的验证
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon4");

        Long supplierId = (Long) play.test.Fixtures.idCache.get("models.supplier.Supplier-kfc3");
        Supplier supplier = Supplier.findById(supplierId);
        Long shopId = (Long) play.test.Fixtures.idCache.get("models.sales.Shop-Shop_4");
        Shop shop = Shop.findById(shopId);
        shop.supplierId = supplierId;
        shop.save();

        Long brandId = (Long) play.test.Fixtures.idCache.get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplier;
        brand.save();

        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        Goods goods = Goods.findById(goodsId);
        goods.supplierId = supplierId;
        goods.save();
        ECoupon ecoupon = ECoupon.findById(id);
        ecoupon.expireAt = DateUtil.getYesterday();
        ecoupon.save();

        Http.Response response = messageSender.doMessageSend("15800002341", ecoupon);
        assertContentEquals("【券市场】券号" + ecoupon.eCouponSn + "已过期，无法进行消费", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentLength(msg.getContent());
        assertEquals("【券市场】券号" + ecoupon.eCouponSn + "已过期，无法进行消费。如有疑问请致电：400-6262-166", msg.getContent());
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
