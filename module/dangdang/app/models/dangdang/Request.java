package models.dangdang;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p/>
 * User: sujie
 * Date: 9/17/12
 * Time: 12:48 PM
 */
public class Request {
    /**
     * 当当请求我们接口传来的参数
     */
    public Map<String, String> params = new HashMap<>();


    /**
     * 解析data节点的数据
     *
     * @param requestBodyAsString
     * @throws org.dom4j.DocumentException
     */
    public void parse(String requestBodyAsString) throws DocumentException {

        Document document = DocumentHelper.parseText(requestBodyAsString);

        Element root = document.getRootElement();
        Iterator iter = root.elementIterator("order"); // 获取根节点下的子节点order
        // 遍历order节点
        while (iter.hasNext()) {
            Element recordEle = (Element) iter.next();
            params.put("order_id", recordEle.elementText("order_id"));
            params.put("ddgid", recordEle.elementText("ddgid"));
            params.put("spgid", recordEle.elementText("spgid"));
            params.put("user_code", recordEle.elementText("user_code"));
            params.put("receiver_mobile_tel", recordEle.elementText("receiver_mobile_tel"));
            params.put("consume_id", recordEle.elementText("consume_id"));
        }
    }

}
