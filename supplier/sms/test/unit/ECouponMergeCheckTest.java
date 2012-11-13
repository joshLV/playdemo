package unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import models.order.ECoupon;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class ECouponMergeCheckTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }
    
    @Test
    public void testSelectCheckEcoupons() {
        List<ECoupon> ecoupons = createEcoupons("1234", new String[]{"100", "100", "100", "50", "20", "20"});
        assertArrayEquals(new String[]{"100", "100", "100", "50", "20", "20"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("500"), ecoupons)));
        assertArrayEquals(new String[]{"100", "100", "20"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("230"), ecoupons)));
        assertArrayEquals(new String[]{"100", "100", "50"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("260"), ecoupons)));
        assertArrayEquals(new String[]{"100"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("112"), ecoupons)));
        assertArrayEquals(new String[]{"50", "20"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("80"), ecoupons)));
        assertArrayEquals(new String[]{"50"}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("60"), ecoupons)));
        assertArrayEquals(new String[]{}, toPriceStringArray(ECoupon.selectCheckECoupons(new BigDecimal("3"), ecoupons)));
    }
    
    private List<ECoupon> createEcoupons(String replyCode, String[] prices) {
        List<ECoupon> ecoupons = new ArrayList<>();
        for (int i = 0; i < prices.length; i++) {
            ECoupon ecoupon = createEcoupon(replyCode, prices[i]);
            ecoupons.add(ecoupon);
        }
        return ecoupons;
    }
    
    private ECoupon createEcoupon(final String replyCode, final String price) {

        ECoupon ecoupon = FactoryBoy.create(ECoupon.class, new BuildCallback<ECoupon>() {
            @Override
            public void build(ECoupon target) {
                target.faceValue = new BigDecimal(price); // 方便测试所以用String
                target.replyCode = replyCode;
            }
        });

        return ecoupon;
    }
    
    private String[] toPriceStringArray(List<ECoupon> ecoupons) {
        String[] results = new String[ecoupons.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = ecoupons.get(i).faceValue.toString();
        }
        return results;
    }
    
}
