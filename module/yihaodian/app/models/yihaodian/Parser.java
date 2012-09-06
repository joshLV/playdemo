package models.yihaodian;

import org.dom4j.Element;

import java.util.List;

/**
 * @author likang
 *         Date: 12-8-30
 */
public interface Parser<V> {
    public V parse(Element node);
}
