package models.dangdang;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.List;

/**
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:27 PM
 */
public class Response<V> implements Serializable {

    private String statusCode;
    private String errorCode;
    private String desc;
    private String spid;
    private String ver;
    private int errorCount;
    private List<V> vs;
    private List<ErrorInfo> errors;

    public Response() {

    }

    public Response(String responseBodyAsString) {

    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDesc() {
        return desc;
    }

    public String getSpid() {
        return spid;
    }

    public String getVer() {
        return ver;
    }

    public List<V> getVs() {
        return vs;
    }

    public void setVs(List<V> vs) {
        this.vs = vs;
    }

    /**
     * 解析当当传过来的参数XML格式
     *
     * @param xml
     */
    public void parseXml(String xml, String contentNodeName, boolean isList, Parser<V> parser) {
        Document document = null;

        try {
            document = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            errorCount = 1;
            ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.errorCode = ErrorCode.NO_DATA_NODE;
            errorInfo.errorDes = "parse xml error";
            errors.add(errorInfo);
            return;
        }

        Element root = document.getRootElement();
        //解析 errorCount
        errorCount = parseIntValue(root, "errorCount");
        //解析错误信息
        if (errorCount > 0) {
            parseErrors(root);
        }

        //解析内容
        parseContent(root, contentNodeName, isList, parser);
    }

    private void parseContent(Element root, String contentNodeName, boolean isList, Parser<V> parser) {
        Element node = root.element(contentNodeName);
        if (node == null) {
            return;
        }
        //解析对象
        if (isList) {
            for (Object o : node.elements()) {
                parseOneContent((Element) o, parser);
            }
        } else {
            parseOneContent(node, parser);
        }
    }

    private void parseOneContent(Element e, Parser<V> parser) {
        V v = parser.parse(e);
        if (v != null) {
            vs.add(v);
        }
    }

    private int parseIntValue(Element element, String nodeName) {
        String nodeValue = element.elementText(nodeName);
        if (nodeValue == null) {
            return 0;
        } else {
            return Integer.parseInt(nodeValue);
        }
    }

    public List<ErrorInfo> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }

    private void parseErrors(Element root) {
        Element errorInfoElement = root.element("errInfoList");
        for (Object o : errorInfoElement.elements()) {
            Element e = (Element) o;
            ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.errorCode = ErrorCode.getErrorCode(Integer.parseInt(e.elementText("errorCode")));
            errorInfo.errorDes = e.elementText("errorDes");
            errors.add(errorInfo);
        }
    }
}
