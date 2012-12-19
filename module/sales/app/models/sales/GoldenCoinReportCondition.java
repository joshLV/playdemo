package models.sales;

import com.uhuila.common.util.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-18
 * Time: 上午11:16
 */
public class GoldenCoinReportCondition {
    public Date createdAtBegin = DateUtil.getBeginOfDay();
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where u.isPresent = false and u.number>0");

        if (createdAtBegin != null) {
            condBuilder.append(" and u.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and u.createdAt  <= :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
