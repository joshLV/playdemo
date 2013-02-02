package models.jingdong.groupbuy;

import org.w3c.dom.Node;
import play.libs.XPath;

import java.util.List;

/**
 * @author likang
 *         Date: 13-2-2
 */
public class JingdongMessage {
    public String version;
    public Long venderId;
    public Boolean zip;
    public Boolean encrypt;

    public String resultCode;
    public String resultMessage;

    public Node message;

    /**
     * 判断京东响应是否OK.
     */
    public boolean isOk() {
        if (resultCode != null){
            return resultCode.equals("200");
        }
        return message != null;
    }

    public Node selectNode(String path) {
        return XPath.selectNode(path, message);
    }

    public List<Node> selectNodes(String path) {
        return XPath.selectNodes(path, message);
    }

    public String selectText(String path) {
        return XPath.selectText(path, message);
    }
}
