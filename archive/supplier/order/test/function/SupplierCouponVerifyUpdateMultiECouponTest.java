package function;

import com.uhuila.common.util.DateUtil;
import controllers.supplier.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponHistoryMessage;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import navigation.RbacLoader;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.mq.MockMQ;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门店验证测试
 * @author tanglq
 */
public class SupplierCouponVerifyUpdateMultiECouponTest extends FunctionalTest {
    Supplier supplier;
    Shop shop;
    // 订单1同组券
    Order order1;
    Goods goods100, goods50;
    Goods singleGoods;

    Category category;
    ECoupon coupon1, coupon2, singleCoupon1;
    SupplierUser supplierUser;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        MockMQ.clear();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        FactoryBoy.create(Supplier.class);
        shop = FactoryBoy.create(Shop.class);
        supplierUser = FactoryBoy.create(SupplierUser.class);

        goods50 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("50");
                g.groupCode = "GROUP1";
            }
        });
        goods100 = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("100");
                g.groupCode = "GROUP1";
            }

        });
        singleGoods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.faceValue = new BigDecimal("75");
            }
        });

        // 设置测试登录的用户名
        Security.setLoginUserForTest(supplierUser.loginName);

        FactoryBoy.create(Account.class, "balanceAccount", new BuildCallback<Account>() {
            @Override
            public void build(Account account) {
                account.amount = new BigDecimal(10000);
            }
        });
    }

    private void generateOrder2With2Group3Single() {
        order1 = FactoryBoy.create(Order.class);

        FactoryBoy.create(OrderItems.class);
        coupon1 = createCoupon(goods100);
        coupon2 = createCoupon(goods100);

        FactoryBoy.create(OrderItems.class);
        singleCoupon1 = createCoupon(singleGoods);
        createCoupon(singleGoods);
        createCoupon(singleGoods);
    }

    protected void generateOrder1WithSameGroupGoods() {
        order1 = FactoryBoy.create(Order.class);

        FactoryBoy.create(OrderItems.class);
        coupon1 = createCoupon(goods100);
        coupon2 = createCoupon(goods100);

        FactoryBoy.create(OrderItems.class);
        createCoupon(goods50);
        createCoupon(goods50);
        createCoupon(goods50);
    }

    public ECoupon createCoupon(final Goods goods) {
        return FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon e) {
                e.goods = goods;
                e.salePrice = goods.salePrice;
                e.faceValue = goods.faceValue;
                e.originalPrice = goods.originalPrice;
            }
        });
    }

    @Test
    public void 购买350元消费275元时验证多张券返回250元已消费券() {
        generateOrder1WithSameGroupGoods();

        verifyECoupon(coupon1, "275.00", "250.00");

        ECouponHistoryMessage lastMessage = (ECouponHistoryMessage) MockMQ.getLastMessage(ECouponHistoryMessage
                .MQ_KEY);
        assertNotNull(lastMessage);
        assertEquals("消费", lastMessage.remark);

        // 得到此次验证前可用的总券数，包括已验证和未验证的
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(5, ecoupons.size());

        // 得到还没有消费的券号列表
        List<String> availableECouponSNs = getAvaiableECouponSNs(ecoupons, 2);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSMessage.SMS2_QUEUE);
        assertSMSContentEquals("您尾号" + coupon1.getLastCode(4)
                + "共3张券(总面值250.00元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。您还有2张券（"
                + StringUtils.join(availableECouponSNs, "/")
                + "总面值100.00元）未消费。如有疑问请致电：4006865151",
                msg.getContent());
    }

    @Test
    public void 购买350元消费400元时验证多张券返回350元已消费券() {
        generateOrder1WithSameGroupGoods();
        verifyECoupon(coupon1, "400.00", "350.00");

        // 得到此次验证前可用的总券数，包括已验证和未验证的
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(5, ecoupons.size());

        // 得到还没有消费的券号列表
        getAvaiableECouponSNs(ecoupons, 0);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSMessage.SMS2_QUEUE);
        assertSMSContentEquals("您尾号" + coupon1.getLastCode(4)
                + "共5张券(总面值350.00元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151",
                msg.getContent());
    }

    @Test
    public void 购买350元消费350元时验证多张券返回350元已消费券() {
        generateOrder1WithSameGroupGoods();
        verifyECoupon(coupon1, "350", "350");

        // 得到此次验证前可用的总券数，包括已验证和未验证的
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(5, ecoupons.size());

        // 得到还没有消费的券号列表
        getAvaiableECouponSNs(ecoupons, 0);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSMessage.SMS2_QUEUE);
        assertSMSContentEquals("您尾号" + coupon1.getLastCode(4)
                + "共5张券(总面值350.00元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151",
                msg.getContent());
    }

    @Test
    public void 购买350元消费40元时验证多张券返回不能验证() {
        generateOrder1WithSameGroupGoods();
        verifyECoupon(coupon1, "40", "0");

        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(5, ecoupons.size());

        // 得到还没有消费的券号列表，应还是5张
        getAvaiableECouponSNs(ecoupons, 5);

        assertEquals(0, MockMQ.size(SMSMessage.SMS2_QUEUE));
    }


    // ===================== 无组商品券测试 =========================

    @Test
    public void 购买225元无组商品券消费185元时验证多张券返回150元已消费券() {
        generateOrder2With2Group3Single();

        verifyECoupon(singleCoupon1, "185.00", "150.00");

        // 得到此次验证前可用的总券数，包括已验证和未验证的
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(3, ecoupons.size());

        // 得到还没有消费的券号列表
        List<String> availableECouponSNs = getAvaiableECouponSNs(ecoupons, 1);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSMessage.SMS2_QUEUE);
        assertSMSContentEquals("您尾号" + singleCoupon1.getLastCode(4)
                + "共2张券(总面值150.00元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。您还有1张券（"
                + StringUtils.join(availableECouponSNs, "/")
                + "总面值75.00元）未消费。如有疑问请致电：4006865151",
                msg.getContent());
    }

    @Test
    public void 购买225元无组商品券消费300元时验证多张券返回225元已消费券() {
        generateOrder2With2Group3Single();
        verifyECoupon(singleCoupon1, "300.00", "225.00");

        // 得到此次验证前可用的总券数，包括已验证和未验证的
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(3, ecoupons.size());

        // 得到还没有消费的券号列表
        getAvaiableECouponSNs(ecoupons, 0);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSMessage.SMS2_QUEUE);
        assertSMSContentEquals("您尾号" + singleCoupon1.getLastCode(4)
                + "共3张券(总面值225.00元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151",
                msg.getContent());
    }

    @Test
    public void 购买225元无组商品券消费225元时验证多张券返回225元已消费券() {
        generateOrder2With2Group3Single();
        verifyECoupon(singleCoupon1, "225.00", "225.00");

        // 得到此次验证前可用的总券数，包括已验证和未验证的
        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(3, ecoupons.size());

        // 得到还没有消费的券号列表
        getAvaiableECouponSNs(ecoupons, 0);

        SMSMessage msg = (SMSMessage)MockMQ.getLastMessage(SMSMessage.SMS2_QUEUE);
        assertSMSContentEquals("您尾号" + singleCoupon1.getLastCode(4)
                + "共3张券(总面值225.00元)于" + DateUtil.getNowTime() + "已成功消费，使用门店：" + shop.name + "。如有疑问请致电：4006865151",
                msg.getContent());
    }

    @Test
    public void 购买225元无组商品券消费50元时验证多张券返回不能验证() {
        generateOrder2With2Group3Single();
        verifyECoupon(singleCoupon1, "50.00", "0.00");

        List<ECoupon> ecoupons = (List<ECoupon>)renderArgs("ecoupons");
        assertNotNull(ecoupons);
        assertEquals(3, ecoupons.size());

        // 得到还没有消费的券号列表，应还是3张
        getAvaiableECouponSNs(ecoupons, 3);

        assertEquals(0, MockMQ.size(SMSMessage.SMS2_QUEUE));
    }

    /**
     * 验证券辅助方法。
     * @param firstEcoupon 其中一张券
     * @param verifyAmount 需要验证的总金额
     * @param expectConsumedAmount  期望验证到的总金额
     */
    protected void verifyECoupon(ECoupon firstEcoupon, String verifyAmount, String expectConsumedAmount) {
        Map<String, String> params = new HashMap<>();
        params.put("shopId", shop.id.toString());
        params.put("eCouponSn", firstEcoupon.eCouponSn);
        params.put("verifyAmount", verifyAmount);
        Http.Response response = POST("/coupons/multi-verify", params);
        assertEquals("HTTP状态码不是200", new Integer(200), response.status);

        if (new BigDecimal(expectConsumedAmount).compareTo(BigDecimal.ZERO) > 0) {
            firstEcoupon.refresh();
            assertEquals("第一张券必须已经验证", ECouponStatus.CONSUMED, firstEcoupon.status);
        }

        BigDecimal consumedAmount = (BigDecimal)renderArgs("consumedAmount");
        assertNotNull("没有得到已验证金额", consumedAmount);
        assertEquals("已验证金额" + consumedAmount + "与期望值" + expectConsumedAmount + "不符", new BigDecimal(expectConsumedAmount).setScale(2), consumedAmount.setScale(2));
    }

    /**
     * 得到还没有消费的券号列表
     * @param ecoupons
     * @return
     */
    protected List<String> getAvaiableECouponSNs(List<ECoupon> ecoupons, int expectAvaiableECouponNumber) {

        List<String> availableECouponSNs = new ArrayList<>();
        for (ECoupon e : ecoupons) {
            ECoupon e1 = ECoupon.findById(e.id);
            e1.refresh();
            if (e1.status == ECouponStatus.UNCONSUMED) {
                availableECouponSNs.add(e1.eCouponSn);
            }
        }

        assertEquals("期望可用券数" + expectAvaiableECouponNumber + ", 实际为" + availableECouponSNs.size(),
                expectAvaiableECouponNumber, availableECouponSNs.size());

        return availableECouponSNs;
    }

    /**
     * 使用正则匹配结果.
     *
     * @param pattern
     * @param content
     */
    public static void assertSMSContentEquals(String pattern, String content) {
        assertEquals("The content (" + content + ") does not match (" + pattern
                + ")", pattern+"【一百券】", content);
    }
}
