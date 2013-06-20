package models.jingdong.groupbuy;

import org.apache.commons.lang.StringUtils;
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

    /**
     * 选择节点.
     *
     * @param path 类似 ./a/b
     * @return 节点
     */
    public Node selectNode(String path) {
        return XPath.selectNode(path, message);
    }

    /**
     * 选择节点列表.
     *
     * @param path 类似 ./a/b
     * @return 节点列表
     */
    public List<Node> selectNodes(String path) {
        return XPath.selectNodes(path, message);
    }

    /**
     * 选择节点内容.
     *
     * @param path 类似 ./a/b
     * @return 节点内容
     */
    public String selectTextTrim(String path) {
        return StringUtils.trimToNull(XPath.selectText(path, message));
    }
}
