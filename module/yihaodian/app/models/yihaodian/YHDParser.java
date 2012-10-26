package models.yihaodian;

import org.dom4j.Element;

import java.util.List;

/**
 * @author likang
 *         Date: 12-8-30
 */
public interface YHDParser<V> {
    public V parse(Element node);
}
