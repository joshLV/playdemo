package models.yihaodian.response;

import models.yihaodian.YHDParser;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-29
 */
public class YHDAttributeInfo implements Serializable {
    private static final long serialVersionUID = 7008202063310030652L;

    public Long id;
    public List<YHDIdName> attributeItemList;
    public YHDAttributeInfo(){
        attributeItemList = new ArrayList<>();
    }

    public static YHDParser<YHDAttributeInfo> attributeCategoryParser = new YHDParser<YHDAttributeInfo>() {
        @Override
        public YHDAttributeInfo parse(Element node) {
            YHDAttributeInfo attributeInfo = new YHDAttributeInfo();
            attributeInfo.id = Long.parseLong(node.elementTextTrim("attributeId"));
            for(Element element : (List<Element>)node.element("attributeItemInfoList").elements()){
                YHDIdName attributeItem = new YHDIdName();
                attributeItem.id = Long.parseLong(element.elementTextTrim("itemId"));
                attributeItem.name = element.elementTextTrim("itemLabel");
                attributeInfo.attributeItemList.add(attributeItem);
            }

            return attributeInfo;
        }
    };

    public static YHDParser<YHDAttributeInfo> attributeSerialParser = attributeCategoryParser;

}
