package models.dangdang.groupbuy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.libs.XPath;

/**
 * @author likang
 *         Date: 13-1-22
 */
public class DDResponse {
    public String ver;
    public String spid;
    public String errorCode;
    public String desc;
    public Node data;

    public static DDResponse parseResponse(Document document) {
        DDResponse response = new DDResponse();
        response.ver = XPath.selectText("//ver", document);
        response.spid = XPath.selectText("//spid", document);
        response.errorCode = XPath.selectText("//error_code", document);
        response.desc = XPath.selectText("//desc", document);
        response.data = XPath.selectNode("//data", document);
        return response;
    }

    public boolean isSuccess() {
        return errorCode != null && "0".equals(errorCode);
    }
}
