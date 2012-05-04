package models.accounts;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.time.DateUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 账户资金明细查询条件.
 * todo 目前先实现只按创建时间、资金流向、支付类型来查询
 * <p/>
 * User: sujie
 * Date: 3/7/12
 * Time: 7:10 PM
 */
public class AccountSequenceCondition implements Serializable {
    public Account account;
    public AccountSequenceFlag sequenceFlag;    //账务变动方向：来账，往账
    public AccountSequenceType sequenceType;    //资金变动类型
    public Date createdAtBegin;
    public Date createdAtEnd;
    public String interval;

    //用于保存界面的查询条件
    public String accountName;

    private Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder();
        boolean hasCond = false;
        if (account != null && account.accountType != null && account.id == null) {
            appendAndIfNeeded(filter, hasCond);
            filter.append("account.accountType =:accountType ");
            params.put("accountType", account.accountType);
            hasCond = true;
        } else if (account != null && account.id != null) {
            appendAndIfNeeded(filter, hasCond);
            filter.append("account =:account");
            params.put("account", account);
            hasCond = true;
        }
        if (createdAtBegin == null && createdAtEnd == null) {
            Date now = new Date();
            createdAtBegin = DateUtils.addMonths(now, -3);
        }
        if (createdAtBegin != null) {
            appendAndIfNeeded(filter, hasCond);
            filter.append("createdAt >=:createdAtBegin");
            params.put("createdAtBegin", createdAtBegin);
            hasCond = true;
        }
        if (createdAtEnd != null) {
            appendAndIfNeeded(filter, hasCond);
            filter.append("createdAt <=:createdAtEnd");
            params.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
            hasCond = true;
        }
        if (sequenceFlag != null) {
            appendAndIfNeeded(filter, hasCond);
            filter.append("sequenceFlag=:sequenceFlag");
            params.put("sequenceFlag", sequenceFlag);
            hasCond = true;
        }
        if (sequenceType != null) {
            appendAndIfNeeded(filter, hasCond);
            filter.append("sequenceType=:sequenceType");
            params.put("sequenceType", sequenceType);
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

    private void appendAndIfNeeded(StringBuilder filter, boolean hasCond) {
        if (hasCond) {
            filter.append(" and ");
        }
    }
}
