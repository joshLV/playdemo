package models.weixin;

import java.io.Serializable;
import java.util.Date;

/**
 * @author likang
 *         Date: 13-3-20
 */
public abstract class WeixinResponse implements Serializable {

    public String toUserName;
    public String fromUserName;

    public long createTime;

    public int funcFlag;

    public WeixinResponse(){
        createTime = (new Date().getTime())/1000l;
        funcFlag = 0;
        fromUserName = WeixinUtil.WEIXIN_USERNAME;
    }


    public abstract WeixinMessageType getMsgType();
}
