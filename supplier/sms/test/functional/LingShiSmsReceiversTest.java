package functional;

import models.order.ECoupon;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;

/**
 * User: tanglq
 * Date: 13-1-5
 * Time: 上午10:41
 */
public class LingShiSmsReceiversTest extends ConsumerSmsVerifyBaseTest {

    @Before
    public void setup() {
        setupTestData();
    }

    /**
     * 正常发送券对象的类
     *
     */
    protected MessageSender getTheMessageSender() {
        return new MessageSender() {
            @Override
            public Http.Response doMessageSend(ECoupon ecoupon, String msg, String mobile) {
                // 尝试使用券所带的手机
                String mobile1 = ecoupon.orderItems.phone;
                if (mobile != null) {
                    mobile1 = mobile;
                }
                String message = "SrcMobile=" + mobile1 + "&Content="
                        + msg +
                        "&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&AppendID="
                        + ecoupon.replyCode.substring(0, 2);
                Logger.info("url=%s", message);
                return GET("/lingshi?"
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
            public Http.Response doMessageSend(String msg) {
                return GET("/lingshi?SrcMobile=15900002342&Content=" + msg +
                        "&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt" +
                        "=1319873904&AppendID=1028");
            }
        };
    }

    @Test
    public void 正常消费者验证过程() {
        testNormalConsumerCheck(getTheMessageSender());
    }

    @Test
    public void 消费者验证券不在适用范围内() {
        testNotInVerifyTime(getTheMessageSender());
    }

    /**
     * 消息应当是数字开头
     */
    @Test
    public void 消费者发送错误格式短信() {
        testInvalidFormatMessage(getTheMessageSender());
    }

    @Test
    public void 发送不存在的店员工号() {
        testNotExistsJobNumber(getTheMessageSender());
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
