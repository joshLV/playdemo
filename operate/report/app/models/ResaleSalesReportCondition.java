package models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import models.accounts.AccountType;
import com.uhuila.common.util.DateUtil;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:52
 */
public class ResaleSalesReportCondition {

    public Date createdAtBegin = DateUtil.getBeginOfDay();
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());    
    public String interval = "-1d";
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where e.order.userType = :userType and e.goods.isLottery=false");

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
