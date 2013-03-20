package models.weixin.response;

import models.weixin.WeixinMessageType;
import models.weixin.WeixinResponse;

/**
 * @author likang
 *         Date: 13-3-20
 */
public class WeixinMusicResponse extends WeixinResponse {
    public String title;
    public String description;
    public String musicUrl;
    public String hqMusicUrl;

    @Override
    public WeixinMessageType getMsgType() {
        return WeixinMessageType.MUSIC;
    }
}
