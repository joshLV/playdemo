package models.order;

import models.consumer.Address;
import models.consumer.User;
import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Orders extends Model {
    @ManyToOne
    public User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<OrderItems> orderItems;

    @Column(name = "order_no")
    public String orderNumber;

    public String status;

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

    public String postcode;

    @Column(name = "createdAt")
    public Date createdAt;

    @Column(name = "updatedAt")
    public Date updatedAt;

    @Column(name = "lock_version")
    public int lockVersion;

    public int deleted;

    @Column(name = "delivery_no")
    public String deliveryNo;
    @Column(name = "delivery_type")
    public int deliveryType;
    /**
     * 成交开始时间
     */
    @Transient
    public String createdAtBegin;
    /**
     * 成交开始时间
     */
    @Transient
    public String createdAtEnd;
    /**
     * 退款开始时间
     */
    @Transient
    public String refundAtBegin;
    /**
     * 退款开始时间
     */
    @Transient
    public String refundAtEnd;

    public Orders() {
    }

    public Orders(User user, Address address) {
        this.user = user;
        this.status = OrderStatus.UNPAID.toString();
        this.deleted = 0;
        this.orderNumber = OrdersNumber.generateOrderNumber();
        this.orderItems = new ArrayList();

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

    public void checkInventory(Goods goods, long number) throws NotEnoughInventoryException {
        if (goods.baseSale < number) {
            throw new NotEnoughInventoryException();
        }
    }

    /**
     * 订单查询
     *
     * @param orders
     * @return
     */
    public static List query(Orders orders) {
        EntityManager entityManager = play.db.jpa.JPA.em();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.no,a.delivery_type,a.order_no,a.delivery_no,c.name,b.number,a.amount,a.created_at,a.pay_method,a.status,a.id  FROM orders a");
        sql.append(" LEFT JOIN order_items b ON a.id = b.order_id ");
        sql.append(" LEFT JOIN goods c ON b.goods_id=c.id");
        //指定某商户
        sql.append(" WHERE c.company_id=1");
        if (orders.createdAtBegin != null && !"".equals(orders.createdAtBegin)) {
            sql.append(" and a.created_at >='" + orders.createdAtBegin + " 00:00:00'");
        }
        if (orders.createdAtEnd != null && !"".equals(orders.createdAtEnd)) {
            sql.append(" and a.created_at <='" + orders.createdAtEnd + " 23:59:59'");
        }
        if (orders.status != null && !"".equals(orders.status)) {
            sql.append(" and a.status ='" + orders.status + "'");
        }
        if (orders.deliveryType != 0) {
            sql.append(" and a.delivery_type =" + orders.deliveryType);
        }
        if (orders.payMethod != null && !"".equals(orders.payMethod)) {
            sql.append(" and a.pay_method =" + orders.payMethod);
        }

        sql.append(" ORDER BY a.created_at DESC");
        List orderList = entityManager.createNativeQuery(sql.toString()).getResultList();
        return orderList;
    }

	/**
	 * 商家券号列表
	 *
	 * @return
	 */
	public static List queryQ() {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.order_no,c.no,c.name,d.discount_price,d.discount_sn,c.expired_bg_on,c.expired_ed_on,a.created_at,d.refund_price,d.refund_at,d.status FROM discount d");
		sql.append(" LEFT JOIN orders a ON a.id = d.order_id ");
		sql.append(" LEFT JOIN order_items b ON a.id = b.order_id ");
		sql.append(" LEFT JOIN goods c ON b.goods_id=c.id");
		sql.append(" WHERE c.company_id=1");
		sql.append(" ORDER BY a.created_at DESC");
		return entityManager.createNativeQuery(sql.toString()).getResultList();
	}

	/**
	 * 会员中心 券号列表
	 *
	 * @return
	 */
	public static List userTicketsQuery(Long id) {
		EntityManager entityManager = play.db.jpa.JPA.em();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.order_no,c.name,d.discount_sn,a.created_at,c.expired_bg_on,c.expired_ed_on,d.status FROM discount d");
		sql.append(" LEFT JOIN orders a ON a.id = d.order_id ");
		sql.append(" LEFT JOIN order_items b ON a.id = b.order_id ");
		sql.append(" LEFT JOIN goods c ON b.goods_id=c.id");
		sql.append(" WHERE a.user_id="+id);
		sql.append(" ORDER BY a.created_at DESC");
		return entityManager.createNativeQuery(sql.toString()).getResultList();
	}

    public void createAndUpdateInventory() {
        if (create()) {
            for (OrderItems orderItem : orderItems) {
                orderItem.goods.baseSale -= orderItem.number;
                orderItem.save();
            }
        }

    }
}
