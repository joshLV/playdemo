package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;

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

    public Date paidAtBegin = DateUtil.getBeginOfDay();
    public Date paidAtEnd = DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where e.order.userType = :userType and e.goods.isLottery=false");

        paramMap.put("userType", type);

        if (paidAtBegin != null) {
            condBuilder.append(" and e.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", paidAtBegin);
        }
        if (paidAtEnd != null) {
            condBuilder.append(" and e.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(paidAtEnd));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
