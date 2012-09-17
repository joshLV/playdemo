package models.dangdang;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;

/**
 * <p/>
 * User: sujie
 * Date: 9/17/12
 * Time: 12:48 PM
 */
public class Request<V> {

    private List<V> nodeList; //root节点下的所有node
    public int errorCode;
    public String desc;


    /**
     * 解析当当传过来的参数XML格式
     *
     * @param xml
     */
    public void parseXml(String xml, String contentNodeName, Parser<V> parser) throws DocumentException {
        Document document = DocumentHelper.parseText(xml);

        Element root = document.getRootElement();

        //解析内容
        Element node = root.element(contentNodeName);
        if (node == null) {
            return;
        }
        //解析对象
        parseOneContent(node, parser);
    }

    private void parseOneContent(Element e, Parser<V> parser) {
        V v = parser.parse(e);
        if (v != null) {
            nodeList.add(v);
        }
    }

    public List<V> getNodeList() {
        return nodeList;
    }
}
