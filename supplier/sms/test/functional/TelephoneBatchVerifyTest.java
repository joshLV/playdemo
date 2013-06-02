package functional;

import controllers.TelephoneVerify;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.operator.Operator;
import models.order.ECoupon;
import models.order.OrderItems;
import models.supplier.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author likang
 *         Date: 12-11-22
 */
@Ignore
public class TelephoneBatchVerifyTest extends FunctionalTest{
    @Before
    public void setup(){
        FactoryBoy.deleteAll();
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(OrderItems.class);
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(SupplierUser.class);

        Account account = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        account.amount = new BigDecimal("99999");
        account.save();

    }
    @Test
    public void 测试验证时返回此为多账券() {
        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify"
                + "?caller=" + caller
                + "&coupon=" + coupon
                + "&timestamp=" + timestamp
                + "&sign=" + sign);
        assertContentEquals("100", response);
    }

    @Test
    public void 测试消费并查询一次消费总额() {
        List<ECoupon> allCoupon = ECoupon.findAll();
        BigDecimal value = BigDecimal.ZERO;
        for (ECoupon coupon : allCoupon) {
            value = value.add(coupon.faceValue);
        }

        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);
        String caller = FactoryBoy.last(SupplierUser.class).loginName;
        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify"
                + "?caller="    + caller
                + "&coupon="    + coupon
                + "&timestamp=" + timestamp
                + "&sign="      + sign
                + "&value="     + value
        );
        assertContentEquals("0", response);

        response = GET("/tel-verify/face-value"
                + "?coupon="    + coupon
                + "&timestamp=" + timestamp
                + "&sign="      + sign
        );
        assertContentEquals(String.valueOf(value.intValue()), response);
    }

    @Test
    public void 测试批量券信息() {
        List<ECoupon> allCoupon = ECoupon.findAll();
        BigDecimal value = BigDecimal.ZERO;
        for (ECoupon coupon : allCoupon) {
            value = value.add(coupon.faceValue);
        }

        ECoupon eCoupon = FactoryBoy.last(ECoupon.class);

        Supplier supplier = FactoryBoy.last(Supplier.class);
        String coupon = eCoupon.eCouponSn;
        Long timestamp = System.currentTimeMillis()/1000;
        String sign = getSign(timestamp);

        Http.Response response = GET("/tel-verify/batch-info"
                + "?coupon="    + coupon
                + "&timestamp=" + timestamp
                + "&sign="      + sign);
        assertContentEquals(supplier.otherName
                + "|" + allCoupon.size()
                + "|" + value.intValue(), response);
    }

    private String getSign(long timestamp){
        return DigestUtils.md5Hex(TelephoneVerify.APP_KEY + timestamp);
    }
}
