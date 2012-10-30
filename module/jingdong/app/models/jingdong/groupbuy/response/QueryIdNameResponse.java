package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class QueryIdNameResponse implements JDMessage, Serializable {
    private static final long serialVersionUID = 7063222052915330652L;
    public List<IdNameResponse> idNameList;
    public String elementName;
    public QueryIdNameResponse(String elementName){
        idNameList = new ArrayList<>();
        this.elementName = elementName;
    }
    @Override
    public boolean parse(Element root) {
        for(Element element : (List<Element>)root.element(elementName).elements()){
            IdNameResponse category = new IdNameResponse();
            category.id = Long.parseLong(element.elementTextTrim("Id"));
            category.name = element.elementTextTrim("Name");
            idNameList.add(category);
        }
        return true;
    }
}
