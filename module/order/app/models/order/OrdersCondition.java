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
     * @param orders 订单信息
     * @param companyId 商户ID
     * @return sql 查询条件
     */
	public String getFilter(Orders orders,Long companyId) {
		StringBuilder sql = new StringBuilder();
		sql.append(" o.deleted = :deleted");
		paramsMap.put("deleted", DeletedStatus.UN_DELETED);
		sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.companyId = :companyId)");
		paramsMap.put("companyId", companyId);

		if (orders.createdAtBegin != null) {
			sql.append(" and o.createdAt >= :createdAtBegin");
			paramsMap.put("createdAtBegin", orders.createdAtBegin);
		}
		if (orders.createdAtEnd != null) {
			sql.append(" and o.createdAt <= :createdAtEnd");
			paramsMap.put("createdAtEnd", orders.createdAtEnd);
		}
		if (orders.refundAtBegin != null) {
			sql.append(" and o.refundAt >= :refundAtBegin");
			paramsMap.put("refundAtBegin", orders.refundAtBegin);
		}
		if (orders.refundAtEnd != null) {
			sql.append(" and o.refundAt <= :refundAtEnd");
			paramsMap.put("refundAtEnd", orders.refundAtEnd);
		}
		if (orders.status != null) {
			sql.append(" and o.status = :status");
			paramsMap.put("status", orders.status);
		}
		if (orders.deliveryType != 0) {
			sql.append(" and o.deliveryType = :deliveryType");
			paramsMap.put("deliveryType", orders.deliveryType);
		}
		if (StringUtils.isNotBlank(orders.payMethod)) {
			sql.append(" and o.payMethod = :payMethod");
			paramsMap.put("payMethod", orders.payMethod);
		}

		if (StringUtils.isNotBlank(orders.searchKey)) {
			//按照商品名称检索
			if ("1".equals(orders.searchKey)) {
				sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.name like :name)");
				paramsMap.put("name", "%" + orders.searchItems + "%");
			}
			//按照商品订单检索
			if ("2".equals(orders.searchKey)) {
				sql.append(" and o.orderNumber like :orderNumber");
				paramsMap.put("orderNumber", "%" + orders.searchItems + "%");
			}

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
