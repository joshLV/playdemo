package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceFlag;
import models.accounts.AccountSequenceType;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.consumer.Address;
import models.consumer.User;
import models.mail.CouponMessage;
import models.mail.MailUtil;
import models.resale.Resaler;
import models.resale.util.ResaleUtil;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sms.SMSUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Entity
@Table(name = "orders")
public class Order extends Model {
	@Column(name = "user_id")
	public long userId;                     //下单用户ID，可能是优惠啦用户，也可能是分销商

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type")
	public AccountType userType;            //用户类型，个人/分销商

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
	@OrderBy("id")
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

	@Version
	@Column(name = "lock_version")
	public int lockVersion;

	/**
	 * 逻辑删除,0:未删除，1:已删除
	 */
	@Enumerated(EnumType.ORDINAL)
	public DeletedStatus deleted;

	@Column(name = "delivery_no")
	public String deliveryNo;

	@Column(name = "delivery_type")
	public int deliveryType;

	@Transient
	public String searchKey;

	@Transient
	public String searchItems;

	@Column(name = "delivery_company")
	public String deliveryCompany;

	public Order() {
	}

	public Order(long userId, AccountType userType) {
		this.userId = userId;
		this.userType = userType;

		this.status = OrderStatus.UNPAID;
		this.deleted = DeletedStatus.UN_DELETED;
		this.orderNumber = generateOrderNumber();
		this.orderItems = new ArrayList<>();
		this.paidAt = null;
		this.amount = BigDecimal.ZERO;
		this.accountPay = BigDecimal.ZERO;
		this.needPay = BigDecimal.ZERO;
		this.discountPay = BigDecimal.ZERO;

		this.lockVersion = 0;

		this.createdAt = new Date();
		this.updatedAt = new Date();
	}

	/**
	 * 设置订单地址
	 *
	 * @param address 地址
	 */
	public void setAddress(Address address) {
		if (address != null) {
			this.receiverAddress = address.getFullAddress();
			this.receiverMobile = address.mobile;
			this.receiverName = address.name;
			this.receiverPhone = address.getPhone();
			this.postcode = address.postcode;
		}

	}

	/**
	 * 添加订单条目.
	 *
	 * @param goods        商品
	 * @param number       数量
	 * @param mobile       手机
	 * @param salePrice    成交价
	 * @param resalerPrice 作为分销商的成本价
	 * @return 添加的订单条目
	 * @throws NotEnoughInventoryException
	 */
	public OrderItems addOrderItem(Goods goods, long number, String mobile, BigDecimal salePrice, BigDecimal resalerPrice)
			throws NotEnoughInventoryException {
		OrderItems orderItems = null;
		if (number > 0 && goods != null) {
			checkInventory(goods, number);
			orderItems = new OrderItems(this, goods, number, mobile, salePrice, resalerPrice);
			this.orderItems.add(orderItems);
			this.amount = this.amount.add(salePrice.multiply(new BigDecimal(String.valueOf(number))));
			this.needPay = this.amount;
		}
		return orderItems;
	}

	public void addFreight() {
		this.amount = this.amount.add(new BigDecimal("6"));
		this.needPay = this.amount;
	}


	public void setUser(long userId, AccountType accountType) {
		this.userId = userId;
		this.userType = accountType;
		this.save();
	}

	/**
	 * 计算订单中有多少个商品
	 *
	 * @param order
	 * @return
	 */
	public static long itemsNumber(Order order) {
		long itemsNumber = 0L;
		if (order == null) {
			return itemsNumber;
		}
		EntityManager entityManager = JPA.em();
		Object result = entityManager.createQuery("SELECT sum( buyNumber ) FROM Order o,o.orderItems WHERE Order.id ="
				+ order.getId()).getSingleResult();
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
		int random = new Random().nextInt() % 100;
		return DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS") + Math.abs(random);
	}

