package models.consumer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.uhuila.common.util.DateUtil;

import models.accounts.AccountType;
import models.order.Order;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

@Entity
@Table(name = "user_points")
public class UserPoint extends Model {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = true)
	public Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = true)
	public User user;

	@Column(name = "point_number")
	public String pointNumber;

	@Column(name = "deal_type")
	public String dealType;

	@Column(name = "deal_points")
	public int dealPoints;

	@Column(name = "current_points")
	public int currentPoints;

	@Column(name = "created_at")
	public Date createdAt;

	/**
	 * 取得积分信息
	 * @param user 用户信息
	 * @param condition 查询条件
	 * @param pageNumber 页数
	 * @param pageSize 每页记录数
	 * @return 积分信息
	 */
	public static JPAExtPaginator<UserPoint> findUserPoints(User user,
			UserCondition condition, int pageNumber, int pageSize) {
		JPAExtPaginator<UserPoint> orderPage = new JPAExtPaginator<>
		("UserPoint u", "u", UserPoint.class,condition.getCondition(user),
				condition.paramsMap)
				.orderBy("createdAt desc");
		orderPage.setPageNumber(pageNumber);
		orderPage.setPageSize(pageSize);

		return orderPage;

	}

	/**
	 * 取得积分类型
	 * @return 积分类型
	 */
	public String getPointTitle() {
		if (StringUtils.isEmpty(pointNumber)) {
			return "";
		}
		UserPointConfig userPointConfig = UserPointConfig.find("byPointNumber", pointNumber).first();
		return userPointConfig.pointTitle;
	}
}
