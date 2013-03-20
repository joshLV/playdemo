package models.weixin.response;

import models.weixin.WeixinMessageType;
import models.weixin.WeixinResponse;

/**
 * @author likang
 *         Date: 13-3-20
 */
public class WeixinTextResponse extends WeixinResponse {
    public String content;

    @Override
    public WeixinMessageType getMsgType() {
        return WeixinMessageType.TEXT;
    }
}