	public void checkInventory(Goods goods, long number) throws NotEnoughInventoryException {
		if (goods.baseSale < number) {
			throw new NotEnoughInventoryException();
		}
	}

	/**
	 * 订单查询
	 *
	 * @param condition  订单查询条件
	 * @param supplierId 商户ID
	 * @param pageNumber 第几页
	 * @param pageSize   每页记录
	 * @return ordersPage 订单信息
	 */
	public static JPAExtPaginator<Order> query(OrdersCondition condition, Long supplierId, int pageNumber, int pageSize) {
		JPAExtPaginator<Order> orderPage = new JPAExtPaginator<>
		("Order o", "o", Order.class, condition.getFilter(supplierId),
				condition.paramsMap)
				.orderBy(condition.getOrderByExpress());
		orderPage.setPageNumber(pageNumber);
		orderPage.setPageSize(pageSize);
		orderPage.setBoundaryControlsEnabled(true);
		return orderPage;
	}

	public void createAndUpdateInventory() {
		save();
		boolean haveFreight = false;
		for (OrderItems orderItem : orderItems) {
			orderItem.goods.baseSale -= orderItem.buyNumber;
			orderItem.goods.saleCount += orderItem.buyNumber;
			orderItem.save();
			if (orderItem.goods.materialType == MaterialType.REAL) {
				haveFreight = true;
			}
		}

		if (haveFreight) {
			addFreight();
			save();
		}
	}

	/**
	 * 订单已支付，修改支付状态、时间，更改库存，发送电子券密码
	 */
	public void paid() {
		this.status = OrderStatus.PAID;
		this.paidAt = new Date();
		this.save();
		Account account = AccountUtil.getAccount(this.userId, this.userType);
		//补加两个账户交易记录
        AccountUtil.addBalance(account,this.accountPay.add(this.discountPay),
                BigDecimal.ZERO, this.payRequestId,AccountSequenceType.CHARGE,"账户充值");
        AccountUtil.addBalance(account,this.accountPay.add(this.discountPay).negate(),
                BigDecimal.ZERO, this.payRequestId, AccountSequenceType.PAY,"支付");

		//如果是电子券
		if (this.orderItems != null) {
			for (OrderItems orderItem : this.orderItems) {
				models.sales.Goods goods = orderItem.goods;
				if (goods == null) {
					continue;
				}
				// 更新订单明细的状态
				orderItem.status = OrderStatus.PAID;
				orderItem.save();

				if (MaterialType.ELECTRONIC.equals(goods.materialType)) {
					List<String> couponCodes = new ArrayList<>();
					for (int i = 0; i < orderItem.buyNumber; i++) {
						ECoupon eCoupon = new ECoupon(this, goods, orderItem).save();

						if (!Play.mode.isDev()) {
							SMSUtil.send(goods.name + "券号:" + eCoupon.eCouponSn, orderItem.phone);
						}
						couponCodes.add(eCoupon.getMaskedEcouponSn());
					}

					CouponMessage mail = new CouponMessage();
					//分销商
					if (AccountType.RESALER.equals(orderItem.order.userType)) {
						String userName = orderItem.order.getResaler().userName;
						mail.setEmail(orderItem.order.getResaler().email);
						mail.setFullName(userName);
					} else {
						//消费者
						mail.setEmail(orderItem.order.getUser().loginName);
						if (orderItem.order.getUser().userInfo == null) {
							mail.setFullName(orderItem.order.getUser().loginName);
						} else {
							mail.setFullName(orderItem.order.getUser().userInfo.fullName);
						}
					}
					mail.setCoupons(couponCodes);
					MailUtil.send(mail);
				}
				goods.save();
			}
		}
	}


	private User user;
	private Resaler resaler;

	public User getUser() {
		if (user == null) {
			user = User.findById(userId);
		}
		return user;
	}

	public Resaler getResaler() {
		if (resaler == null) {
			resaler = Resaler.findById(userId);
		}
		return resaler;
	}

