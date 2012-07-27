package functional;

import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http.Response;

public class EnsmsConsumerVerifyTest extends ConsumerSmsVerifyBaseTest {

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
             public Response doMessageSend(ECoupon ecoupon, String msg, String mobile) {
                 // 尝试使用券所带的手机
                 String mobile1 = ecoupon.orderItems.phone;
                 if (mobile != null) {
                     mobile1 = mobile;
                 }
                 String message = "mobiles=" + mobile1 + "&msg="
                         + msg +
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
    public void 正常消费者验证过程() {
        testNormalConsumerCheck(getTheMessageSender());
    }
    
    /**
     * 消息应当是数字开头
     */
    public void 店员发送错误格式短信() {
        testInvalidFormatMessage(getInvalidMessageSender());
    }
    
    public void 店员发送不存在的券号() {
        testEcouponNotExists(getInvalidMessageSender());
    }
    
    public void 无效的商户代码() {
        testInvalidSupplier(getTheMessageSender());
    }


    public void 商户被冻结() {
        testLockedSupplier(getTheMessageSender());
    }    
    

    public void 不是当前商户所发行的券() {
        testTheGoodsFromOtherSupplier(getTheMessageSender());
    }
    

    public void 券已经被消费() {
        testConsumeredECoupon(getTheMessageSender());
    }
    

    public void 券已经过期() {
        testExpiredECoupon(getTheMessageSender());
    }
}
