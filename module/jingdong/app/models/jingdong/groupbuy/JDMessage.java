package models.jingdong.groupbuy;

import org.dom4j.Element;

/**
 * @author likang
 *         Date: 12-9-28
 */
public interface JDMessage {
    public boolean parse(Element root);
}
