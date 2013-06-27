package models.order;

import cache.CacheHelper;
import com.google.gson.JsonObject;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.Voucher;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.consumer.UserWebIdentification;
import models.huanlegu.HuanleguMessage;
import models.huanlegu.HuanleguUtil;
import models.kangou.KangouCard;
import models.kangou.KangouUtil;
import models.ktv.KtvRoomOrderInfo;
import models.ktv.KtvTaobaoSku;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.operator.Operator;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.GoodsStatistics;
import models.sales.ImportedCoupon;
import models.sales.ImportedCouponStatus;
import models.sales.MaterialType;
import models.sales.SecKillGoodsItem;
import models.sms.SMSMessage;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.w3c.dom.Node;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.exceptions.UnexpectedException;
import play.libs.XPath;
import play.modules.paginate.JPAExtPaginator;
import play.modules.solr.Solr;
import util.common.InfoUtil;
import util.extension.ExtensionResult;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


@Entity
@Table(name = "orders")
public class Order extends Model {
    private static final long serialVersionUID = 7063112063912330652L;
    public static String EMAIL_RECEIVER = Play.configuration.getProperty("goods_not_enough.receiver", "dev@uhuila.com");
    public static final BigDecimal FREIGHT = new BigDecimal("6");
    private static final String DECIMAL_FORMAT = "0000000";
    public static final String COUPON_EXPIRE_FORMAT = "yyyy-MM-dd";

    /**
     * TODO 待更名为resalerId  下单用户ID，分销商
     */
    @Column(name = "user_id")
    public Long userId;

    @Column(name = "consumer_id", nullable = true)
    public Long consumerId;                //下单用户ID，消费者

    @Enumerated(EnumType.STRING)
    @Column(name = "orderType")
    public OrderType orderType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    @OrderBy("id")
    public List<OrderItems> orderItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<ECoupon> eCoupons;

    /**
     * 检查是否是一百券网站的订单.
     */
    @Transient
    public boolean isWebsiteOrder() {
        //FIXME: 优化一下
        Resaler sinaResaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);
        return (sinaResaler != null && this.userId.equals(sinaResaler.id))
                || Resaler.getYibaiquan().id.equals(this.userId);
    }

    /**
     * 得到此订单的付款者现金账户.
     *
     * @return 如果是网站订单，返回消费者账户；否则返回分销账户.
     */
    @Transient
    public Account getBuyerAccount() {
        if (isWebsiteOrder()) {
            return AccountUtil.getConsumerAccount(this.consumerId);
        }
        return AccountUtil.getResalerAccount(this.userId);
    }

    /**
     * 订单号
     */
    @Column(name = "order_no", unique = true)
    public String orderNumber;

    @Enumerated(EnumType.STRING)
    public OrderStatus status;

    public BigDecimal amount;       //订单总金额

    @Column(name = "account_pay")
    public BigDecimal accountPay;   //使用余额付款金额  可提现余额

    /**
     * 使用折扣码后折扣的费用.
     */
    @Column(name = "rebate_value")
    public BigDecimal rebateValue;

    /**
     * 推荐者ID
     */
    @Column(name = "promote_user_id", nullable = true)
    public Long promoteUserId;

    /**
     * 使用网银付款金额
     * discountPay = needPay - 余额支付
     */
    @Column(name = "discount_pay")
    public BigDecimal discountPay;

    @Column(name = "promotion_balance_pay")
    public BigDecimal promotionBalancePay;  //使用活动金余额付款金额


    /**
     * 订单应付金额
     * needPay = amount - rebateValue.
     */
    @Column(name = "need_pay")
    public BigDecimal needPay;

    @Column(name = "freight")
    public BigDecimal freight;      //运费

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

