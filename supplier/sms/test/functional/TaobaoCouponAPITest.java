package functional;

import models.taobao_coupon.TaobaoCouponUtil;
import org.junit.Before;
import org.junit.Test;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-11-30
 */
public class TaobaoCouponAPITest extends FunctionalTest{
    @Before
    public void setup() {

    }

    @Test
    public void testSend() {

    }

    @Test
    public void testSign() {
        /**
         */
        Map<String, String> params = new HashMap<>();
        params.put("valid_ends","2013-01-11 23:59:59");
        params.put("outer_iid", "32");
        params.put("item_title","测试用 拍下概不负责");
        params.put("taobao_sid","828005208");
        params.put( "order_id", "259599322131179");
        params.put("send_type","2");
        params.put("timestamp","2012-11-30 17:16:01");
        params.put("sign","8D5F91B2FC6D2E8843461AB1D8F605B1");
        params.put("num","1");
        params.put("consume_type","0");
        params.put("valid_start","2012-11-30 00:00:00");
        params.put("token","a6670b68da9bdb8c6365c49b71e395b7");
        params.put("method","send");
        params.put("num_iid","21339404852");
        params.put("sms_template","验证码$code.您已成功订购券生活8提供的测试用 拍下概不负责,有效期2012/11/30至2013/01/11,消费时请出示本短信以验证.淘宝客服电话057188158198.");
        params.put("seller_nick","券生活8");
        params.put("mobile","13472581853");
        assertTrue(TaobaoCouponUtil.verifyParam(params));
    }


}
