package models.order;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import models.accounts.Account;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import models.sales.Goods;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

/**
 * User: pwg
 * Date: 12-3-5
 * Time: 下午5:16
 */
@Entity
@Table(name = "e_coupon")
public class ECoupon extends Model {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	private static DecimalFormat decimalFormat = new DecimalFormat("00000");

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = true)
	public Orders order;

	@ManyToOne
	public Goods goods;

	@OneToOne
	@JoinColumn(name = "item_id")
	public OrderItems orderItems;

	@Required
	@Column(name = "e_coupon_sn")
	public String eCouponSn;

	@Column(name = "e_coupon_price")
	public BigDecimal eCouponPrice;

	@Column(name = "income_price")
	public BigDecimal incomePrice;

	@Column(name = "refund_price")
	public BigDecimal refundPrice;

	@Column(name = "created_at")
	@Temporal(TemporalType.DATE)
	public Date createdAt;

	@Column(name = "consumed_at")
	public Date consumedAt;

	@Column(name = "refund_at")
	public Date refundAt;

	@Column(name = "buy_number")
	public int buyNumber;

	@Enumerated(EnumType.STRING)
	public ECouponStatus status;


	public ECoupon(Orders order, Goods goods,OrderItems orderItems) {
		this.order = order;
		this.goods = goods;
		this.eCouponPrice = orderItems.originalPrice;
		this.refundPrice = eCouponPrice;
		this.createdAt = new Date();

		this.consumedAt = null;
		this.refundAt = null;
		this.status = ECouponStatus.UNCONSUMED;
		this.eCouponSn = generateSerialNumber();
		this.orderItems = orderItems;
	}

	public ECoupon() {
	}

	private String generateSerialNumber() {
		int random = new Random().nextInt() % 10000;
		return dateFormat.format(new Date()) + decimalFormat.format(random);

	}

	/**
	 * 根据页面录入券号查询对应信息
	 *
	 * @param eCouponSn 券号
	 * @param companyId 商户ID
	 * @return ECoupon 券信息
	 */
	public static ECoupon query(String eCouponSn, Long companyId) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select distinct e from ECoupon e where 1=1 ");
		if (companyId != null) {
			sql.append(" and e.goods.companyId = :companyId");
			params.put("companyId", companyId);
		}
		if (StringUtils.isNotBlank(eCouponSn)) {
			sql.append(" and e.eCouponSn = :eCouponSn");
			params.put("eCouponSn", eCouponSn);
		}
		Query couponQery = entityManager.createQuery(sql.toString());
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			couponQery.setParameter(entry.getKey(), entry.getValue());
		}
		sql.append(" order by e.createdAt desc");

		List<ECoupon> queryList = couponQery.getResultList();
		if (queryList.size() == 0) {
			return null;
		}

		return queryList.get(0);
	}

	/**
	 * 根据页面录入券号查询对应信息
	 *
	 * @param eCouponSn 券号
	 * @param companyId 商户ID
	 * @return queryMap 查询信息
	 */
	public static Map<String, Object> queryInfo(String eCouponSn, Long companyId) {
		ECoupon eCoupon = query(eCouponSn, companyId);
		Map<String, Object> queryMap = new HashMap();
		if (eCoupon != null) {
			java.text.DateFormat df = new java.text.SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			queryMap.put("name", eCoupon.goods.name);
			queryMap.put("expireAt", eCoupon.goods.expireAt != null ? df.format(eCoupon.goods.expireAt) : null);
			queryMap.put("consumedAt", eCoupon.consumedAt != null ? df.format(eCoupon.consumedAt) : null);
			queryMap.put("eCouponSn", eCoupon.eCouponSn);
			queryMap.put("refundAt", eCoupon.refundAt != null ? df.format(eCoupon.refundAt) : null);
			queryMap.put("status", eCoupon.status);
			queryMap.put("error", 0);
		}

		return queryMap;
	}


	/**
	 * 修改券状态,并产生消费交易记录
	 *
	 * @param eCouponSn 券号
	 * @param companyId 商户ID
	 */
	public static boolean update(String eCouponSn, Long companyId) {
		ECoupon eCoupon = query(eCouponSn, companyId);
		//产生消费记录
		List<Account> account = Account.find("uid = ?",companyId).fetch();
		if(account.size()>0) {
			TradeBill tradeBill = TradeUtil.createConsumeTrade(eCouponSn, account.get(0), eCoupon.incomePrice, eCoupon.order.id);
			if (tradeBill != null) {
				TradeUtil.success(tradeBill);
				eCoupon.status = ECouponStatus.CONSUMED;
				eCoupon.save();

				return true;
			}
		}
		return false;
	}

	/**
	 * 商家券号列表
	 *
	 * @param companyId  商户ID
	 * @param pageNumber 页数
	 * @param pageSize   记录数
	 * @return ordersPage 列表信息
	 */
	public static JPAExtPaginator<ECoupon> queryCoupons(Long companyId, int pageNumber, int pageSize) {
		StringBuilder sql = new StringBuilder();
		sql.append(" e.goods.companyId = :companyId)");
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("companyId", companyId);
		JPAExtPaginator<ECoupon> ordersPage = new JPAExtPaginator<>
		("ECoupon e", "e", ECoupon.class, sql.toString(),
				paramsMap)
				.orderBy(" e.consumedAt desc");

		ordersPage.setPageNumber(pageNumber);
		ordersPage.setPageSize(pageSize);
		ordersPage.setBoundaryControlsEnabled(false);
		return ordersPage;
	}

	/**
	 * 会员中心 券号列表
	 *
	 * @param user           用户信息
	 * @param createdAtBegin 开始日
	 * @param createdAtEnd   结束日
	 * @param status         状态
	 * @param goodsName      商品名称
	 * @param pageNumber     页数
	 * @param pageSize       记录数
	 * @return couponsPage 券记录
	 */
	public static JPAExtPaginator<ECoupon> userCouponsQuery(User user, Date createdAtBegin, Date createdAtEnd,
			ECouponStatus status, String goodsName, int pageNumber, int pageSize) {
		CouponsCondition condition = new CouponsCondition();

		JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>
		("ECoupon e", "e", ECoupon.class, condition.getFilter(user, createdAtBegin, createdAtEnd, status, goodsName),
				condition.couponsMap)
				.orderBy(condition.getOrderByExpress());

		couponsPage.setPageNumber(pageNumber);
		couponsPage.setPageSize(pageSize);
		couponsPage.setBoundaryControlsEnabled(false);
		return couponsPage;
	}
}
