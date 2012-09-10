package functional;

import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;

/**
 * 北京奥恩软件-店员验证测试.
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
public class EnsmsClerkVerifyTest extends ClerkSmsVerifyBaseTest {

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
                 String message = "mobiles=" + mobile + "&msg=#"
                         + ecoupon.eCouponSn
                         +
                         "&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code="
                         + ecoupon.replyCode;
                 return GET("/getsms?"
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
                return GET("/getsms?mobiles=15900002342&msg=" + msg +
                        "&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                        "=1319873904&code=1028");
            }
        };
    }
    
    @Test
    public void 正常店员验证过程() {
        testNormalClerkCheck(getTheMessageSender());
    }

    @Test
    public void 店员验证券不在适用范围内() {
        testNotInVerifyTime(getTheMessageSender());
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
    public void 券已经被冻结() {
        testFreezedECoupon(getTheMessageSender());
    }
    @Test
    public void 券已经过期() {
        testExpiredECoupon(getTheMessageSender());
    }
}
