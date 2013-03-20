package models.weixin;

import java.util.Date;

/**
 * @author likang
 *         Date: 13-3-20
 */
public abstract class WeixinResponse {
    public String toUserName;
    public String fromUserName;

    public int createTime;

    public int funcFlag;

    public WeixinResponse(){
        createTime = (int)(new Date().getTime()/1000);
        funcFlag = 0;
        fromUserName = WeixinUtil.WEIXIN_USERNAME;
    }


    public abstract WeixinMessageType getMsgType();
}
