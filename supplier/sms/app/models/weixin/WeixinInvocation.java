package models.weixin;

import util.extension.ExtensionInvocation;
import util.extension.annotation.ExtensionPoint;

/**
 * 定义微信扩展点.
 * User: tanglq
 * Date: 13-3-22
 * Time: 上午12:01
 */
@ExtensionPoint("Weixin")
public abstract class WeixinInvocation implements ExtensionInvocation<WeixinContext> {
}
