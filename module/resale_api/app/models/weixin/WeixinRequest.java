package models.weixin;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import play.libs.XPath;

import java.util.Date;

/**
 * @author likang
 *         Date: 13-3-19
 */
public class WeixinRequest {
    public String toUserName;
    public String fromUserName;

    public Date createTime;

    public WeixinMessageType msgType;

    public Document message;


    public String selectTextTrim(String relativePath) {
        return StringUtils.trimToNull(XPath.selectText("/xml/" + relativePath, message));
    }
}
