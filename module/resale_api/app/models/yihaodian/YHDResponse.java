package models.yihaodian;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import play.libs.XPath;

import java.util.ArrayList;
import java.util.List;


/**
 * @author likang
 *         Date: 12-8-30
 */
public class YHDResponse {
    public int errorCount = 1;
    public int totalCount = 0;
    public List<Node> errors = new ArrayList<>(); // errorCode errorDes pkInfo

    public Node data;

    public boolean isOk() {
        return errorCount == 0;
    }

    public String selectTextTrim(String path) {
        return StringUtils.trimToNull(XPath.selectText(path, data));
    }

    public String firstErrorCode() {
        if (errors.size() == 0) {
            return null;
        }
        return StringUtils.trimToNull(XPath.selectText("./errorCode", errors.get(0)));
    }

    public String firstErrorDes() {
        if (errors.size() == 0) {
            return null;
        }
        return StringUtils.trimToNull(XPath.selectText("./errorDes", errors.get(0)));
    }

    public List<Node> selectNodes(String path) {
        return XPath.selectNodes(path, data);
    }
}
