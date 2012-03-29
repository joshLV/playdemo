package models.resale;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ResalerCondition {
	public String loginName;
	public ResalerStatus status;

	private Map<String, Object> paramMap = new HashMap<>();

	/**
	 * @return condition 查询条件
	 */
	public String getFitter(){
		StringBuilder condition = new StringBuilder("1=1");
		if (StringUtils.isNotEmpty(loginName)) {
			condition.append(" and r.loginName like :loginName");
			paramMap.put("loginName","%"+ loginName+"%");
		}

		if (status != null) {
			condition.append(" and r.status = :status");
			paramMap.put("status",status);
		}
		return condition.toString();
	}

	public Map<String, Object> getParamMap() {
		return paramMap;
	}
}
