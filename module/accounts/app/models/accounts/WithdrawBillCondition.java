package models.accounts;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-5-9
 */
public class WithdrawBillCondition implements Serializable {
    public Account account;
    public AccountType accountType;
    public WithdrawBillStatus status;

    public Date appliedAtBegin;
    public Date appliedAtEnd;
    public Date processedAtBegin;
    public Date processedAtEnd;

    public String searchUser;


    public String interval;

    private Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder("1=1");
        if (status != null) {
            filter.append(" and status = :status");
            params.put("status", status);
        }
        if (account != null) {
            filter.append(" and account = :account");
            params.put("account", account);
        }
        if (accountType != null) {
            filter.append(" and account.accountType = :accountType");
            params.put("accountType", accountType);
        }
        if (appliedAtBegin != null) {
            filter.append(" and appliedAt >= :appliedAtBegin");
            params.put("appliedAtBegin", appliedAtBegin);
        }
        if (appliedAtEnd != null) {
            filter.append(" and appliedAt <= :appliedAtEnd");
            params.put("appliedAtEnd", DateUtil.getEndOfDay(appliedAtEnd));
        }
        if (processedAtBegin != null) {
            filter.append(" and processedAt >= :processedAtBegin");
            params.put("processedAtBegin", processedAtBegin);
        }
        if (processedAtEnd != null) {
            filter.append(" and processedAt <= :processedAtEnd");
            params.put("processedAtEnd", DateUtil.getEndOfDay(processedAtEnd));
        }


        if (StringUtils.isNotBlank(searchUser)) {
            filter.append(" and applier in (select loginName from User u where u.mobile=:searchUser) ");
            params.put("searchUser", searchUser);
        }


        if (StringUtils.isNotBlank(searchUser)) {
            filter.append(" or applier in (select loginName from User u where u.loginName=:searchUser)");
            params.put("searchUser", searchUser);
        }

        if (StringUtils.isNotBlank(searchUser)) {
            filter.append(" or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.receiverMobile=:searchUser)");
            params.put("searchUser", searchUser);
        }


        if (StringUtils.isNotBlank(searchUser)) {
            filter.append(" or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.buyerMobile=:searchUser)");
            params.put("searchUser", searchUser);
        }

        if (StringUtils.isNotBlank(searchUser)) {
            filter.append("or applier in (select u.loginName from Order o, User u where o.userId=u.id and o.id in (select oi.order.id from o.orderItems oi where oi.phone =:searchUser)))");
            params.put("searchUser", searchUser);
        }




        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