	/**
	 * 会员中心订单查询
	 *
	 * @param user       用户信息
	 * @param condition  查询条件
	 * @param pageNumber 第几页
	 * @param pageSize   每页记录
	 * @return ordersPage 订单信息
	 */
	public static JPAExtPaginator<Order> findUserOrders(User user, OrdersCondition condition,
			int pageNumber, int pageSize) {
		JPAExtPaginator<Order> orderPage = new JPAExtPaginator<>
		("Order o", "o", Order.class, condition.getFilter(user),
				condition.paramsMap)
				.orderBy(condition.getOrderByExpress());
		orderPage.setPageNumber(pageNumber);
		orderPage.setPageSize(pageSize);
		orderPage.setBoundaryControlsEnabled(false);
		return orderPage;
	}

	/**
	 * 分销商订单查询
	 *
	 * @param condition  查询条件
	 * @param pageNumber 第几页
	 * @param pageSize   每页记录
	 * @return ordersPage 订单信息
	 */
	public static JPAExtPaginator<Order> findResalerOrders(OrdersCondition condition,
			Resaler resaler, int pageNumber, int pageSize) {
		JPAExtPaginator<Order> orderPage = new JPAExtPaginator<>
		("Order o", "o", Order.class, condition.getResalerFilter(resaler),
				condition.paramsMap)
				.orderBy(condition.getOrderByExpress());
		orderPage.setPageNumber(pageNumber);
		orderPage.setPageSize(pageSize);
		orderPage.setBoundaryControlsEnabled(false);
		return orderPage;
	}

	@Transient
	static Map totalMap = new HashMap();

	public static Map getTotalMap() {
		return totalMap;
	}

	/**
	 * 本月订单总数（成功的和进行中的）
	 *
	 * @param resaler
	 * @param status
	 * @return
	 */
	public static void getThisMonthTotal(Resaler resaler) {

		EntityManager entityManager = JPA.em();
		Map thisMonthMap = ResaleUtil.findThisMonth();

		//本月成功订单笔数
		String condition = getCondition(resaler, thisMonthMap, OrderStatus.PAID);
		Query query = entityManager.createQuery("SELECT o FROM Order o" + condition);
		List<Order> orderlist = query.getResultList();
		totalMap.put("thisPaidTotal", orderlist.size());
		BigDecimal amount = new BigDecimal(0);
		for (Order order : orderlist) {
			amount = amount.add(order.amount);
		}
		//本月已支付订单金额
		totalMap.put("thisMonthPaidAmount", amount);

		//本月未支付订单笔数
		condition = getCondition(resaler, thisMonthMap, OrderStatus.UNPAID);
		query = entityManager.createQuery("SELECT o FROM Order o" + condition);
		orderlist = query.getResultList();
		totalMap.put("thisUnPaidTotal", orderlist.size());
		//本月未支付订单金额
		amount = new BigDecimal(0);
		for (Order order : orderlist) {
			amount = amount.add(order.amount);
		}
		totalMap.put("thisMonthUnPaidAmount", amount);

		//上月成功订单笔数
		Map lastMonthMap = ResaleUtil.findLastMonth();
		condition = getCondition(resaler, lastMonthMap, OrderStatus.PAID);

		query = entityManager.createQuery("SELECT o FROM Order o" + condition);
		orderlist = query.getResultList();
		totalMap.put("lastPaidTotal", orderlist.size());
		//上月已支付订单金额
		amount = new BigDecimal(0);
		for (Order order : orderlist) {
			amount = amount.add(order.amount);
		}
		totalMap.put("lastMonthPaidAmount", amount);

		//上月未支付订单笔数
		condition = getCondition(resaler, lastMonthMap, OrderStatus.UNPAID);
		query = entityManager.createQuery("SELECT o FROM Order o" + condition);
		orderlist = query.getResultList();
		totalMap.put("lastUnPaidTotal", orderlist.size());
		//上月未支付订单金额
		amount = new BigDecimal(0);
		for (Order order : orderlist) {
			amount = amount.add(order.amount);
		}
		totalMap.put("lastMonthUnPaidAmount", amount);

		//本月总订单笔数
		condition = getCondition(resaler, thisMonthMap, null);
		query = entityManager.createQuery("SELECT o FROM Order o" + condition);
		orderlist = query.getResultList();
		totalMap.put("thisMonthTotal", orderlist.size());
		//本月总订单金额
		amount = new BigDecimal(0);
		for (Order order : orderlist) {
			amount = amount.add(order.amount);
		}
		totalMap.put("thisMonthTotalAmount", amount);

		//上月总订单笔数
		condition = getCondition(resaler, lastMonthMap, null);
		query = entityManager.createQuery("SELECT o FROM Order o" + condition);
		orderlist = query.getResultList();
		totalMap.put("lastMonthTotal", orderlist.size());
		//上月总订单金额
		amount = new BigDecimal(0);
		for (Order order : orderlist) {
			amount = amount.add(order.amount);
		}
		totalMap.put("lastMonthTotalAmount", amount);
	}


