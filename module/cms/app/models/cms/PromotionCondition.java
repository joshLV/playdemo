package models.cms;


import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 */
public class PromotionCondition implements Serializable {
    
    private static final long serialVersionUID = 83409113062L;
    
    public String name;
    public Date effectAt;       //起始时间
    public Date expiredAt;      //截止日期

    private Map<String, Object> params = new HashMap<>();
    public String getFilter(){
        StringBuilder filter = new StringBuilder("1=1");
        if(name != null){
            filter.append(" and name like :name");
            params.put("name", "%" + name + "%");
        }
        if(effectAt != null){
            filter.append(" and effectAt > effectAt");
            params.put("effectAt", effectAt);
        }
        if(expiredAt != null){
            filter.append(" and expiredAt < expiredAt");
            params.put("expiredAt", expiredAt);
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
