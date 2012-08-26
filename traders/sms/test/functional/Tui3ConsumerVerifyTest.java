package functional;

import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http.Response;

public class Tui3ConsumerVerifyTest extends EnsmsConsumerVerifyTest {

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
                 String message = "do=sms&mobile=" + mobile1 + "&content="
                         + msg +
                         "&ext="
                         + ecoupon.replyCode;
                 Logger.info("url=%s", message);
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
                        "&ext=1028");
            }
        };
    }
    
}
