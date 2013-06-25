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
    public String errorMsg;
    public String sequenceId;
    public String sign;

    public Node message;

    //response 特有的head
    public String statusCode;

    //request 特有的head
    public String distributorId;
    public String clientId;

    /**
     * 判断欢乐谷信息是否OK.
     */
    public boolean isResponseOk() {
        return "200".equals(statusCode) && message != null;
    }

    public boolean isRequestOk() {
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
