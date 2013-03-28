package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.TelephoneVerify;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.sales.Shop;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author likang
 */
public class TelephoneVerifyTest extends FunctionalTest{
    
    Shop shop;
    
    @Before
    public void setup(){
        FactoryBoy.deleteAll();
        shop = FactoryBoy.create(Shop.class);
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(SupplierUser.class, new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser target) {
                target.loginName = "02183135817";
            }
        });

        Account account = AccountUtil.getPlatformIncomingAccount();
        account.amount = new BigDecimal("99999");
        account.save();
    }

    @Test
    public void testParams(){
        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = FactoryBoy.last(ECoupon.class).eCouponSn;
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
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);
        assertNotNull(eCoupon);
        eCoupon.delete();

        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("8", response);//;对不起，未找到此券
    }

    @Test
    public void testInvalidSupplier(){
        Supplier supplier = FactoryBoy.last(Supplier.class);
        supplier.deleted = DeletedStatus.DELETED;
        supplier.save();

        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = FactoryBoy.last(ECoupon.class).eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("7", response);//;对不起，商户不存在

        supplier.deleted = DeletedStatus.UN_DELETED;
        supplier.status = SupplierStatus.FREEZE;
        supplier.save();

        response = GET("/tel-verify?caller=" + caller +  "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("7", response);//;对不起，商户不存在

        supplier.status = SupplierStatus.NORMAL;
        supplier.save();

        ECoupon eCoupon = ECoupon.find("byECouponSn", coupon).first();
        Long originSupplierId = eCoupon.goods.supplierId;
        eCoupon.goods.supplierId = originSupplierId + 1L;
        eCoupon.goods.save();

        response = GET("/tel-verify?caller=" + caller +  "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("9", response);//;对不起，券不存在

        eCoupon.goods.supplierId = originSupplierId;
        eCoupon.goods.save();

        SupplierUser supplierUser = FactoryBoy.last(SupplierUser.class);
        supplierUser.delete();
        supplier.delete();
        response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("7", response);//;对不起，商户不存在
    }

    @Test
    public void testCouponStatus(){
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

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
    public void test少一位也能验证(){
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = eCoupon.eCouponSn.substring(1);
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("0", response);//;消费成功，价值" + eCoupon.faceValue + "元"
    }

    @Test
    public void test少两位就不能验证了(){
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = eCoupon.eCouponSn.substring(2);
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify?caller=" + caller + "&coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("8", response);
    }

    @Test
    public void testFaceValueParams(){
        String coupon = FactoryBoy.last(ECoupon.class).eCouponSn;
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
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);
        eCoupon.triggerCouponSn = eCoupon.eCouponSn;
        eCoupon.save();

        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify/face-value?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("" + eCoupon.faceValue.intValue(), response);//
    }

    @Test
    public void test查询面值时少一位(){
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);
        eCoupon.triggerCouponSn = eCoupon.eCouponSn;
        eCoupon.save();

        String coupon = eCoupon.eCouponSn.substring(1);
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify/face-value?coupon=" + coupon + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals("" + eCoupon.faceValue.intValue(), response);//
    }

    @Test
    public void testConsumedAtParams(){
        String coupon = FactoryBoy.last(ECoupon.class).eCouponSn;
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
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Date consumedAt = new Date();
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.consumedAt = consumedAt;
        eCoupon.shop = shop;
        eCoupon.save();

        Http.Response response = GET("/tel-verify/consumed-at?coupon=" + eCoupon.eCouponSn + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals(new SimpleDateFormat("M月d日H点m分").format(consumedAt) + ",消费门店 " + shop.name,
                response);//;该券无法重复消费。消费时间为" + new SimpleDateFormat("yyyy年MM月dd日hh点mm分").format(eCoupon.consumedAt)
    }

    @Test
    public void test查询消费时间时少一位(){
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Date consumedAt = new Date();
        eCoupon.status = ECouponStatus.CONSUMED;
        eCoupon.consumedAt = consumedAt;
        eCoupon.shop = shop;
        eCoupon.save();

        Http.Response response = GET("/tel-verify/consumed-at?coupon=" + eCoupon.eCouponSn.substring(1) + "&timestamp=" + timestamp + "&sign=" + sign);
        assertContentEquals(new SimpleDateFormat("M月d日H点m分").format(consumedAt) + ",消费门店 " + shop.name,
                response);//;该券无法重复消费。消费时间为" + new SimpleDateFormat("yyyy年MM月dd日hh点mm分").format(eCoupon.consumedAt)
    }
    private String getSign(long timestamp){
        return DigestUtils.md5Hex(TelephoneVerify.APP_KEY + timestamp);
    }
}
