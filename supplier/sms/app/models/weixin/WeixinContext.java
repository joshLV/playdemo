package models.weixin;

import util.extension.InvocationContext;

/**
 * User: tanglq
 * Date: 13-3-22
 * Time: 上午12:00
 */
public class WeixinContext implements InvocationContext {
    public WeixinRequest weixinRequest;

    public String resultText;

    private WeixinContext() {}

    public static WeixinContext build(WeixinRequest request) {
        WeixinContext weixinContext = new WeixinContext();
        weixinContext.weixinRequest = request;
        return weixinContext;
    }
}
