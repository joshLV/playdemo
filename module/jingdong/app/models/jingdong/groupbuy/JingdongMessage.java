package models.jingdong.groupbuy;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author likang
 *         Date: 13-1-31
 */
public class JingdongMessage {
    public String   version;
    public Long     venderId;
    public Boolean  zip;
    public Boolean  encrypt;
    public String   resultCode;
    public String   resultMessage;

    public Node message;
}
