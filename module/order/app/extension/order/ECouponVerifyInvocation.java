package extension.order;

import util.extension.ExtensionInvocation;
import util.extension.annotation.ExtensionPoint;

/**
 * 券验证Invocation.
 * 用于扩展券验证时的行为。
 */
@ExtensionPoint("ECouponVerifyCheck")
public abstract class ECouponVerifyInvocation implements ExtensionInvocation<ECouponVerifyContext> {
}
