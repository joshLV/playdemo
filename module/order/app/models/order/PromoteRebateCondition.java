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
    public String interval = "-1d";
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

    public String getFilter() {
        StringBuilder filter = new StringBuilder("where p.order=e.order and e.status=:e_status and (p.status=:status or p.status=:status1) ");
        params.put("e_status", ECouponStatus.CONSUMED);
        params.put("status", RebateStatus.ALREADY_REBATE);
        params.put("status1", RebateStatus.PART_REBATE);
        if (createdAtBegin != null) {
            filter.append(" and e.consumedAt >= :createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            filter.append(" and e.consumedAt <= :createdAtEnd");
            params.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
