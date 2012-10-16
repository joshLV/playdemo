package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class QueryCategoryResponse implements JDMessage{
    public List<CategoryResponse> categories;
    public QueryCategoryResponse(){
        categories = new ArrayList<>();
    }
    @Override
    public boolean parse(Element root) {
        for(Element element : (List<Element>)root.element("Categories").elements()){
            CategoryResponse category = new CategoryResponse();
            category.id = Long.parseLong(element.elementTextTrim("Id"));
            category.name = element.elementTextTrim("Name");
            categories.add(category);
        }
        return true;
    }
}
