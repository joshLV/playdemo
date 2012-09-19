package models.dangdang;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Map;

/**
 * <p/>
 * User: sujie
 * Date: 9/17/12
 * Time: 12:48 PM
 */
public class Request<V> {
    /**
     * 当当请求我们接口传来的参数
     */
    public static Map<String, String> params;
    private String requestBodyAsString;

    /**
     * 解析data节点的数据
     *
     * @param requestBodyAsString
     * @throws DocumentException
     */
    public void parse(String requestBodyAsString) throws DocumentException {
        Document document = DocumentHelper.parseText(requestBodyAsString);
        Element root = document.getRootElement();
        params.put("orderId", root.elementText("order_id"));
        params.put("ddgid", root.elementText("ddgid"));
        params.put("spgid", root.elementText("spgid"));
        params.put("userCode", root.elementText("user_code"));
        params.put("receiveMobile", root.elementText("receiveMobile"));
        params.put("consumeId", root.elementText("consumeId"));
    }

    public Map<String, String> getParams() {
        return params;
    }

}
