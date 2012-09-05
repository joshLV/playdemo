package models.order;

import models.consumer.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-5
 * Time: 下午3:12
 */
public class PromoteRebateCondition {
    public Date createdAtBegin;
    public Date createdAtEnd;
    public Map<String, Object> params = new HashMap<>();

    public String getFilter(User user) {
        StringBuilder filter = new StringBuilder("1=1");

        if (user != null) {
            filter.append(" and promoteUser=:promoteUser");
            params.put("promoteUser", user);
        }

        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
