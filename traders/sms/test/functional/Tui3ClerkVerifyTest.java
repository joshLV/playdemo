package functional;

import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;

public class Tui3ClerkVerifyTest extends ClerkSmsVerifyBaseTest {

    @Before
    public void setup() {
        setupTestData();
    }
    
    /**
     * 正常发送券对象的类
     */
    protected MessageSender getTheMessageSender() {
        return new MessageSender() {
             @Override 
             public Response doMessageSend(String mobile, ECoupon ecoupon) {
                 String message = "do=sms&mobile=" + mobile + "&content=#"
                         + ecoupon.eCouponSn
                         + "&ext="
                         + ecoupon.replyCode;
                 return GET("/tui3?"
                         + message);
             }
        };
    }

    /**
     * 发送无效消息格式的类.
     */
    protected InvalidMessageSender getInvalidMessageSender() {
        return new InvalidMessageSender() {
            @Override
            public Response doMessageSend(String msg) {
                return GET("/tui3?do=sms&mobile=15900002342&content=" + msg +
                        "&code=1028");
            }
        };
    }
    
    @Test
    public void 正常店员验证过程() {
        testNormalClerkCheck(getTheMessageSender());
    }
    
    /**
     * 消息应当以#开头
     */
    @Test
    public void 店员发送错误格式短信() {
        testInvalidFormatMessage(getInvalidMessageSender());
    }
    
    @Test
    public void 店员发送不存在的券号() {
        testEcouponNotExists(getInvalidMessageSender());
    }
    
    @Test
    public void 无效的商户代码() {
        testInvalidSupplier(getTheMessageSender());
    }

    @Test
    public void 商户被冻结() {
        testLockedSupplier(getTheMessageSender());
    }    
    
    @Test
    public void 不是当前商户所发行的券() {
        testTheGoodsFromOtherSupplier(getTheMessageSender());
    }
    
    @Test
    public void 券已经被消费() {
        testConsumeredECoupon(getTheMessageSender());
    }
    
    @Test
    public void 券已经过期() {
        testExpiredECoupon(getTheMessageSender());
    }
}
