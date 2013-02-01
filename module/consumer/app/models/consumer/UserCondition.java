package models.consumer;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserCondition {
    public Date createdAtBegin;
    public Date createdAtEnd;

    public String mobile;
    public String loginName;
    public UserStatus status;
    public String interval = "-1d";

    public String pointNumber;
    public int tradeType;
    public Map<String, Object> paramsMap = new HashMap<>();

    public String getCoinsCondition(User user) {

        StringBuilder sql = new StringBuilder("1=1");
        if (user != null) {
            sql.append(" and u.user = :user");
            paramsMap.put("user", user);
        }
        System.out.println(createdAtBegin + ">>>>createdAtBegin"+createdAtEnd);
        if (createdAtBegin != null) {
            sql.append(" and u.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and u.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (tradeType != 0) {
            if (tradeType > 0) {
                sql.append(" and number>0");
            } else {
                sql.append(" and number<0");
            }
        }

        return sql.toString();
    }

    public String getCondition(User user) {

        StringBuilder sql = new StringBuilder("1=1");
        if (user != null) {
            sql.append(" and u.user = :user");
            paramsMap.put("user", user);
        }

        if (createdAtBegin != null) {
            sql.append(" and u.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and u.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (StringUtils.isNotBlank(pointNumber)) {
            sql.append(" and u.pointNumber = :pointNumber");
            paramsMap.put("pointNumber", pointNumber);
        }

        return sql.toString();
    }

    public String getFilter() {
        StringBuilder sql = new StringBuilder("1=1");
        if (createdAtBegin != null) {
            sql.append(" and u.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and u.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (StringUtils.isNotBlank(loginName)) {
            String name = loginName.trim();
            if (User.isOpenIdExpress(name)) {
                OpenIdSource source = User.getOpenSourceFromName(name);
                name = User.getOpenIdFromName(name);
                sql.append(" and (u.loginName like :loginName or (u.openIdSource =:openIdSource and u.openId like :loginName))");
                paramsMap.put("openIdSource", source);
                paramsMap.put("name", name);
            } else {
                sql.append(" and (u.loginName like :loginName )");
                paramsMap.put("loginName", "%" + name + "%");
            }
        }
        if (StringUtils.isNotBlank(mobile)) {
            sql.append(" and u.mobile = :mobile");
            paramsMap.put("mobile", mobile);
        }
        if (status != null) {
            sql.append(" and u.status = :status");
            paramsMap.put("status", status);
        }
        return sql.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramsMap;
    }
}
