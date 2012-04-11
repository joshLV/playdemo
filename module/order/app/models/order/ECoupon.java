package models.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import models.accounts.AccountType;
import models.accounts.RefundBill;
import models.accounts.TradeBill;
import models.accounts.TradeStatus;
import models.accounts.util.AccountUtil;
import models.accounts.util.RefundUtil;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import models.sales.Goods;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

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
	private static String dateFormat = "yyyyMMddhhmmssSSS";
	private static java.text.DateFormat df = new java.text.SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = true)
	public Order order;

	@ManyToOne
	public Goods goods;

	@OneToOne
	@JoinColumn(name = "item_id")
	public OrderItems orderItems;

	@Required
	@Column(name = "e_coupon_sn")
	public String eCouponSn;

	// ====  价格列表  ====
	@Column(name = "face_value")
	public BigDecimal faceValue;        //商品面值、市场价

	@Column(name = "original_price")
	public BigDecimal originalPrice;    //供应商进货价

	@Column(name = "resaler_price")
	public BigDecimal resalerPrice;     //用户在哪个分销商平台购买的价格，用于计算分销平台的佣金

	@Column(name = "sale_price")
	public BigDecimal salePrice;        //最终成交价,对于普通分销商来说，此成交价与以上分销商价(resalerPrice)相同；
	// ====  价格列表  ====


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


	public ECoupon(Order order, Goods goods, OrderItems orderItems) {
		this.order = order;
		this.goods = goods;

		this.faceValue = orderItems.faceValue;
		this.originalPrice = orderItems.originalPrice;
		this.resalerPrice = orderItems.resalerPrice;
		this.salePrice = orderItems.salePrice;

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
		int random = new Random().nextInt() % 100;
		return DateFormatUtils.format(new Date(), dateFormat) + Math.abs(random);
	}

	/**
	 * 根据页面录入券号查询对应信息
	 *
	 * @param eCouponSn 券号
	 * @param supplierId 商户ID
	 * @return ECoupon 券信息
	 */
	public static ECoupon query(String eCouponSn, Long supplierId) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select distinct e from ECoupon e where 1=1 ");
		if (supplierId != null) {
			sql.append(" and e.goods.supplierId = :supplierId");
			params.put("supplierId", supplierId);
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
	 * @param supplierId 商户ID
	 * @return queryMap 查询信息
	 */
	public static Map<String, Object> queryInfo(String eCouponSn, Long supplierId) {
		ECoupon eCoupon = query(eCouponSn, supplierId);
		Map<String, Object> queryMap = new HashMap();
		if (eCoupon != null) {
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
	 * 优惠券被消费。
	 * 修改优惠券状态、发佣金、给商户打钱
	 * 
	 * @return
	 */
	public void consumed(){
		Account supplierAccount = AccountUtil.getAccount(orderItems.goods.supplierId, AccountType.BUSINESS);

		//给商户打钱
		TradeBill consumeTrade = TradeUtil.createConsumeTrade(eCouponSn, supplierAccount, originalPrice);
		TradeUtil.success(consumeTrade);
		//给优惠券平台佣金
		TradeBill platformCommissionTrade = TradeUtil.createCommissionTrade(
				AccountUtil.getPlatformCommissionAccount(), 
				resalerPrice.subtract(originalPrice),
				eCouponSn);

		TradeUtil.success(platformCommissionTrade);
		//如果是在优惠啦网站下的单，还要给优惠啦佣金
		if (order.userType == AccountType.CONSUMER){
			TradeBill uhuilaCommissionTrade = TradeUtil.createCommissionTrade(
					AccountUtil.getUhuilaAccount(),
					salePrice.subtract(resalerPrice),
					eCouponSn);

			TradeUtil.success(uhuilaCommissionTrade);
		}
		this.status = ECouponStatus.CONSUMED;
		this.consumedAt = new Date();
		this.save();
	}

	/**
	 * 商家券号列表
	 *
	 * @param supplierId  商户ID
	 * @param pageNumber 页数
	 * @param pageSize   记录数
	 * @return ordersPage 列表信息
	 */
	public static JPAExtPaginator<ECoupon> queryCoupons(Long supplierId, int pageNumber, int pageSize) {
		StringBuilder sql = new StringBuilder(" 1=1");
		Map<String, Object> paramsMap = new HashMap<>();
		if (supplierId != null) {
			sql.append(" and e.goods.supplierId = :supplierId)");
			paramsMap.put("supplierId", supplierId);
		}

		JPAExtPaginator<ECoupon> ordersPage = new JPAExtPaginator<>("ECoupon e", "e", ECoupon.class, sql.toString(),
				paramsMap).orderBy(" e.consumedAt desc");

		ordersPage.setPageNumber(pageNumber);
		ordersPage.setPageSize(pageSize);
		ordersPage.setBoundaryControlsEnabled(true);
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
	public static JPAExtPaginator<ECoupon> userCouponsQuery(Long userId, AccountType accountType, Date createdAtBegin, Date createdAtEnd,
			ECouponStatus status, String goodsName, int pageNumber, int pageSize) {
		CouponsCondition condition = new CouponsCondition();

		JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>
		("ECoupon e", "e", ECoupon.class, condition.getFilter(userId, accountType, createdAtBegin, createdAtEnd, status, goodsName),
				condition.couponsMap).orderBy(condition.getOrderByExpress());

		couponsPage.setPageNumber(pageNumber);
		couponsPage.setPageSize(pageSize);
		couponsPage.setBoundaryControlsEnabled(false);
		return couponsPage;
	}

	/**
	 * 退款
	 * 
	 * @param eCoupon 券信息
	 * @param userId 用户信息
	 * @param applyNote 退款原因
	 * @return
	 */
	public static String applyRefund(ECoupon eCoupon,Long userId,String applyNote) {
		String returnFlg ="{\"error\":\"ok\"}";

		if(eCoupon == null || eCoupon.order.userId != userId || eCoupon.order.userType != AccountType.CONSUMER){
			returnFlg="{\"error\":\"no such eCoupon\"}";
			return returnFlg;
		}
		if(eCoupon.status == ECouponStatus.UNCONSUMED ){
			returnFlg = "{\"error\":\"can not apply refund with this goods\"}";
			return returnFlg;
		}
		//查找原订单信息
		Order order = eCoupon.order;
		TradeBill tradeBill = null;
		OrderItems orderItem = null;

		if(order != null){
			tradeBill = TradeBill.find("byOrderIdAndTradeStatus", order.getId(), TradeStatus.SUCCESS).first();
			orderItem = OrderItems.find("byOrderAndGoods",order, eCoupon.goods).first();
		}
		if(order == null || tradeBill == null || orderItem == null){
			returnFlg = "{\"error\":\"can not get the trade bill\"}";
			return returnFlg;
		}

		//创建退款流程
		RefundBill refundBill = RefundUtil.create(tradeBill, order.getId(), orderItem.getId(),
				orderItem.salePrice, applyNote);
		RefundUtil.success(refundBill);

		//更改库存
		eCoupon.goods.baseSale += 1;
		eCoupon.goods.saleCount -= 1;
		eCoupon.goods.save();

		//更改订单状态
		eCoupon.status = ECouponStatus.REFUND;
		eCoupon.refundAt = new Date();
		eCoupon.save();

		return returnFlg;
	}

	public String getEcouponSn(){
		String sn = eCouponSn.substring(0, 4)+ "**********"+eCouponSn.substring(14);
		return sn;
	}

}
