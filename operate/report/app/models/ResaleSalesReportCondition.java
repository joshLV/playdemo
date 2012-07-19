package models;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:52
 */
public class ResaleSalesReportCondition {

    public Date createdAtBegin = DateUtil.getYesterday();
    public Date createdAtEnd = DateUtil.getEndOfDay(DateUtil.getYesterday());
    public String orderBy = "r.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("o.userType = ? and o.id in (select o.id from o.orderItems oi ) " +
                "and o.id in (select o.id from o.eCoupons oe ) and e.orderItems=oi");
        if (createdAtBegin != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "r.createdAt DESC" : orderBy + " " + orderType;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
