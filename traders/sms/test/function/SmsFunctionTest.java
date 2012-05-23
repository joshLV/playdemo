package function;

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
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sms.MockSMSProvider;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
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
    public void testClerk() {
        String message = "mobiles=15900002342&msg=#12i34567003#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808" +
                "&dt" +
                "" + "=1319873904&code=1028";
        //msg不符合的情况
        Http.Response response = GET("/getsms?" + message);
        assertEquals("msg is wrong", response.out.toString());

       message = "mobiles=15900002342&msg=##&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808" +
                "&dt" +
                "" + "=1319873904&code=1028";
        //msg不符合的情况
        response = GET("/getsms?" + message);
        assertEquals("msg is wrong", response.out.toString());

             //msg不存在的情况
        message = "mobiles=15900002342&msg=#11234567003#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);
        assertEquals("Not Found the coupon", response.out.toString());


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
        assertEquals("Not Found the supplier", response.out.toString());


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
        assertEquals("The supplier was freeze!", response.out.toString());


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
        assertEquals("该商户下没有此店员信息！", response.out.toString());
        SMSMessage msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("没有找到对应的店员信息，请确认您是否商户<肯德基>的店员，谢谢！", msg);
        assertEquals("没有找到对应的店员信息，请确认您是否商户<肯德基>的店员，谢谢！", msg.getContent());

        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("请核对你的信息,重新发送短信！", msg);
        assertEquals("请核对你的信息,重新发送短信！", msg.getContent());

        //店员符合
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon2");
        ecoupon = ECoupon.findById(id);
        message = "mobiles=15900002342&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        assertEquals(ECouponStatus.UNCONSUMED, ecoupon.status);
        response = GET("/getsms?" + message);

        ecoupon = ECoupon.findById(id);
        ecoupon.refresh();

        assertEquals(ECouponStatus.CONSUMED, ecoupon.status);

        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("你的券号:******7002(面值10.00元)已经消费成功", msg);
        assertEquals("你的券号:******7002(面值10.00元)已经消费成功", msg.getContent());

        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("券号:******7002,(面值10.00元)消费成功", msg);
        assertEquals("券号:******7002,(面值10.00元)消费成功", msg.getContent());


        //不是商户品牌的券号
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon5");
        ecoupon = ECoupon.findById(id);
        message = "mobiles=15900002341&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";

        response = GET("/getsms?" + message);
        assertEquals("对不起，该券不是该商户品牌的，请确认！", response.out.toString());
        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("对不起，该券不是该商户品牌下的，请确认！", msg);
        assertEquals("对不起，该券不是该商户品牌下的，请确认！", msg.getContent());

        //无门店设置
        id = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon5");
        ecoupon = ECoupon.findById(id);
        message = "mobiles=15900002341&msg=#" + ecoupon.eCouponSn + "#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";

        supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc2");
        Long brandId = (Long) Fixtures.idCache.get("models.sales.Brand-Brand_3");
        Brand brand = Brand.findById(brandId);
        brand.supplier = Supplier.findById(brandId);
        brand.save();
        response = GET("/getsms?" + message);

        assertEquals("无门店设置，请确认，谢谢！", response.out.toString());
        msg = MockSMSProvider.getLastSMSMessage();
        assertNotNull("商户<肯德基>无门店设置，请确认，谢谢！", msg);
        assertEquals("商户<肯德基>无门店设置，请确认，谢谢！", msg.getContent());

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
        assertNotNull("你的券号:******7004(面值10.00元)不能消费，状态码：CONSUMED");
        assertEquals("你的券号:******7004(面值10.00元)不能消费，状态码：CONSUMED", msg.getContent());


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
        Long   supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.status = SupplierStatus.NORMAL;
        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.save();

        message = "mobiles=15900002342&msg=#5001&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                "=1319873904&code=1028";
        response = GET("/getsms?" + message);

        

    }
}
