package functional;

import java.math.BigDecimal;
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
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import controllers.EnSmsReceivers;

public class ConsumerSmsVerifyBaseTest extends FunctionalTest {

    @Test
    public void 类型检查() {
        assertTrue(new EnSmsReceivers() instanceof Controller);
    }

    /**
     * 使用正则匹配结果.
     * @param pattern
     * @param content
     */
    public static void assertSMSContentMatch(String pattern, String content) {
        assertTrue("短信内容(" + content + ")超过67字符, size:" + content.length(), content.length() <= 67);
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(content).find();
        assertTrue("The content (" + content + ") does not match '" + pattern + "'", ok);
    }
    
    /**
     * 执行特定消息发送代码的接口.
     * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
     *
     */
    public interface MessageSender {
        public Response doMessageSend(ECoupon ecoupon, String msg, String mobile);
    };
    
    public interface InvalidMessageSender {
        public Response doMessageSend(String msg);
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
    
    ECoupon ecouponKFC = null;
    
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


        Long kfcId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc");
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");

        supplierKFC = Supplier.findById(kfcId);
        
        
        Goods goods = Goods.findById(goodsId);
        goods.supplierId = kfcId;
        goods.save();

        Long kfc1Id = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_001");
        goods = Goods.findById(goodsId);
        goods.supplierId = kfc1Id;
        goods.save();

        Long kfc2Id = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc2");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_003");
        goods = Goods.findById(goodsId);
        goods.supplierId = kfc2Id;
        goods.save();

        Long kfc3Id = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc3");
        goodsId = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_004");
        goods = Goods.findById(goodsId);
        goods.supplierId = kfc3Id;
        goods.save();

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("99999");
        account.save();

        Long  goods2Id = (Long) Fixtures.idCache.get("models.sales.Goods-Goods_002");
        Goods goods2 = Goods.findById(goods2Id);
        goods2.supplierId = supplierKFC.id;
        goods2.save();
        
        Long brandId = (Long) play.test.Fixtures.idCache.get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplierKFC;
        brand.save();
        
        Long shopId = (Long) play.test.Fixtures.idCache.get("models.sales.Shop-Shop_4");
        kfcShop = Shop.findById(shopId);
        kfcShop.supplierId = kfcId;
        kfcShop.save();
        
        Long kfcClerkId = (Long) play.test.Fixtures.idCache.get("models.admin.SupplierUser-user2");
        kfcClerk = SupplierUser.findById(kfcClerkId);
        kfcClerk.deleted = DeletedStatus.UN_DELETED;
        kfcClerk.shop = kfcShop;
        kfcClerk.save();

        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ecouponKFC = ECoupon.findById(id);
        
    }

    /**
     * 测试正常验证过程
     * @param sendMessage
     */
    public void testNormalConsumerCheck(MessageSender messageSender) {

        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC.goods.supplierId, kfcClerk.supplier.id);
        
        assertEquals(ECouponStatus.UNCONSUMED, ecouponKFC.status);
        Http.Response response = messageSender.doMessageSend(ecouponKFC, kfcClerk.jobNumber, null);

        assertStatus(200, response); 

        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】您尾号" + getLastString(ecouponKFC.eCouponSn, 4) + "券于\\d+月\\d+日\\d+时\\d+分成功消费，门店：优惠拉。客服4006262166", 
                msg.getContent());
        // 店员短信
        msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】" + getBeginString(ecouponKFC.orderItems.phone, 3) + "\\*\\*\\*\\*\\*" + 
                getLastString(ecouponKFC.orderItems.phone, 3) + "尾号" + getLastString(ecouponKFC.eCouponSn, 4) + "券（面值" +
                		ecouponKFC.faceValue + "元）于\\d+月\\d+日\\d+时\\d+分在优惠拉验证成功。客服4006262166", msg.getContent());

        ECoupon ecoupon = ECoupon.findById(ecouponKFC.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);        
    }
    
    /**
     * 券格式无效的测试
     * @param messageSender
     */
    public void testInvalidFormatMessage(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(ecouponKFC, "abc", null);
        assertEquals("Unsupport Message", response.out.toString());
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                msg.getContent());      
    }

    /**
     * 店员工号不存在.
     * @param messageSender
     */
    public void testNotExistsJobNumber(MessageSender messageSender) {
        Http.Response response = messageSender.doMessageSend(ecouponKFC, "998788", null);

        assertStatus(200, response); 

        // 消费者短信
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】店员工号无效，请核实工号是否正确或是否是" + supplierKFC.fullName + "门店。如有疑问请致电：400-6262-166", 
                msg.getContent());

        ECoupon ecoupon = ECoupon.findById(ecouponKFC.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);          
    }

    /**
     * 商户不存在.
     * @param messageSender
     */
    public void testInvalidSupplier(MessageSender messageSender) {
        Supplier kfc = Supplier.findById(supplierKFC.id);
        kfc.deleted = DeletedStatus.DELETED;
        kfc.save();

        Response response = messageSender.doMessageSend(ecouponKFC, kfcClerk.jobNumber, null);
        assertContentEquals("【券市场】" + kfc.fullName + "未在券市场登记使用", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】" + kfc.fullName + "未在券市场登记使用，请致电400-6262-166咨询",
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

        Response response = messageSender.doMessageSend(ecouponKFC, kfcClerk.jobNumber, null);
        assertContentEquals("【券市场】" + kfc.fullName + "已被券市场锁定", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertSMSContentMatch("【券市场】" + kfc.fullName + "已被券市场锁定，请致电400-6262-166咨询",
                msg.getContent());              
    }

    /**
     * 不是商户品牌的券号
     * @param messageSender
     */
    public void testTheGoodsFromOtherSupplier(MessageSender messageSender) {
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon5");
        ECoupon ecoupon = ECoupon.findById(id);

        Response response = messageSender.doMessageSend(ecoupon, kfcClerk.jobNumber, null);
        
        assertContentEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店", response);
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg.getContent());
    }

    /**
     * 测试已经消费的券重复验证
     */
    public void testConsumeredECoupon(MessageSender messageSender) {
        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC.goods.supplierId, kfcClerk.supplier.id);
        
        ecouponKFC.status = ECouponStatus.CONSUMED;
        ecouponKFC.save();
        
        Http.Response response = messageSender.doMessageSend(ecouponKFC, kfcClerk.jobNumber, null);

        assertStatus(200, response); 
        
        assertContentEquals("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertEquals("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", msg.getContent());
        
        ECoupon ecoupon = ECoupon.findById(ecouponKFC.id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);
        
    }

    /**
     * 测试券过期的情况
     */
    public void testExpiredECoupon(MessageSender messageSender) {
        assertEquals(supplierKFC.id, kfcClerk.supplier.id);
        assertEquals(ecouponKFC.goods.supplierId, kfcClerk.supplier.id);
        
        ecouponKFC.expireAt = DateUtil.getYesterday();
        ecouponKFC.save();
        
        assertEquals(ECouponStatus.UNCONSUMED, ecouponKFC.status);
        Http.Response response = messageSender.doMessageSend(ecouponKFC, kfcClerk.jobNumber, null);

        assertContentEquals("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", response);

        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertEquals("【券市场】您的券号已过期，无法进行消费。如有疑问请致电：400-6262-166", msg.getContent());
    }


    /**
     * 取后length位的字符.
     * @param str
     * @param length
     * @return
     */
    protected String getLastString(String str, int length) {
        return str.substring(str.length() - length);
    }
    
    /**
     * 取前length位的字符.
     * @param str
     * @param length
     * @return
     */
    protected String getBeginString(String str, int length) {
        return str.substring(0, length);
    }
    
}