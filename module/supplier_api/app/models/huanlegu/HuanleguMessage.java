package models.huanlegu;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import play.libs.XPath;

import java.util.List;

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
     * 判断欢乐谷响应是否OK.
     */
    public boolean isOk() {
        if (statusCode != null){
            return statusCode.equals("200");
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
