package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountType;
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

    public Date createdAtBegin;
    public Date createdAtEnd;
    public String interval = "0";
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where e.order.userType = :userType");

            paramMap.put("userType", type);

        if (createdAtBegin != null) {
            condBuilder.append(" and e.order.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and e.order.createdAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
