package models.resale;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.uhuila.common.constants.DeletedStatus;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.OrderStatus;
/**
 * 分销订单查询条件
 * 
 * @author yanjy
 *
 */
public class ResalerOrdersCondition {
	public Date createdAtBegin; 
	public Date createdAtEnd; 
	public OrderStatus status;
	public String goodsName;
	public Map<String, Object> paramsMap = new HashMap<>();

	/**
	 *
	 *
	 * @param resaler 用户信息
	 * @return sql 查询条件
	 */
	public String getResalerFilter(Resaler resaler) {
		StringBuilder sql = new StringBuilder();
		sql.append(" o.deleted = :deleted");
		paramsMap.put("deleted", DeletedStatus.UN_DELETED);
		if (resaler != null) {
			sql.append(" and o.userId = :resalerId and o.userType = :userType");
			paramsMap.put("resalerId", resaler.id);
			paramsMap.put("userType", AccountType.RESALER);
		}
		if (createdAtBegin != null) {
			sql.append(" and o.createdAt >= :createdAtBegin");
			paramsMap.put("createdAtBegin", createdAtBegin);
		}
		if (createdAtEnd != null) {
			sql.append(" and o.createdAt <= :createdAtEnd");
			paramsMap.put("createdAtEnd", createdAtEnd);
		}
		if (status != null) {
			sql.append(" and o.status = :status");
			paramsMap.put("status", status);
		}

		//按照商品名称检索
		if (StringUtils.isNotBlank(goodsName)) {
			sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.name like :goodsName)");
			paramsMap.put("goodsName", "%" + goodsName + "%");
		}

		return sql.toString();
	}

	public String getOrderByExpress() {
		String orderBySql = "o.createdAt desc";
		return orderBySql;
	}
}
