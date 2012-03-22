package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.consumer.User;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrdersCondition {
	public Map<String, Object> paramsMap = new HashMap<>();

	/**
	 *
	 * @param order 订单信息
	 * @param supplierId 商户ID
	 * @return sql 查询条件
	 */
	public String getFilter(Order order,Long supplierId) {
		StringBuilder sql = new StringBuilder();
		sql.append(" o.deleted = :deleted");
		paramsMap.put("deleted", DeletedStatus.UN_DELETED);
		sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.supplierId = :supplierId)");
		paramsMap.put("supplierId", supplierId);

		if (order.createdAtBegin != null) {
			sql.append(" and o.createdAt >= :createdAtBegin");
			paramsMap.put("createdAtBegin", order.createdAtBegin);
		}
		if (order.createdAtEnd != null) {
			sql.append(" and o.createdAt <= :createdAtEnd");
			paramsMap.put("createdAtEnd", order.createdAtEnd);
		}
		if (order.refundAtBegin != null) {
			sql.append(" and o.refundAt >= :refundAtBegin");
			paramsMap.put("refundAtBegin", order.refundAtBegin);
		}
		if (order.refundAtEnd != null) {
			sql.append(" and o.refundAt <= :refundAtEnd");
			paramsMap.put("refundAtEnd", order.refundAtEnd);
		}
		if (order.status != null) {
			sql.append(" and o.status = :status");
			paramsMap.put("status", order.status);
		}
		if (order.deliveryType != 0) {
			sql.append(" and o.deliveryType = :deliveryType");
			paramsMap.put("deliveryType", order.deliveryType);
		}
		if (StringUtils.isNotBlank(order.payMethod)) {
			sql.append(" and o.payMethod = :payMethod");
			paramsMap.put("payMethod", order.payMethod);
		}

		//按照商品名称检索
		if ("1".equals(order.searchKey)) {
			sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.name like :name)");
			paramsMap.put("name", "%" + order.searchItems + "%");
		}
		//按照商品订单检索
		if ("2".equals(order.searchKey)) {
			sql.append(" and o.orderNumber like :orderNumber");
			paramsMap.put("orderNumber", "%" + order.searchItems + "%");
		}

		return sql.toString();
	}

	public String getOrderByExpress() {
		String orderBySql = "o.createdAt desc";
		return orderBySql;
	}

	/**
	 *
	 *
	 * @param user 用户信息
	 * @param createdAtBegin 下单开始时间
	 * @param createdAtEnd 下单结束时间
	 * @param status 状态
	 * @param goodsName 商品名
	 * @return sql 查询条件
	 */
	public String getFilter(User user, Date createdAtBegin, Date createdAtEnd,
			OrderStatus status, String goodsName) {
		StringBuilder sql = new StringBuilder();
		sql.append(" o.deleted = :deleted");
		paramsMap.put("deleted", DeletedStatus.UN_DELETED);
		if (user != null) {
			sql.append(" and o.user = :user");
			paramsMap.put("user", user);
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
}
