package models.jingdong.groupbuy.response;

import models.jingdong.groupbuy.JDMessage;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class QueryCityResponse implements JDMessage{
    public List<CityResponse> cities;

    public QueryCityResponse(){
        cities = new ArrayList<>();
    }

    @Override
    public boolean parse(Element root) {
        for(Element element : (List<Element>)root.element("Cities").elements()){
            CityResponse city = new CityResponse();
            city.id = Long.parseLong(element.elementTextTrim("Id"));
            city.name = element.elementTextTrim("Name");
            cities.add(city);
        }
        return true;
    }
}
