package models.order;

import com.uhuila.common.util.DateUtil;
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
    public RebateStatus status;
    public Date createdAtBegin;
    public Date createdAtEnd;
    public Map<String, Object> params = new HashMap<>();

    public String getFilter(User user) {
        StringBuilder filter = new StringBuilder("1=1 and rebateAmount > 0");

        if (user != null) {
            filter.append(" and promoteUser=:promoteUser");
            params.put("promoteUser", user);
        }
        if (createdAtBegin != null) {
            filter.append(" and createdAt >= :createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            filter.append(" and createdAt <= :createdAtEnd");
            params.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (status != null) {
            filter.append(" and status = :status");
            params.put("status", status);
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
