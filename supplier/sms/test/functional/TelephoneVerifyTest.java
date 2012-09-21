package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.TelephoneVerify;
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
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author likang
 */
public class TelephoneVerifyTest extends FunctionalTest{
    @Before
    public void setup(){
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

        Long shopId = (Long) Fixtures.idCache.get("models.sales.Shop-Shop_5");
        Shop shop = Shop.findById(shopId);

        Long supplierUserId = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user1");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        supplierUser.supplier = Supplier.findById(supplierId);
        supplierUser.shop = shop;
        supplierUser.save();

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
    public void testParams(){
        String caller = "1";
        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("1", response);//;主叫号码无效

        response = GET("/tel-verify?caller=" + caller + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("2", response);//;券号无效

        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&sign=" + sign);
        assertContentEquals("3", response);//;时间戳无效

        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp);
        assertContentEquals("4", response);//;签名无效


        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + DigestUtils.md5Hex("wrongpasswd" + timestamp));
        assertContentEquals("6", response);//;签名错误

        timestamp = timestamp - 500000;
        sign = getSign(timestamp);
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("5", response);//;请求超时
    }

    @Test
    public void testNoSuchCoupon(){
        long couponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon eCoupon = ECoupon.findById(couponId);
        eCoupon.delete();

        String caller = "1";
        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("7", response);//;对不起，未找到此券
    }

    @Test
    public void testInvalidSupplier(){
        long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.deleted = DeletedStatus.DELETED;
        supplier.save();

        String caller = "1";
        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("8", response);//;对不起，商户不存在

        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.status = SupplierStatus.FREEZE;
        supplier.save();

        response = GET("/tel-verify?caller=" + caller +  "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("8", response);//;对不起，商户不存在

        supplier.status = SupplierStatus.NORMAL;
        supplier.save();

        ECoupon eCoupon = ECoupon.find("byECouponSn", coupon).first();
        Long originSupplierId = eCoupon.goods.supplierId;
        eCoupon.goods.supplierId = originSupplierId + 1L;
        eCoupon.goods.save();

        response = GET("/tel-verify?caller=" + caller +  "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("7", response);//;对不起，券不存在

        eCoupon.goods.supplierId = originSupplierId;
        eCoupon.goods.save();

        Long supplierUserId = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user1");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        supplierUser.delete();
        supplier.delete();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("8", response);//;对不起，商户不存在
    }

    @Test
    public void testCouponStatus(){
        long couponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon eCoupon = ECoupon.findById(couponId);

        String caller = "1";
        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);


        Long supplierUserId = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user1");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);

        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.save();
        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("10", response);//;该券无法重复消费。消费时间为" + new SimpleDateFormat("yyyy年MM月dd日hh点mm分").format(eCoupon.consumedAt)

        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.save();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("0", response);//;消费成功，价值" + eCoupon.faceValue + "元"


        /*
        eCoupon.expireAt = new Date(System.currentTimeMillis()/1000 - 30000);
        eCoupon.save();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("对不起，该券已过期", response);
        */
    }

    @Test
    public void testFaceValueParams(){
        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify/face-value?timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("券号无效", response);//;券号无效

        response = GET("/tel-verify/face-value?coupon=" + coupon + "&sign=" + sign);
        assertContentEquals("时间戳无效", response);//;时间戳无效

        response = GET("/tel-verify/face-value?coupon=" + coupon + "&timestamp=" + timestamp);
        assertContentEquals("签名无效", response);//;签名无效


        response = GET("/tel-verify/face-value?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + DigestUtils.md5Hex("wrongpasswd" + timestamp));
        assertContentEquals("签名错误", response);//;签名错误

        timestamp = timestamp - 500000;
        sign = getSign(timestamp);
        response = GET("/tel-verify/face-value?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("请求超时", response);//;请求超时
    }

    @Test
    public void testFacevalue(){
        long couponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon eCoupon = ECoupon.findById(couponId);

        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify/face-value?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("" + eCoupon.faceValue.intValue(), response);//
    }

    @Test
    public void testConsumedAtParams(){
        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify/consumed-at?timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("券号无效", response);//;券号无效

        response = GET("/tel-verify/consumed-at?coupon=" + coupon + "&sign=" + sign);
        assertContentEquals("时间戳无效", response);//;时间戳无效

        response = GET("/tel-verify/consumed-at?coupon=" + coupon + "&timestamp=" + timestamp);
        assertContentEquals("签名无效", response);//;签名无效


        response = GET("/tel-verify/consumed-at?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + DigestUtils.md5Hex("wrongpasswd" + timestamp));
        assertContentEquals("签名错误", response);//;签名错误

        timestamp = timestamp - 500000;
        sign = getSign(timestamp);
        response = GET("/tel-verify/consumed-at?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("请求超时", response);//;请求超时
    }

    @Test
    public void testConsumedAt(){
        long couponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon eCoupon = ECoupon.findById(couponId);

        String coupon = "1253678001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Date consumedAt = new Date();
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.consumedAt = consumedAt;
        eCoupon.save();

        Http.Response response = GET("/tel-verify/consumed-at?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals(new SimpleDateFormat("M月d日H点m分").format(consumedAt), response);//;该券无法重复消费。消费时间为" + new SimpleDateFormat("yyyy年MM月dd日hh点mm分").format(eCoupon.consumedAt)
    }
    private String getSign(long timestamp){
        return DigestUtils.md5Hex(TelephoneVerify.APP_KEY + timestamp);
    }
}
