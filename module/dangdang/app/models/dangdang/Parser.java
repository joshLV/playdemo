package models.dangdang;

import org.dom4j.Element;
/**
 * 解析器.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:49 PM
 */
public interface Parser<V> {
    public V parse(Element node);

}