	@Transient
	public OrderStatus getRealGoodsStatus() {
		return getStatus(MaterialType.REAL);
	}

	@Transient
	public OrderStatus getElectronicGoodsStatus() {
		return getStatus(MaterialType.ELECTRONIC);
	}

	@Transient
	private List<Goods> realGoods = new ArrayList<>();
	@Transient
	private List<Goods> electronicGoods = new ArrayList<>();

	@Transient
	public List<Goods> getRealGoods() {
		if (realGoods.size() > 0) {
			return realGoods;
		}
		for (OrderItems orderItem : orderItems) {
			if (MaterialType.REAL.equals(orderItem.goods.materialType)) {
				realGoods.add(orderItem.goods);
			}
		}
		return realGoods;
	}

	@Transient
	public List<Goods> getElectronicGoods() {
		if (electronicGoods.size() > 0) {
			return electronicGoods;
		}
		for (OrderItems orderItem : orderItems) {
			if (MaterialType.ELECTRONIC.equals(orderItem.goods.materialType)) {
				electronicGoods.add(orderItem.goods);
			}
		}
		return electronicGoods;
	}

	private OrderStatus getStatus(MaterialType type) {
		OrderStatus status = null;
		for (OrderItems orderItem : orderItems) {
			if (type.equals(orderItem.goods.materialType)) {
				if (status == null) {
					status = orderItem.status;
				} else if (orderItem.status.compareTo(status) < 0) {
					status = orderItem.status;
				}
			}
		}

		return status == null ? this.status : status;
	}

	/**
	 * 查询条件
	 *
	 * @param monthMap 当月和上月
	 * @param status   状态
	 * @return
	 */
	private static String getCondition(Resaler resaler, Map monthMap, OrderStatus status) {
		StringBuilder buider = new StringBuilder();
		buider.append(" where userType ='" + AccountType.RESALER + "'");
		buider.append(" and userId =" + resaler.id);
		if (monthMap.get("fromDay") != null) {
			buider.append(" and createdAt >='" + monthMap.get("fromDay") + "'");
		}
		if (monthMap.get("toDay") != null) {
			buider.append(" and createdAt <='" + monthMap.get("toDay") + "'");
		}
		if (status != null) {
			buider.append(" and status = '" + status + "'");
		}
		return buider.toString();
	}

	public void sendRealGoods() {
		Order order = Order.findById(id);
		order.deliveryCompany = this.deliveryCompany;
		order.deliveryNo = this.deliveryNo;
		for (OrderItems orderItem : order.orderItems) {
			if (MaterialType.REAL.equals(orderItem.goods.materialType)) {
				orderItem.status = OrderStatus.SENT;
				orderItem.save();
			}
		}

		order.save();
	}

	public static Order findOneByUser(Long id, Long userId, AccountType accountType) {
		return Order.find("byIdAndUserIdAndUserType", id, userId, accountType).first();
	}
}
