package functional;

import com.uhuila.common.constants.DeletedStatus;
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
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;

/**
 * <p/>
 * User: yanjy
 * Date: 12-5-22
 * Time: 下午2:09
 */
public class SmsFunctionTest extends FunctionalTest {

    @org.junit.Before
    public void setup() {
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


    @Test
    @Ignore
    public void testClerk() {
        String message = "mobiles=15900002342&msg=#12i34567003#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808" +
                "&dt" +
                "" + "=1319873904&code=1028";
        //msg不符合的情况
        Http.Response response = GET("/getsms?" + message);
        assertEquals("券号无效！", response.out.toString());

        message = "mobiles=15900002342&msg=##&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808" +
                "&dt" +
                "" + "=1319873904&code=1028";
        //msg不符合的情况
        response = GET("/getsms?" + message);
        assertEquals("券号无效！", response.out.toString());

        //msg不存在的情况
        message = "mobiles=15900002342&msg=#11234567003#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);
        assertEquals("【券市场】您输入的券号11234567003不存在，请确认！", response.out.toString());


        //商户不存在
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.deleted = DeletedStatus.DELETED;
        supplier.save();
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon ecoupon = ECoupon.findById(id);
        message = "mobiles=15900002342&msg=#" + ecoupon.eCouponSn +
                "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);
        assertEquals("【券市场】该商户不存在或被删除了！，请确认！", response.out.toString());


        //商户被冻结的情况
        supplier = Supplier.findById(supplierId);
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.status = SupplierStatus.FREEZE;
        supplier.save();
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        ecoupon = ECoupon.findById(id);
        message = "mobiles=15900002342&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);
        assertEquals("【券市场】该商户已被锁定，请确认！", response.out.toString());


        //店员不符合的情况
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        ecoupon = ECoupon.findById(id);

        supplier = Supplier.findById(supplierId);
        supplier.status = SupplierStatus.NORMAL;
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.save();
        message = "mobiles=15900002342&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);
        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", response.out.toString());
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg);
        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg.getContent());

        //店员符合
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ecoupon = ECoupon.findById(id);

        supplierId = (Long) play.test.Fixtures.idCache.get("models.supplier.Supplier-kfc");
        Long shopId = (Long) play.test.Fixtures.idCache.get("models.sales.Shop-Shop_4");
        Shop shop = Shop.findById(shopId);
        shop.supplierId = supplierId;
        shop.save();

        Long brandId = (Long) play.test.Fixtures.idCache.get("models.sales.Brand-Brand_2");
        Brand brand = Brand.findById(brandId);
        brand.supplier = supplier;
        brand.save();

        message = "mobiles=15900002342&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
        response = GET("/getsms?" + message);

        ecoupon = ECoupon.findById(id);
        ecoupon.refresh();

//        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

//        msg = MockSMSProvider.getLastSMSMessage();
//        assertNotNull("【券市场】您尾号7002的券号于4月23日19时41分已成功消费，使用门店：优惠拉。如有疑问请致电：400-6262-166", msg);
//        assertEquals("【券市场】您尾号7002的券号于4月23日19时41分已成功消费，使用门店：优惠拉。如有疑问请致电：400-6262-166", msg.getContent());
//
//        msg = MockSMSProvider.getLastSMSMessage();
//        assertNotNull("【券市场】,159*****342消费者的尾号7002的券（面值：10.00元）于4月23日19时44分已验证成功，使用门店：优惠拉。客服热线：400-6262-166", msg);
//        assertEquals("【券市场】,159*****342消费者的尾号7002的券（面值：10.00元）于4月23日19时44分已验证成功，使用门店：优惠拉。客服热线：400-6262-166", msg.getContent());


        //不是商户品牌的券号
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon5");
        ecoupon = ECoupon.findById(id);
        message = "mobiles=15900002341&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";

        response = GET("/getsms?" + message);

        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", response.out.toString());
        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg);
        assertEquals("【券市场】店员工号无效，请核实工号是否正确或是否是肯德基门店。如有疑问请致电：400-6262-166", msg.getContent());


        //已消费的验证
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon4");
        ecoupon = ECoupon.findById(id);
        message = "mobiles=15800002341&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);
        response = GET("/getsms?" + message);

        ecoupon = ECoupon.findById(id);
        ecoupon.refresh();
        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);
        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166");
        assertEquals("【券市场】您的券号已消费，无法再次消费。如有疑问请致电：400-6262-166", msg.getContent());


    }


    @Test
    public void testConsumer() {
        String message = "mobiles=15900002342&msg=#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808" +
                "&dt" +
                "" + "=1319873904&code=1028";
        //msg不符合的情况
        Http.Response response = GET("/getsms?" + message);
//        assertEquals("msg is wrong", response.out.toString());
//
//        //msg不存在的情况
//        message = "mobiles=15900002342&msg=7003&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
//                "=1319873904&code=1028";
//        response = GET("/getsms?" + message);
//        assertEquals("Not Found the coupon", response.out.toString());


        //店员不符合的情况
        Long id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon3");
        ECoupon ecoupon = ECoupon.findById(id);
        Long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.status = SupplierStatus.NORMAL;
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.save();

        message = "mobiles=15900002342&msg=#5001&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);


    }
}
