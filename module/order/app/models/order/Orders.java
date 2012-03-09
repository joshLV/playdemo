package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.consumer.Address;
import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;

import java.math.BigDecimal;
import java.util.*;


@Entity
@Table(name = "orders")
public class Orders extends Model {
	@ManyToOne
	public User user;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
	public List<OrderItems> orderItems;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
	public List<ECoupon> eCoupons;

	@Column(name = "order_no")
	public String orderNumber;

	@Enumerated(EnumType.STRING)
	public OrderStatus status;

	public BigDecimal amount;

	@Column(name = "account_pay")
	public BigDecimal accountPay;

	@Column(name = "discount_pay")
	public BigDecimal discountPay;

	@Column(name = "need_pay")
	public BigDecimal needPay;

	@Column(name = "buyer_phone")
	public String buyerPhone;

	@Column(name = "buyer_mobile")
	public String buyerMobile;

	public String remark;

	@Column(name = "pay_method")
	public String payMethod;

	@Column(name = "pay_request_id")
	public Long payRequestId;

	@Column(name = "receiver_phone")
	public String receiverPhone;

	@Column(name = "receiver_mobile")
	public String receiverMobile;

	@Column(name = "receiver_address")
	public String receiverAddress;

	@Column(name = "receiver_name")
	public String receiverName;

	@Column(name = "paid_at")
	public Date paidAt;

	@Column(name = "refund_at")
	public Date refundAt;

	public String postcode;

	@Column(name = "created_at")
	public Date createdAt;

	@Column(name = "updated_at")
	public Date updatedAt;

	@Column(name = "lock_version")
	public int lockVersion;

	public String paymentSourceCode;

	/**
	 * 逻辑删除,0:未删除，1:已删除
	 */
	@Enumerated(EnumType.ORDINAL)
	public DeletedStatus deleted;

	@Column(name = "delivery_no")
	public String deliveryNo;

	@Column(name = "delivery_type")
	public int deliveryType;
	/**
	 * 成交开始时间
	 */
	@Transient
	public Date createdAtBegin;
	/**
	 * 成交开始时间
	 */
	@Transient
	public Date createdAtEnd;
	/**
	 * 退款开始时间
	 */
	@Transient
	@Temporal(TemporalType.DATE)
	public Date refundAtBegin;
	/**
	 * 退款开始时间
	 */
	@Transient
	@Temporal(TemporalType.DATE)
	public Date refundAtEnd;

	@Transient
	public String searchKey;

	@Transient
	public String searchItems;

	public Orders() {
	}

	public Orders(User user, Address address) {
		this.user = user;
		this.status = OrderStatus.UNPAID;
		this.deleted = DeletedStatus.UN_DELETED;
		this.orderNumber = OrdersNumber.generateOrderNumber();
		this.orderItems = new ArrayList();
		this.paidAt = null;
		this.amount = new BigDecimal(0);
		this.accountPay = new BigDecimal(0);
		this.needPay = new BigDecimal(0);
		this.discountPay = new BigDecimal(0);

		this.lockVersion = 0;

		this.createdAt = new Date();
		this.updatedAt = new Date();
		if (address != null) {
			this.receiverAddress = address.getFullAddress();
			this.receiverMobile = address.mobile;
			this.receiverName = address.name;
			this.receiverPhone = address.getPhone();
			this.postcode = address.postcode;
		}
	}

	public Orders(User user, long goodsId, long number, Address address, String mobile) throws NotEnoughInventoryException {
		this(user, address);

		Goods goods = Goods.findById(goodsId);
		checkInventory(goods, number);
		if (goods.salePrice.compareTo(new BigDecimal(0)) > 0) {
			this.amount = goods.salePrice.multiply(new BigDecimal(number));
			if (goods.materialType == MaterialType.REAL) {
				this.amount = this.amount.add(new BigDecimal(5));
			}
			//todo 目前没考虑支付优惠
			this.needPay = amount;
		}
		this.receiverMobile = mobile;

		OrderItems orderItems = new OrderItems(this, goods, number);
		this.orderItems.add(orderItems);
	}

	public Orders(User user, List<Cart> cartList, Address address) throws NotEnoughInventoryException {
		this(user, address);

		this.amount = Cart.amount(cartList);
		for (Cart cart : cartList) {
			if (cart.goods.materialType == MaterialType.REAL) {
				this.amount = this.amount.add(new BigDecimal(5));
				break;
			}
		}
		this.needPay = amount;

		for (Cart cart : cartList) {
			if (cart.number <= 0) {
				continue;
			}
			checkInventory(cart.goods, cart.number);
			OrderItems orderItems = new OrderItems(this, cart.goods, cart.number);
			this.orderItems.add(orderItems);
		}
	}

