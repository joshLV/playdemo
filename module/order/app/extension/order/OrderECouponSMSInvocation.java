package extension.order;

import util.extension.ExtensionInvocation;
import util.extension.annotation.ExtensionPoint;

/**
 * 订单券短信内容扩展接口，用于重定义短信内容。
 */
@ExtensionPoint("OrderECouponSMS")
public abstract class OrderECouponSMSInvocation implements ExtensionInvocation<OrderECouponSMSContext> {
}