// TODO 待删除字段
//    @Column(name = "refund_at")
//    public Date refundAt;

    public String postcode;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @ManyToOne
    public Operator operator;

    public String description;          //订单描述

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "delivery_no")
    public String deliveryNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    public DeliveryType deliveryType;

    @Column(name = "web_identification_id")
    public Long webIdentificationId;

    @Transient
    public String searchKey;


    @Transient
    public String searchItems;

    @Column(name = "delivery_company")
    public String deliveryCompany;

    @Column(name = "voucher_value")
    public BigDecimal voucherValue;

    /**
     * 此订单已退回到账户的金额,这个最大只能等于此订单余额支付和网银支付的总额
     */
    @Column(name = "refunded_amount")
    public BigDecimal refundedAmount;

    /**
     * 支付方式名称
     */
    @Transient
    public String payMethodName;
    /**
     * 记录是消费者还是分销商的帐号,导出报表用
     */
    @Transient
    public String accountEmail;

    @Transient
    public String outerOrderNumber;

    @Column(name = "supplier_order_number")
    public String supplierOrderNumber;

    public Order() {
    }

    public static final String CACHEKEY = "ORDER";
    public static final String CACHEKEY_BASEUSERID = "ORDER_USERID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEUSERID + this.userId);

        // 更新对应商品库存
        if (this.orderItems != null) {
            for (OrderItems item : this.orderItems) {
                item.goods.refreshSaleCount();
            }
        }

        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEUSERID + this.userId);
        super._delete();
    }

    @Transient
    public long getRealGoodsOrderItemCount() {
        long count = 0L;
        for (OrderItems orderItem : orderItems) {
            if (orderItem.goods.materialType == MaterialType.REAL) {
                count++;
            }
        }
        return count;
    }

    /**
     * 新建订单
     *
     * @param userId
     * @param userType
     */
    private Order(Long userId, AccountType userType) {
        this(userId, userType, Operator.defaultOperator());
    }

    private Order(Long userId, AccountType userType, Operator operator) {
        // FIXME: 检查一下新浪微博钱包 是调用哪个接口
        if (userType == AccountType.CONSUMER) {
            User user = User.findById(userId);
            UserInfo userInfo = UserInfo.findByUser(user);
            if (userInfo != null) {
                this.buyerPhone = userInfo.phone;
            }
            this.buyerMobile = user.mobile;
            // 网站类型
            this.consumerId = userId;
            this.userId = Resaler.getYibaiquan().id;
        } else {
            this.userId = userId;   //分销商
        }

        this.operator = operator;
        this.status = OrderStatus.UNPAID;
        this.deleted = DeletedStatus.UN_DELETED;
        this.orderNumber = generateOrderNumber();
        this.orderItems = new ArrayList<>();
        this.paidAt = null;
        this.amount = BigDecimal.ZERO;
        this.needPay = BigDecimal.ZERO;
        this.accountPay = BigDecimal.ZERO;
        this.discountPay = BigDecimal.ZERO;
        this.promotionBalancePay = BigDecimal.ZERO;
        this.voucherValue = BigDecimal.ZERO;
        this.refundedAmount = BigDecimal.ZERO;
        this.freight = BigDecimal.ZERO;

        this.lockVersion = 0;

        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    /**
     * 创建普通消费订单.
     *
     * @param user 付款用户
     * @return 消费订单
     */
    public static Order createConsumeOrder(User user) {
        Order order = new Order(user.id, AccountType.CONSUMER, Operator.defaultOperator());
        order.orderType = OrderType.CONSUME;
        order.consumerId = user.id;
        return order;
    }

    public static Order createResaleOrder(Resaler resaler) {
        Order order = new Order(resaler.id, AccountType.RESALER, resaler.operator);
        order.orderType = OrderType.CONSUME;
        order.consumerId = resaler.id;
        return order;
    }

    /**
     * sina 创建消费订单.
     *
     * @param user    付款用户
     * @param resaler 分销商
     * @return 消费订单
     */
    public static Order createConsumeOrder(User user, Resaler resaler) {
        Order order = new Order(resaler.id, AccountType.RESALER, resaler.operator);
        order.orderType = OrderType.CONSUME;
        order.consumerId = user.id;
        return order;
    }

    /**
     * 创建充值订单.
     *
     * @param payerUserId 付款用户ID.
     * @return 充值订单
     */
    public static Order createChargeOrder(long payerUserId, AccountType payerAccountType) {
        Order order = new Order(payerUserId, payerAccountType);
        order.orderType = OrderType.CHARGE;
        return order;
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

    public void generateOrderDescription() {
        if (this.orderType == OrderType.CHARGE) {
            this.description = "一百券充值" + this.amount + "元";
        } else if (this.orderItems.size() == 1) {
            this.description = this.orderItems.get(0).goodsName;
        } else if (this.orderItems.size() > 1) {
            this.description = this.orderItems.get(0).goodsName + "等商品";
        }
    }

    public boolean containsRealGoods() {
        for (OrderItems orderItem : orderItems) {
            if (MaterialType.REAL.equals(orderItem.goods.materialType)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 计算单列商品应折扣的金额.
     *
     * @param g
     * @param number
     * @param discountCode
     * @return
     */
    public static BigDecimal getDiscountGoodsAmount(models.sales.Goods g,
                                                    Long number, DiscountCode discountCode) {
        BigDecimal amount = g.salePrice.multiply(new BigDecimal(number.toString()));
        BigDecimal discountValue = getDiscountValueOfGoodsAmount(g, number, discountCode);
        return amount.subtract(discountValue);
    }


    /**
     * 单品折扣
     *
     * @param g
     * @param number
     * @param discountCode
     * @return
     */
    public static BigDecimal getDiscountValueOfGoodsAmount(models.sales.Goods g,
                                                           Long number, DiscountCode discountCode) {
        if (discountCode == null || discountCode.goods == null || !discountCode.goods.id.equals(g.id)) {
            return BigDecimal.ZERO;
        }
        if (discountCode.discountAmount != null && discountCode.discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return discountCode.discountAmount.multiply(new BigDecimal(number));
        }
        if (discountCode.discountPercent != null && discountCode.discountPercent.compareTo(BigDecimal.ZERO) > 0 && discountCode.discountPercent.compareTo(BigDecimal.ONE) < 0) {
            BigDecimal amount = g.salePrice.multiply(new BigDecimal(number.toString()));
            return discountCode.discountPercent.multiply(amount);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算整单折扣。
     * 注意：只应当用于计算电子券类的折扣，实物券不参与折扣.
     *
     * @param amount
     * @param discountCode
     * @return
     */
    public static BigDecimal getDiscountTotalECartAmount(BigDecimal amount, DiscountCode discountCode) {
        return amount.subtract(getDiscountValueOfTotalECartAmount(amount, discountCode));
    }

    /**
     * 整单折扣
     *
     * @param amount
     * @param discountCode
     * @return
     */
    public static BigDecimal getDiscountValueOfTotalECartAmount(BigDecimal amount, DiscountCode discountCode) {
        if (discountCode == null || discountCode.goods != null) {
            return BigDecimal.ZERO;
        }
        if (discountCode.discountAmount != null && discountCode.discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            return discountCode.discountAmount;
        }
        if (discountCode.discountPercent != null && discountCode.discountPercent.compareTo(BigDecimal.ZERO) > 0 && discountCode.discountPercent.compareTo(BigDecimal.ONE) < 0) {
            return discountCode.discountPercent.multiply(amount);
        }
        return BigDecimal.ZERO;
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
     */
    public OrderItems addOrderItem(Goods goods, Long number, String mobile, BigDecimal salePrice, BigDecimal resalerPrice) {
        return this.addOrderItem(goods, number, mobile, salePrice, resalerPrice, null, false);
    }

    public OrderItems addOrderItem(Goods goods, Long number, String mobile, BigDecimal salePrice, BigDecimal resalerPrice,
                                   DiscountCode discountCode, boolean isPromoteFlag) {
        OrderItems orderItem = null;
        if (number > 0 && goods != null) {
            orderItem = new OrderItems(this, goods, number, mobile, salePrice, resalerPrice);
            orderItem = addOrderItem(orderItem, discountCode, isPromoteFlag);
        }

        return orderItem;
    }

    public OrderItems addOrderItem(OrderItems orderItem, DiscountCode discountCode, boolean isPromoteFlag) {
        if (orderItem.buyNumber <= 0 || orderItem.goods == null) {
            return null;
        }
        //通过推荐购买的情况
        if (isPromoteFlag) {
            orderItem.rebateValue = getPromoteRebateOfGoodsAmount(orderItem.goods, orderItem.buyNumber);
        } else {
            //用优惠码的情况
            orderItem.rebateValue = getDiscountValueOfGoodsAmount(orderItem.goods, orderItem.buyNumber, discountCode);
        }
        this.orderItems.add(orderItem);
        this.amount = this.amount.add(orderItem.getLineValue()); //计算折扣价
        this.needPay = this.amount;

        return orderItem;
    }

    public void addFreight() {
        this.amount = this.amount.add(FREIGHT);
        this.needPay = this.amount;
        this.freight = FREIGHT;
    }

    /**
     * 这个方法只被测试使用
     *
     * @param userId
     * @param accountType
     */
    public void setUser(long userId, AccountType accountType) {
        if (accountType == AccountType.CONSUMER) {
            this.consumerId = userId;
            this.userId = Resaler.getYibaiquan().id;
        } else {
            this.userId = userId;
        }
        this.save();
    }


    /**
     * 计算会员订单明细中已购买的商品
     *
     * @param goodsId 商品ID
     * @param number  购买数量
     * @return true 表示已经超过限购数量， false 表示未超过
     */
    public static Boolean checkLimitNumber(Long goodsId, Long boughtNumber, long number) {

        //取出商品的限购数量
        Goods goods = Goods.findById(goodsId);
        long limitNumber = 0L;
        if (goods.limitNumber != null) {
            limitNumber = goods.limitNumber;
        }

        //超过限购数量,则表示已经购买过该商品
        if (limitNumber > 0 && (number > limitNumber || limitNumber <= boughtNumber)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * 生成订单编号.
     *
     * @return 订单编号
     */
    public static String generateOrderNumber() {
        String numberHeader = String.valueOf(Integer.parseInt(String.format("%tj", new Date())) % 8 + 1);
        for (int i = 0; i < 100000; i++) {
            int random = new Random().nextInt(10000000);
            //使用7位的格式化工具对数字进行补零
            DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);
            String orderNumber = numberHeader + decimalFormat.format(random);
            Order order = Order.find("byOrderNumber", orderNumber).first();
            if (order == null) {
                return orderNumber;
            }
        }
        throw new RuntimeException("still could not generate an unique order number after 100000 tries");
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

    /**
     * 取消定单，增加库存，减少销量
     */
    public void cancelAndUpdateOrder() {
        this.status = OrderStatus.CANCELED;
        this.updatedAt = new Date();
        for (OrderItems orderItem : this.orderItems) {

            orderItem.status = OrderStatus.CANCELED;
            orderItem.save();
            orderItem.goods.refreshSaleCount();

            //更新在搜索服务器中goods的销量
            Solr.save(orderItem.goods);

            //如果是秒杀商品，做回库处理
            cancelSecKillOrder(orderItem);
        }
        this.save();
    }

    /**
     * 取消秒杀商品订单.
     *
     * @param orderItem
     */
    private void cancelSecKillOrder(OrderItems orderItem) {
        if (orderItem.secKillGoodsItemId != null) {
            SecKillGoodsItem goodsItem = SecKillGoodsItem.findById(orderItem.secKillGoodsItemId);
//            goodsItem.baseSale += orderItem.buyNumber;
//            goodsItem.saleCount -= orderItem.buyNumber;
            goodsItem.virtualInventory += orderItem.buyNumber;
            goodsItem.virtualSale -= orderItem.buyNumber;
            goodsItem.save();
        }
    }

    public void createAndUpdateInventory() {
        generateOrderDescription();
        save();
        boolean haveFreight = false;  //是否有物流
        for (OrderItems orderItem : orderItems) {
            // fix: org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: models.sales.GoodsLevelPrice
            // Goods goods = Goods.findById(orderItem.goods.id);
            /*
            orderItem.goods.baseSale -= orderItem.buyNumber;
            orderItem.goods.saleCount += orderItem.buyNumber;
            orderItem.goods.save();
            */
            orderItem.save();

            //FIXME: “更新搜索服务器中的商品库存”- 不应该通过保存商品的方式更新搜索服务
            Solr.save(orderItem.goods);

//            orderItem.goods.save();

            if (orderItem.goods.materialType == MaterialType.REAL) {
                haveFreight = true;
            }
            Long baseSale = orderItem.goods.getRealStocks();
            if (orderItem.goods.couponType == GoodsCouponType.IMPORT && (baseSale == 10 || baseSale == 0)) {
                //发送提醒邮件
                MailMessage mailMessage = new MailMessage();
                mailMessage.addRecipient(EMAIL_RECEIVER);
                mailMessage.setSubject(Play.mode.isProd() ? "库存不足，商品即将下架" : "商品下架【测试】");
                Supplier supplier = Supplier.findById(orderItem.goods.supplierId);
                mailMessage.putParam("supplierName", supplier.fullName);
                mailMessage.putParam("goodsName", orderItem.goods.name);
                mailMessage.putParam("faceValue", orderItem.goods.faceValue);
                mailMessage.putParam("baseSales", baseSale);
                mailMessage.putParam("offSalesFlag", "noInventory");
                MailUtil.sendGoodsOffSalesMail(mailMessage);
            }
        }

        if (haveFreight) {
            addFreight();
            save();
        }
    }

    public void payAndSendECoupon() {
        if (this.status == OrderStatus.PAID) {
            return;
        }

        //先将用户银行支付的钱充值到自己账户上
        Account account = chargeAccount();

        if (paid(account)) {
            generateECoupon();
            remindBigOrderRemark();
            this.sendOrderSMS("发送券号");
        }
    }

    public Account chargeAccount() {
        Account account = this.getBuyerAccount();
        PaymentSource paymentSource = PaymentSource.find("byCode", this.payMethod).first();

        //先将用户银行支付的钱充值到自己账户上
        if (this.discountPay.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill chargeTradeBill = TradeUtil.chargeTrade(paymentSource, account.operator)
                    .ebankPaymentAmount(this.discountPay)
                    .toAccount(account)
                    .orderId(this.getId())
                    .make();

            TradeUtil.success(chargeTradeBill, "充值");
            this.payRequestId = chargeTradeBill.getId();
        }
        this.save();
        return account;
    }

    /**
     * 订单已支付，修改支付状态、时间，更改库存，发送电子券密码
     */
    public boolean paid(Account account) {
        System.out.println("《into paid()=========:" + account);
        if (this.status != OrderStatus.UNPAID) {
            throw new RuntimeException("can not pay order:" + this.getId() + " since it's already been processed");
        }
        //更改子订单状态
        if (this.orderItems != null) {
            for (OrderItems orderItem : this.orderItems) {
                orderItem.status = OrderStatus.PAID;
                GoodsStatistics.addBuyCount(orderItem.goods.id);
                orderItem.save();
            }
        }

        if (this.webIdentificationId != null) {
            UserWebIdentification uwi = UserWebIdentification.findById(this.webIdentificationId);
            if (uwi != null) {
                if (uwi.orderCount == null) {
                    uwi.orderCount = 0;
                }
                uwi.orderCount += 1;
                if (uwi.payAmount == null) {
                    uwi.payAmount = BigDecimal.ZERO;
                }
                uwi.payAmount = uwi.payAmount.add(this.amount);
                uwi.save();
            }
        }

        //如果订单类型不是充值,那接着再支付此次订单
        if (this.orderType != OrderType.CHARGE) {
            try {
                TradeBill tradeBill = TradeUtil.orderTrade(this.operator)
                        .fromAccount(account)
                        .balancePaymentAmount(this.accountPay)
                        .ebankPaymentAmount(this.discountPay)
                        .promotionPaymentAmount(this.promotionBalancePay)
                        .paymentSource(PaymentSource.getBalanceSource())
                        .orderId(this.getId())
                        .make();

                TradeUtil.success(tradeBill, this.description);
                this.payRequestId = tradeBill.getId();
            } catch (RuntimeException e) {
                Logger.error("orderId:" + this.id + " can not pay", e);
                e.printStackTrace();
                return false;
                //忽略，此时订单没有支付，但余额已经保存
            }
        }
        this.status = OrderStatus.PAID;
        this.paidAt = new Date();
        this.save();
        return true;
    }

    /**
     * 发送电子券相关短信/邮件/通知
     */
    private void generateECoupon() {
        System.out.println(this.orderItems + "《=========this.orderItems:");

        if (this.status != OrderStatus.PAID) {
            return;
        }
        if (this.orderItems == null || this.orderItems.size() == 0) {
            throw new UnexpectedException("the paid order has no order items! order id: " + this.id);
        }
        String operator = null;
        Date expireAt = null;
        if (!isWebsiteOrder()) {
            String loginName = this.getResaler().loginName;
            String partner = this.getResaler().partner;
            operator = "分销商：" + loginName;
            //使用淘宝传过来的截止日期
            if ("TB".equals(partner)) {
                OuterOrder outerOrder = OuterOrder.find("byYbqOrder", this).first();
                if (outerOrder != null) {
                    try {
                        JsonObject jsonObject = outerOrder.getMessageAsJsonObject();
                        if (jsonObject.has("valid_ends")) {
                            String expireStr = jsonObject.get("valid_ends").getAsString(); //买家手机号
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            expireAt = format.parse(expireStr);
                        }
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
        } else {
            operator = "消费者:" + this.getUser().getShowName();
        }

        Resaler sinaResaler = Resaler.findOneByLoginName(Resaler.SINA_LOGIN_NAME);

        for (OrderItems orderItem : this.orderItems) {
            Goods goods = orderItem.goods;
            if (goods == null) {
                continue;
            }
            //如果不是电子券，跳过
            if (MaterialType.ELECTRONIC == goods.materialType) {
                boolean isKtvSupplier = false;
                //ktv商户的场合
                KtvRoomOrderInfo roomOrderInfo = null;
                if (goods.isKtvProduct()) {
                    isKtvSupplier = true;
                    roomOrderInfo = KtvRoomOrderInfo.find("orderItem=?", orderItem).first();
                }
                for (int i = 0; i < orderItem.buyNumber; i++) {
                    //创建电子券
                    ECoupon eCoupon = createCoupon(goods, orderItem);
                    if (expireAt != null) {
                        eCoupon.expireAt = expireAt;
                        eCoupon.save();
                    }

                    //在新浪微博购买产生券，更新partner
                    if (sinaResaler != null && this.userId.equals(sinaResaler.id)) {
                        eCoupon.partner = ECouponPartner.SINA;
                        eCoupon.save();
                    }

                    //ktv商户的话，更新券的价格信息
                    if (isKtvSupplier && roomOrderInfo != null) {
                        eCoupon.appointmentDate = roomOrderInfo.scheduledDay;
                        eCoupon.appointmentRemark =
                                roomOrderInfo.roomType.getName() + eCoupon.salePrice + "元," +
                                        KtvTaobaoSku.humanTimeRange(roomOrderInfo.scheduledTime,
                                                roomOrderInfo.scheduledTime + roomOrderInfo.product.duration);
                        eCoupon.effectiveAt = new Date();
                        eCoupon.expireAt = DateUtils.addDays(DateUtils.ceiling(roomOrderInfo.scheduledDay, Calendar.DATE), 10);
                        eCoupon.save();
                    }

                    String remark = "产生券号";
                    //二次验证商品记录预付订金
                    if (goods.isSecondaryVerificationGoods()) {
                        remark = "产生预约券号";
                        eCoupon.advancedDeposit = goods.advancedDeposit;
                        eCoupon.save();
                    }
                    //记录券历史信息
                    ECouponHistoryMessage.with(eCoupon).operator(operator)
                            .remark(remark).fromStatus(ECouponStatus.UNCONSUMED).toStatus(ECouponStatus.UNCONSUMED)
                            .sendToMQ();
                }

                //ktv商户的场合,发送券之后更新ktvRoomOrder订单的状态和时间
                if (isKtvSupplier && roomOrderInfo != null) {
                    roomOrderInfo.dealKtvRoom();
                }
            }

            //邮件提醒
            sendPaidMail(goods, orderItem);
        }

    }

    /**
     * 邮件提醒
     */
    private void sendPaidMail(Goods goods, OrderItems orderItem) {
        //抽奖商品不提醒
        if (goods.isLottery != null && goods.isLottery) {
            return;
        }
        MailMessage mail = new MailMessage();
        //只有消费者才发邮件
        if (orderItem.order.isWebsiteOrder()) {
            String email = orderItem.order.getUser().loginName;
            if (StringUtils.isNotBlank(email)) {
                //消费者
                mail.addRecipient(email);
                String note = "";
                if (this.orderItems.size() > 1) {
                    note = "等件";
                }
                String content = "您已成功购买" + goods.name + note + "订单号是" + this
                        .orderNumber + "，支付金额是" + this.amount + "元。\r";

                mail.putParam("full_name", content);
                MailUtil.sendCouponMail(mail);
            }
        }
    }

    /**
     * 当订单金额大于一定额度时，如果用户有留言，提醒运营人员
     */
    private void remindBigOrderRemark() {
        if (StringUtils.isBlank(remark)
                || amount.compareTo(new BigDecimal("300.00")) < 0) {
            return;
        }
        String goodsName = "";
        if (realGoods != null && realGoods.size() > 0) {
            for (Goods g : realGoods) {
                goodsName += g.name + " ";
            }
        }
        if (electronicGoods != null && electronicGoods.size() > 0) {
            for (Goods g : electronicGoods) {
                goodsName += g.name + " ";
            }
        }

        //发送提醒邮件
        MailMessage mailMessage = new MailMessage();
        mailMessage.addRecipient("op@uhuila.com");
        mailMessage.setSubject(Play.mode.isProd() ? "客户留言" : "客户留言【测试】");
        mailMessage.putParam("orderNumber", orderNumber);
        mailMessage.putParam("remark", remark);
        mailMessage.putParam("goodsName", goodsName);
        mailMessage.putParam("phone", buyerMobile);
        mailMessage.putParam("orderId", id);
        mailMessage.putParam("addr", play.Play.configuration.getProperty("application.baseUrl"));
        MailUtil.sendCustomerRemarkMail(mailMessage);
    }

    /**
     * 创建 一个券
     *
     * @return 一个电子券
     */
    private ECoupon createCoupon(Goods goods, OrderItems orderItem) {
        ECoupon eCoupon = null;
        //支持导入券号
        if (goods.couponType == GoodsCouponType.IMPORT) {
            ImportedCoupon importedCoupon = ImportedCoupon.find("byGoodsAndStatus", goods, ImportedCouponStatus.UNUSED).first();
            if (importedCoupon == null) {
                throw new RuntimeException("can not find an imported coupon of goods " + goods.getId());
            } else {
                eCoupon = new ECoupon(this, goods, orderItem, importedCoupon.coupon, importedCoupon.password).save();

                eCoupon.autoVerify();
                importedCoupon.status = ImportedCouponStatus.USED;
                importedCoupon.save();
            }
        } else {
            eCoupon = new ECoupon(this, goods, orderItem);
            eCoupon.save();

            if (KangouUtil.SUPPLIER_DOMAIN_NAME.equals(orderItem.goods.getSupplier().domainName)) {
                KangouCard card = KangouUtil.getCardId(eCoupon);

                if (card == null) {
                    eCoupon.delete();
                    throw new RuntimeException("can not generate a kangou goods: " + goods.getId());
                }
                eCoupon.createType = ECouponCreateType.IMPORT;
                eCoupon.supplierECouponId = card.cardId;
                eCoupon.eCouponSn = card.cardNumber;
                eCoupon.save();
                KangouUtil.setCardUseAndSend(eCoupon);
                eCoupon.autoVerify();  // 自动验证掉
            }

        }
        return eCoupon;
    }


    @Transient
    private User user;
    @Transient
    private Resaler resaler;

    @Transient
    public User getUser() {
        if (user == null) {
            if (consumerId != null) {
                user = User.findById(consumerId);
            }
        }
        return user;
    }

    @Transient
    public Resaler getResaler() {
        if (resaler == null && userId != null) {
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
        if (user == null) {
            user = new User();
        }

        JPAExtPaginator<Order> orderPage = new JPAExtPaginator<>
                ("Order o", "o", Order.class, condition.getFilter(user),
                        condition.paramsMap)
                .orderBy(condition.getUserOrderByExpress());
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);
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
        return orderPage;
    }

    /*
        返回订单的状态
     */
    @Transient
    public OrderStatus getRealGoodsStatus() {
        return this.status;
    }

    @Transient
    public OrderStatus getElectronicGoodsStatus() {
        return this.status;
    }

    @Transient
    private List<Goods> realGoods = new ArrayList<>();
    @Transient
    private List<Goods> electronicGoods = new ArrayList<>();

    /**
     * 得到订单的所有实体券
     *
     * @return
     */
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

    /**
     * 得到订单的所有电子券
     *
     * @return
     */
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

    public static void sendRealGoodsAndPayCommissions(long id, String deliveryCompany, String deliveryNo) {
        sendRealGoods(id, deliveryCompany, deliveryNo);
        payRealGoodsCommissions(id);
    }

    public static void sendRealGoods(long id, String deliveryCompany, String deliveryNo) {
        Order order = Order.findById(id);
        if (order == null || order.deliveryCompany != null || order.deliveryNo != null) {
            return;
        }
        order.deliveryCompany = deliveryCompany;
        order.deliveryNo = deliveryNo;
        order.save();
        for (OrderItems orderItem : order.orderItems) {
            if (MaterialType.REAL.equals(orderItem.goods.materialType)) {
                orderItem.status = OrderStatus.SENT;
                orderItem.sendAt = new Date();
                orderItem.save();
            }
        }

        modifyOrderStatusByItems(order);
        order.save();
    }

    public static void payRealGoodsCommissions(long id) {
        Order order = Order.findById(id);
        if (order == null) {
            return;
        }
        for (OrderItems orderItem : order.orderItems) {
            if (MaterialType.REAL.equals(orderItem.goods.materialType)) {

                //给商户打钱
                Account supplierAccount = AccountUtil.getSupplierAccount(orderItem.goods.supplierId, Operator.defaultOperator());
                TradeBill consumeTrade = TradeUtil.consumeTrade(order.operator)
                        .toAccount(supplierAccount)
                        .balancePaymentAmount(orderItem.originalPrice.multiply(new BigDecimal(orderItem.buyNumber)))
                        .coupon(orderItem.goods.name)
                        .orderId(order.getId())
                        .make();
                TradeUtil.success(consumeTrade, order.description);

                BigDecimal platformCommission;
                if (orderItem.salePrice.compareTo(orderItem.resalerPrice) < 0) {
                    //如果成交价小于分销商成本价（这种情况只有在一百券网站上才会发生），
                    //那么一百券就没有佣金，平台的佣金也变为成交价减成本价
                    platformCommission = orderItem.salePrice.subtract(orderItem.originalPrice);
                } else {
                    //平台的佣金等于分销商成本价减成本价
                    platformCommission = orderItem.resalerPrice.subtract(orderItem.originalPrice);
                    //如果是在一百券网站下的单，还要给一百券佣金
                    if (order.isWebsiteOrder()) {
                        TradeBill uhuilaCommissionTrade = TradeUtil.commissionTrade(order.operator)
                                .toAccount(AccountUtil.getUhuilaAccount(order.operator))
                                .balancePaymentAmount(orderItem.salePrice.subtract(orderItem.resalerPrice).multiply(new BigDecimal(orderItem.buyNumber)))
                                .orderId(order.getId())
                                .make();

                        TradeUtil.success(uhuilaCommissionTrade, order.description);
                    }
                }

                if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
                    //给优惠券平台佣金
                    TradeBill platformCommissionTrade = TradeUtil.commissionTrade(order.operator)
                            .toAccount(AccountUtil.getPlatformCommissionAccount(order.operator))
                            .balancePaymentAmount(platformCommission.multiply(new BigDecimal(orderItem.buyNumber)))
                            .orderId(order.getId())
                            .make();
                    TradeUtil.success(platformCommissionTrade, order.description);
                }
            }
        }

        if (order.freight != null && order.freight.compareTo(BigDecimal.ZERO) >= 0) {
            //给优惠券平台佣金
            TradeBill freightTrade = TradeUtil.freightTrade(order.operator)
                    .toAccount(AccountUtil.getPlatformCommissionAccount(order.operator))
                    .balancePaymentAmount(order.freight)
                    .orderId(order.getId())
                    .make();
            TradeUtil.success(freightTrade, "运费:" + order.description);
        }
    }

    /**
     * 检查订单项的发货状态，只要有一个未发货就认为是未发货，只有全部为已发货状态的才能认为订单是已发货。
     *
     * @param order
     */
    private static void modifyOrderStatusByItems(Order order) {
        OrderStatus status = OrderStatus.SENT;
        if (order.status.equals(OrderStatus.PAID)) {
            for (OrderItems item : order.orderItems) {
                if (!item.status.equals(OrderStatus.SENT)) {
                    status = OrderStatus.PAID;
                }
                if (status.equals(OrderStatus.SENT)) {
                    order.status = OrderStatus.SENT;
                }
            }

        }
    }

    public static Order findOneByResaler(String orderNumber, Resaler resaler) {
        return Order.find("byOrderNumberAndUserId", orderNumber, resaler.id).first();
    }

    public static Order findOneByUser(String orderNumber, User user) {
        return Order.find("byOrderNumberAndConsumerId", orderNumber, user.id).first();
    }

    public static boolean verifyAndPay(String orderNumber, String fee, String paymentCode) {
        Order order = Order.find("byOrderNumber", orderNumber).first();
        if (order == null) {
            Logger.error("payment_notify:找不到订单:" + orderNumber);
            return false;
        }
        if (order.status == OrderStatus.PAID) {
            Logger.info("payment_notify:订单已支付:" + orderNumber);
            return true;
        }
        if (fee == null || new BigDecimal(fee).compareTo(order.discountPay) < 0) {
            Logger.error("payment_notify:支付金额非法:订单:" + orderNumber + ";支付金额:" + fee);
            return false;
        }
        PaymentSource paymentSource = PaymentSource.findByCode(paymentCode);
        if (paymentSource == null) {
            Logger.error("payment_notify:找不到支付方式: %s", paymentCode);
            return false;
        }
        //重新设置支付方式，这才是最终的支付方式
        order.payMethod = paymentSource.code;

        order.payAndSendECoupon();
        return true;
    }

    public static boolean confirmPaymentInfo(Order order, Account account, boolean useBalance, String paymentSourceCode) {
        return confirmPaymentInfo(order, account, useBalance, paymentSourceCode, null);
    }

    public static boolean confirmPaymentInfo(Order order, Account account, boolean useBalance,
                                             String paymentSourceCode,
                                             List<Voucher> vouchers) {
        //使用抵用券
        order.voucherValue = BigDecimal.ZERO;
        List<Voucher> validVouchers = new ArrayList<>();
        if (vouchers != null) {
            for (Voucher voucher : vouchers) {
                if (Voucher.canAssign(account, voucher)) {
                    order.voucherValue = order.voucherValue.add(voucher.value);
                    validVouchers.add(voucher);
                }
            }
        }
        if (order.voucherValue.compareTo(order.needPay) > 0) {
            order.voucherValue = order.needPay;
        }

        BigDecimal needPayExceptVoucher = order.needPay.subtract(order.voucherValue);

        //有些账号还没有promotionAmount
        if (account.promotionAmount == null) {
            account.promotionAmount = BigDecimal.ZERO;
        }
        //计算使用余额支付和使用银行卡支付的金额
        BigDecimal balancePaymentAmount = BigDecimal.ZERO;
        BigDecimal ebankPaymentAmount = BigDecimal.ZERO;
        //先计算总余额支付和银行卡支付
        if (useBalance && order.orderType == OrderType.CONSUME) {
            balancePaymentAmount = account.amount.add(account.promotionAmount).min(needPayExceptVoucher);
            ebankPaymentAmount = needPayExceptVoucher.subtract(balancePaymentAmount);
        } else {
            ebankPaymentAmount = needPayExceptVoucher;
        }
        //余额支付中再分一下可提现和不可提现支付
        System.out.println(order.discountPay + "《=inital========order.discountPay:");
        order.accountPay = balancePaymentAmount.subtract(account.promotionAmount.min(balancePaymentAmount));
        order.promotionBalancePay = balancePaymentAmount.subtract(order.accountPay);
        order.discountPay = ebankPaymentAmount;


        //创建订单交易
        //如果使用余额足以支付，则付款直接成功
        System.out.println("《=========:");
        System.out.println(order.discountPay + "《===order.discountPay======:");
        System.out.println(order.accountPay + "《=========order.accountPay:");
        System.out.println(order.promotionBalancePay + "《=========order.promotionBalancePay:");
        System.out.println(order.voucherValue + "《=========order.voucherValue:");
        System.out.println(order.needPay + "《=========order.needPay:");

        if (order.discountPay.compareTo(BigDecimal.ZERO) == 0
                && order.accountPay.add(order.promotionBalancePay).add(order.voucherValue)
                .compareTo(order.needPay) == 0) {
            order.payMethod = PaymentSource.getBalanceSource().code;
            System.out.println("before send coupon《=========:");
            order.payAndSendECoupon();
            useVouchers(validVouchers, order);
            return true;
        }

        PaymentSource paymentSource = PaymentSource.findByCode(paymentSourceCode);
        order.payMethod = paymentSourceCode;

        //无法确定支付渠道
        if (paymentSource == null) {
            return false;
        }

        order.save();
        //试用抵用券
        useVouchers(validVouchers, order);
        return true;
    }

    private static void useVouchers(List<Voucher> vouchers, Order order) {
        for (Voucher voucher : vouchers) {
            voucher.order = order;
            voucher.usedAt = new Date();
            voucher.save();
        }
    }

    /**
     * 统计订单总金额/
     *
     * @param orderList
     * @return
     */
    public static BigDecimal summary(JPAExtPaginator<Order> orderList) {

        BigDecimal amount = BigDecimal.ZERO;

        for (Order order : orderList) {
            amount = amount.add(order.amount);
        }
        return amount;
    }

    /**
     * 计算一笔订单所产生的返利金额
     *
     * @return
     */
    public static BigDecimal getPromoteRebateAmount(Order order) {
        BigDecimal promoterPrice;
        BigDecimal addAmount = BigDecimal.ZERO;
        for (OrderItems item : order.orderItems) {
            BigDecimal amount = BigDecimal.ZERO;
            //默认给推荐人2%，如果商品没设置返利
            promoterPrice = item.goods.promoterPrice == null ? new BigDecimal(2) : item.goods.promoterPrice;
            amount = amount.add(item.goods.salePrice.multiply(promoterPrice)).multiply(new BigDecimal(0.01));
            amount = amount.setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(item.buyNumber));
            addAmount = addAmount.add(amount);
        }
        return addAmount;
    }

    /**
     * 计算推荐者推荐购物的金额
     *
     * @return
     */
    public static BigDecimal getBoughtPromoteRebateAmount(Long promoteUserId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( o.amount ) FROM Order o,PromoteRebate p WHERE p.order=o and" +
                " o.promoteUserId=:promoteUserId and p.status=:p_status");
        q.setParameter("promoteUserId", promoteUserId);
        q.setParameter("p_status", RebateStatus.UN_CONSUMED);
        Object result = q.getSingleResult();
        return result == null ? BigDecimal.ZERO : (BigDecimal) result;
    }

    /**
     * 计算返利后的差价
     *
     * @param goods
     * @param number
     * @return
     */
    public static BigDecimal getPromoteRebateOfTotalGoodsAmount(Goods goods, Long number) {
        BigDecimal amount = goods.salePrice.multiply(new BigDecimal(number.toString()));
        return amount.subtract(getPromoteRebateOfGoodsAmount(goods, number));
    }

    /**
     * 计算每件商品的返利金额
     *
     * @param goods
     * @param number
     * @return
     */
    public static BigDecimal getPromoteRebateOfGoodsAmount(Goods goods, Long number) {
        //默认给被推荐人1%，如果商品没设置返利
        BigDecimal invitedUserPrice = goods.invitedUserPrice == null ? BigDecimal.ONE : goods.invitedUserPrice;
        return goods.salePrice.multiply(invitedUserPrice).multiply(new BigDecimal(number)).multiply(new BigDecimal(0.01));
    }

    public User getUserInfo() {
        User user = this.getUser();
        return user;
    }

    /**
     * 发送此订单的所有券的购买通知短信
     */
    public void sendOrderSMS(String remark) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);
        for (OrderItems item : this.orderItems) {
            //二次验证商品记录预付订金
            if (item.goods.isSecondaryVerificationGoods()) {
                remark = "发送预约券号";
            }
            OrderECouponMessage.with(item).remark(remark).sendToMQ();

            //ktv商户发送给店员短信
            if (item.goods.isKtvProduct()) {
                KtvRoomOrderInfo roomOrderInfo = KtvRoomOrderInfo.find("orderItem", item).first();
                if (roomOrderInfo == null || roomOrderInfo.shop == null || StringUtils.isBlank(roomOrderInfo.shop.managerMobiles)) {
                    break;
                }
                if (roomOrderInfo.scheduledDay.compareTo(DateUtils.truncate(new Date(), Calendar.DATE)) >= 0) {
                    String content = dateFormat.format(roomOrderInfo.scheduledDay) + "," + roomOrderInfo.shop.name +
                            "预订【" + item.phone + roomOrderInfo.roomType.getName() + "(" + item.buyNumber + "间)" +
                            KtvTaobaoSku.humanTimeRange(roomOrderInfo.scheduledTime, roomOrderInfo.scheduledTime + roomOrderInfo.product.duration) + "】";
                    String[] mobileArray = StringUtils.trimToEmpty(roomOrderInfo.shop.managerMobiles).split(",");
                    for (String mobile : mobileArray) {
                        new SMSMessage(content, mobile).send();
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.userId)
                .append(this.status).append(this.id).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.userId, other.userId)
                .append(this.status, other.status).append(this.id, other.id).isEquals();
    }

}
