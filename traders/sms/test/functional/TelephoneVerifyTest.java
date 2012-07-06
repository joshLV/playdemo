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
        String coupon = "1234567001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("1;主叫号码无效", response);

        response = GET("/tel-verify?caller=" + caller + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("3;券号无效", response);

        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&sign=" + sign);
        assertContentEquals("4;时间戳无效", response);

        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp);
        assertContentEquals("5;签名无效", response);


        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + DigestUtils.md5Hex("wrongpasswd" + timestamp));
        assertContentEquals("7;签名错误", response);

        timestamp = timestamp - 500000;
        sign = getSign(timestamp);
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("6;请求超时", response);
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
        assertContentEquals("8;对不起，未找到此券", response);
    }

    @Test
    public void testInvalidSupplier(){
        long supplierId = (Long) Fixtures.idCache.get("models.supplier.Supplier-kfc1");
        Supplier supplier = Supplier.findById(supplierId);
        supplier.deleted = DeletedStatus.DELETED;
        supplier.save();

        String caller = "1";
        String coupon = "1234567001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("9;对不起，商户不存在", response);

        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.status = SupplierStatus.FREEZE;
        supplier.save();

        response = GET("/tel-verify?caller=" + caller +  "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("9;对不起，商户不存在", response);

        supplier.status = SupplierStatus.NORMAL;
        supplier.save();

        Long supplierUserId = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user1");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);
        supplierUser.delete();
        supplier.delete();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("9;对不起，商户不存在", response);
    }

    @Test
    public void testCouponStatus(){
        long couponId = (Long) Fixtures.idCache.get("models.order.ECoupon-coupon1");
        ECoupon eCoupon = ECoupon.findById(couponId);

        String caller = "1";
        String coupon = "1234567001";
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);


        Long supplierUserId = (Long) Fixtures.idCache.get("models.admin.SupplierUser-user1");
        SupplierUser supplierUser = SupplierUser.findById(supplierUserId);

        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.save();
        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("11;该券无法重复消费。消费时间为" + new SimpleDateFormat("yyyy年MM月dd日hh点mm分").format(eCoupon.consumedAt), response);

        eCoupon.status = ECouponStatus.UNCONSUMED;
        eCoupon.save();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("0;消费成功，价值" + eCoupon.faceValue + "元", response);


        /*
        eCoupon.expireAt = new Date(System.currentTimeMillis()/1000 - 30000);
        eCoupon.save();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("对不起，该券已过期", response);
        */
    }

    private String getSign(long timestamp){
        return DigestUtils.md5Hex(TelephoneVerify.APP_KEY + timestamp);
    }
}
