package unit;

import factory.FactoryBoy;
import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.common.InfoUtil;

/**
 * 券号相关测试
 * User: tanglq
 * Date: 13-1-25
 * Time: 上午11:41
 */
public class ECouponSNUnitTest extends UnitTest {
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testGetSafeECouponSN() throws Exception {
        ECoupon ecoupon = FactoryBoy.build(ECoupon.class);
        ecoupon.eCouponSn = "1324123423 密码234234";
        assertEquals("1324123423", ecoupon.getSafeECouponSN());
    }

    @Test
    public void testGetCharSequence() {
        assertEquals("1341234124", InfoUtil.getFirstCharSequence("1341234124"));
        assertEquals("ABC1341234124", InfoUtil.getFirstCharSequence("ABC1341234124"));
        assertEquals("1341234124", InfoUtil.getFirstCharSequence("1341234124测试"));
        assertEquals("1341234124", InfoUtil.getFirstCharSequence("1341234124 3234234"));
        assertEquals("ad834234234", InfoUtil.getFirstCharSequence("ad834234234 密码31342"));
        assertEquals("1341234124", InfoUtil.getFirstCharSequence("1341234124 密码324234"));
        assertEquals("324234", InfoUtil.getFirstCharSequence("密码324234"));
        assertEquals("测试中文", InfoUtil.getFirstCharSequence("测试中文"));
        assertEquals("测试中文 ", InfoUtil.getFirstCharSequence("测试中文 "));
    }
}
