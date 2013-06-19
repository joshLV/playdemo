package models.huanlegu;

import org.w3c.dom.Node;

/**
 * @author likang
 *         Date: 13-6-19
 */
public class HuanleguMessage {
    public String version;
    public String timeStamp;
    public String statusCode;
    public String errorMsg;
    public String sequenceId;
    public String sign;

    public Node message;

    /**
     * 判断京东响应是否OK.
     */
    public boolean isOk() {
        if (statusCode != null){
            return statusCode.equals("200");
        }
        return message != null;
    }
}
