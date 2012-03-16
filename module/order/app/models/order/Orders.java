package models.order;

import com.uhuila.common.constants.DeletedStatus;
import models.consumer.Address;
import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sms.SMSUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


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
        this.orderNumber = generateOrderNumber();
        this.orderItems = new ArrayList<>();
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

    public Orders(User user, long goodsId, long number, Address address, String mobile)
            throws NotEnoughInventoryException {
        this(user, address);

        models.sales.Goods goods = Goods.findById(goodsId);
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

    public Orders(User user, List<Cart> cartList, Address address, String mobile) throws NotEnoughInventoryException {
        this(user, address);

        this.amount = Cart.amount(cartList);
        for (Cart cart : cartList) {
            if (cart.goods.materialType == MaterialType.REAL) {
                this.amount = this.amount.add(new BigDecimal(5));
                break;
            }
        }
        this.needPay = amount;
        this.receiverMobile = mobile;

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
     * @param orders     订单信息
     * @param companyId  商户ID
     * @param pageNumber 第几页
     * @param pageSize   每页记录
     * @return ordersPage 订单信息
     */
    public static JPAExtPaginator<Orders> query(Orders orders, Long companyId, int pageNumber, int pageSize) {
        OrdersCondition condition = new OrdersCondition();
        JPAExtPaginator<Orders> ordersPage = new JPAExtPaginator<>
                ("Orders o", "o", Orders.class, condition.getFilter(orders, companyId),
                        condition.paramsMap)
                .orderBy(condition.getOrderByExpress());
        ordersPage.setPageNumber(pageNumber);
        ordersPage.setPageSize(pageSize);
        ordersPage.setBoundaryControlsEnabled(false);
        return ordersPage;
    }

    public void createAndUpdateInventory(User user, String cookieIdentity) {
        save();
        for (OrderItems orderItem : orderItems) {
            orderItem.goods.baseSale -= orderItem.buyNumber;
            orderItem.save();
        }
        Cart.clear(user, cookieIdentity);

    }

    public void paid() {
        this.status = OrderStatus.PAID;
        this.paidAt = new Date();
        this.save();

        //如果是电子券
        if (this.orderItems != null) {
            for (OrderItems orderItem : this.orderItems) {
                models.sales.Goods goods = orderItem.goods;
                if (goods == null) {
                    continue;
                }
                goods.baseSale -= 1;
                goods.saleCount += 1;
                if (goods.materialType == MaterialType.ELECTRONIC) {
                    ECoupon eCoupon = new ECoupon(this, goods, orderItem).save();
                    SMSUtil.send(goods.name + "券号:" + eCoupon.eCouponSn, this.receiverMobile);
                }
                goods.save();
            }
        }
    }


    /**
     * 会员中心订单查询
     *
     * @param user           用户信息
     * @param createdAtBegin 下单开始时间
     * @param createdAtEnd   下单结束时间
     * @param status         状态
     * @param goodsName      商品名
     * @param pageNumber     第几页
     * @param pageSize       每页记录
     * @return ordersPage 订单信息
     */
    public static JPAExtPaginator<Orders> findMyOrders(User user, Date createdAtBegin, Date createdAtEnd,
                                                       OrderStatus status, String goodsName,
                                                       int pageNumber, int pageSize) {
        OrdersCondition condition = new OrdersCondition();
        JPAExtPaginator<Orders> ordersPage = new JPAExtPaginator<>
                ("Orders o", "o", Orders.class, condition.getFilter(user, createdAtBegin, createdAtEnd,
                        status, goodsName),
                        condition.paramsMap)
                .orderBy(condition.getOrderByExpress());
        ordersPage.setPageNumber(pageNumber);
        ordersPage.setPageSize(pageSize);
        ordersPage.setBoundaryControlsEnabled(false);
        return ordersPage;
    }


}
