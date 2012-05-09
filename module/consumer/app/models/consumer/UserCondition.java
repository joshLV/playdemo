package models.consumer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.uhuila.common.util.DateUtil;

public class UserCondition {
	public Date createdAtBegin;
	public Date createdAtEnd;

	public String pointNumber;
	public Map<String, Object> paramsMap = new HashMap<>();

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
			paramsMap.put("createdAtEnd",DateUtil.getEndOfDay(createdAtEnd));
		}

		if (StringUtils.isNotBlank(pointNumber)) {
			sql.append(" and u.pointNumber = :pointNumber");
			paramsMap.put("pointNumber", pointNumber);
		}


		return sql.toString();
	};
}