	public static long itemsNumber(Orders orders) {
		long itemsNumber = 0L;
		if (orders == null) {
			return itemsNumber;
		}
		Object result = OrderItems.em().createNativeQuery(
				"select sum(buy_number) from order_items where order_id ="
						+ orders.getId()).getSingleResult();
		if (result != null) {
			itemsNumber = ((java.math.BigDecimal) result).longValue();
		}
		return itemsNumber;
	}

	/**
	 * 生成订单编号.
	 *
	 * @return 订单编号
	 */
	public static String generateOrderNumber() {
		int random = new Random().nextInt() % 10000;
		return DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS") + random;
	}

	public void checkInventory(Goods goods, long number) throws NotEnoughInventoryException {
		if (goods.baseSale < number) {
			throw new NotEnoughInventoryException();
		}
	}

	/**
	 * 订单查询
	 *
	 * @param orders    订单信息
	 * @param companyId 商户ID
	 * @param pageNumber 第几页
	 * @param pageSize 每页记录
	 * @return
	 */
	public static JPAExtPaginator<Orders> query(Orders orders, Long companyId,int pageNumber, int pageSize) {
		OrdersCondition condition= new OrdersCondition();
		JPAExtPaginator<Orders> ordersPage = new JPAExtPaginator<>
		("Orders o", "o", Orders.class, condition.getFilter(orders,companyId),
				condition.paramsMap)
				.orderBy(condition.getOrderByExpress());
		ordersPage.setPageNumber(pageNumber);
		ordersPage.setPageSize(pageSize);
		ordersPage.setBoundaryControlsEnabled(false);
		return ordersPage;
	}

	/**
	 * 商家券号列表
	 *
	 * @return
	 */
	public static JPAExtPaginator<ECoupon> queryCoupons( Long companyId,int pageNumber, int pageSize) {
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
	 * @param user
	 * @param createdAtBegin
	 * @param createdAtEnd
	 * @param status
	 * @param goodsName
	 * @return
	 */
	public static List userCuponsQuery(User user, Date createdAtBegin, Date createdAtEnd, ECouponStatus status, String goodsName) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select distinct e from ECoupon e where 1=1 ");
		if (user != null) {
			sql.append(" and e.order.user = :user");
			params.put("user", user);
		}

		if (createdAtBegin != null) {
			sql.append(" and e.createdAt >= :createdAtBegin");
			params.put("createdAtBegin", createdAtBegin);
		}

		if (createdAtEnd != null) {
			sql.append(" and e.createdAt <= :createdAtEnd");
			params.put("createdAtEnd", createdAtEnd);
		}

		if (StringUtils.isNotBlank(goodsName)) {
			sql.append(" and e.goods.name like :name");
			params.put("name", "%" + goodsName + "%");
		}

		if (status != null) {
			sql.append(" and e.status = :status");
			params.put("status", status);
		}
		Query couponQery = entityManager.createQuery(sql.toString());
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			couponQery.setParameter(entry.getKey(), entry.getValue());
		}
		sql.append(" order by e.createdAt desc");
		return couponQery.getResultList();
	}

	public void createAndUpdateInventory(User user, String cookieIdentity) {
		save();
		for (OrderItems orderItem : orderItems) {
			orderItem.goods.baseSale -= orderItem.buyNumber;
			orderItem.save();
		}
		Cart.clear(user, cookieIdentity);

	}

	/**
	 * 我的订单查询
	 *
	 * @param user
	 * @param createdAtBegin
	 * @param createdAtEnd
	 * @param status
	 * @param goodsName
	 * @return
	 */
	public static List<Orders> findMyOrders(User user, Date createdAtBegin, Date createdAtEnd, OrderStatus status, String goodsName) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct o from Orders o, OrderItems oi  WHERE oi member of o.orderItems");
		Map<String, Object> params = new HashMap<String, Object>();
		if (user != null) {
			sql.append(" and o.user = :user");
			params.put("user", user);
		}
		if (createdAtBegin != null) {
			sql.append(" and o.createdAt >= :createdAtBegin");
			params.put("createdAtBegin", createdAtBegin);
		}
		if (createdAtEnd != null) {
			sql.append(" and o.createdAt <= :createdAtEnd");
			params.put("createdAtEnd", createdAtEnd);
		}
		if (status != null) {
			sql.append(" and o.status = :status");
			params.put("status", status);
		}

		//按照商品名称检索
		if (StringUtils.isNotBlank(goodsName)) {
			sql.append(" and oi.goods.name like :goodsName");
			params.put("goodsName", "%" + goodsName + "%");
		}

		sql.append(" order by o.createdAt desc");
		Query orderQery = entityManager.createQuery(sql.toString());
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			orderQery.setParameter(entry.getKey(), entry.getValue());
		}

		return orderQery.getResultList();
	}

}
