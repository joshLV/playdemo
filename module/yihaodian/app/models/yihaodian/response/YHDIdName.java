package models.yihaodian.response;

import models.yihaodian.YHDParser;
import org.dom4j.Element;

import java.io.Serializable;

/**
 * @author likang
 *         Date: 12-10-26
 */
public class YHDIdName implements Serializable {
    private static final long serialVersionUID = 7068222063310330652L;

    public Long id;
    public String name;

    public static YHDParser<YHDIdName> brandParser = new YHDParser<YHDIdName>() {
        @Override
        public YHDIdName parse(Element node) {
            YHDIdName brand = new YHDIdName();
            brand.id = Long.parseLong(node.elementTextTrim("brandId"));
            brand.name = node.elementTextTrim("brandName");
            return brand;
        }
    };
}
