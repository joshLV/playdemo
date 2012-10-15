package models.order;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.admin.SupplierUser;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.consumer.UserWebIdentification;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.resale.Resaler;
import models.resale.util.ResaleUtil;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.GoodsStatistics;
import models.sales.ImportedCoupon;
import models.sales.ImportedCouponStatus;
import models.sales.MaterialType;
import models.sales.SecKillGoodsItem;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import play.Logger;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Entity
@Table(name = "orders")
public class Order extends Model {
    private static final long serialVersionUID = 7063112063912330652L;
    public static String EMAIL_RECEIVER = Play.configuration.getProperty("goods_not_enough.receiver", "dev@uhuila.com");
    public static final BigDecimal FREIGHT = new BigDecimal("6");
    private static final String DECIMAL_FORMAT = "0000000";
    private static final String COUPON_EXPIRE_FORMAT = "yyyy-MM-dd";

    @Column(name = "user_id")
    public long userId;                     //下单用户ID，可能是一百券用户，也可能是分销商

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    public AccountType userType;            //用户类型，个人/分销商

    @Enumerated(EnumType.STRING)
    @Column(name = "orderType")
    public OrderType orderType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    @OrderBy("id")
    public List<OrderItems> orderItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    public List<ECoupon> eCoupons;

    @Column(name = "order_no", unique = true)
    public String orderNumber;

    @Enumerated(EnumType.STRING)
    public OrderStatus status;

    public BigDecimal amount;       //订单总金额

    @Column(name = "account_pay")
    public BigDecimal accountPay;   //使用余额付款金额

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

    @Column(name = "refunded_promotion_amount")
    public BigDecimal refundedPromotionAmount;  //此订单已退款的用活动金支付的金额

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

    @Index(name = "ext_request_sn")
    @Column(name = "ext_request_sn")
    public String extRequestSN;

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

    public Order() {
    }

    public static final String CACHEKEY = "ORDER";
    public static final String CACHEKEY_BASEUSERID = "ORDER_USERID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEUSERID + this.userId);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEUSERID + this.userId);
        super._delete();
    }

    private Order(long userId, AccountType userType) {
        this.userId = userId;
        this.userType = userType;

        if (userType == AccountType.CONSUMER) {
            User user = User.findById(userId);
            UserInfo userInfo = UserInfo.findByUser(user);
            if (userInfo != null) {
                this.buyerPhone = userInfo.phone;
            }
            this.buyerMobile = user.mobile;
        }

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
        this.refundedPromotionAmount = BigDecimal.ZERO;
        this.freight = BigDecimal.ZERO;

        this.lockVersion = 0;

        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    /**
     * 创建普通消费订单.
     *
     * @param userId      付款用户ID
     * @param accountType 付款用户账户类型
     * @return 消费订单
     */
    public static Order createConsumeOrder(long userId, AccountType accountType) {
        Order order = new Order(userId, accountType);
        order.orderType = OrderType.CONSUME;
        return order;
    }

    /**
     * 创建充值订单.
     *
     * @param payerUserId      付款用户ID.
     * @param payerAccountType 付款用户类型.
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
                                                    Integer number, DiscountCode discountCode) {
        BigDecimal amount = g.salePrice.multiply(new BigDecimal(number.toString()));
        BigDecimal discountValue = getDiscountValueOfGoodsAmount(g, number, discountCode);
        return amount.subtract(discountValue);
    }

    public static BigDecimal getDiscountValueOfGoodsAmount(models.sales.Goods g,
                                                           Integer number, DiscountCode discountCode) {
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
     * @throws NotEnoughInventoryException
     */
    public OrderItems addOrderItem(Goods goods, Integer number, String mobile, BigDecimal salePrice, BigDecimal resalerPrice) throws NotEnoughInventoryException {
        return this.addOrderItem(goods, number, mobile, salePrice, resalerPrice, null, false);
    }

    public OrderItems addOrderItem(Goods goods, Integer number, String mobile, BigDecimal salePrice, BigDecimal resalerPrice,
                                   DiscountCode discountCode, boolean isPromoteFlag)
            throws NotEnoughInventoryException {
        OrderItems orderItem = null;
        if (number > 0 && goods != null) {
            checkInventory(goods, number);
            orderItem = new OrderItems(this, goods, number, mobile, salePrice, resalerPrice);
            //通过推荐购买的情况
            if (isPromoteFlag) {
                orderItem.rebateValue = getPromoteRebateOfGoodsAmount(goods, number);
            } else {
                //用优惠码的情况
                orderItem.rebateValue = getDiscountValueOfGoodsAmount(goods, number, discountCode);
            }
            this.orderItems.add(orderItem);
            this.amount = this.amount.add(orderItem.getLineValue()); //计算折扣价
            this.needPay = this.amount;
        }
        return orderItem;
    }

    public void addFreight() {
        this.amount = this.amount.add(FREIGHT);
        this.needPay = this.amount;
        this.freight = FREIGHT;
    }


    public void setUser(long userId, AccountType accountType) {
        this.userId = userId;
        this.userType = accountType;
        this.save();
    }


    /**
     * 计算会员订单明细中已购买的商品
     *
     * @param user    会员ID
     * @param goodsId 商品ID
     * @param number  购买数量
     * @return
     */
    public static Boolean checkLimitNumber(User user, Long goodsId, Long boughtNumber, int number) {

        //取出商品的限购数量
        Goods goods = Goods.findById(goodsId);
        int limitNumber = 0;
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

    /**
     * 取消定单，增加库存，减少销量
     */
    public void cancelAndUpdateOrder() {
        this.status = OrderStatus.CANCELED;
        this.updatedAt = new Date();
        for (OrderItems orderItem : this.orderItems) {
            orderItem.goods.baseSale += orderItem.buyNumber;
            orderItem.goods.saleCount -= orderItem.buyNumber;
            orderItem.status = OrderStatus.CANCELED;
            orderItem.goods.save();
            orderItem.save();
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
            goodsItem.baseSale += orderItem.buyNumber;
            goodsItem.saleCount -= orderItem.buyNumber;
            goodsItem.virtualInventory += orderItem.buyNumber;
            goodsItem.virtualSale -= orderItem.buyNumber;
            goodsItem.save();
        }
    }


    public void createAndUpdateInventory() {
        generateOrderDescription();
        save();
        boolean haveFreight = false;
        for (OrderItems orderItem : orderItems) {
            // fix: org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing: models.sales.GoodsLevelPrice
            // Goods goods = Goods.findById(orderItem.goods.id);
            orderItem.goods.baseSale -= orderItem.buyNumber;
            orderItem.goods.saleCount += orderItem.buyNumber;
            orderItem.goods.save();
            orderItem.save();
            if (orderItem.goods.materialType == MaterialType.REAL) {
                haveFreight = true;
            }
            if (orderItem.goods.baseSale == 10 || orderItem.goods.baseSale == 0) {
                //发送提醒邮件
                MailMessage mailMessage = new MailMessage();
                mailMessage.addRecipient(EMAIL_RECEIVER);
                mailMessage.setSubject(Play.mode.isProd() ? "库存不足，商品即将下架" : "商品下架【测试】");
                Supplier supplier = Supplier.findById(orderItem.goods.supplierId);
                mailMessage.putParam("supplierName", supplier.fullName);
                mailMessage.putParam("goodsName", orderItem.goods.name);
                mailMessage.putParam("faceValue", orderItem.goods.faceValue);
                mailMessage.putParam("baseSales", orderItem.goods.baseSale);
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

        if (paid()) {
            sendECoupon();
        }
    }

    /**
     * 订单已支付，修改支付状态、时间，更改库存，发送电子券密码
     */
    public boolean paid() {
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

        Account account = AccountUtil.getAccount(this.userId, this.userType);
        PaymentSource paymentSource = PaymentSource.find("byCode", this.payMethod).first();

        //先将用户银行支付的钱充值到自己账户上
        if (this.discountPay.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill chargeTradeBill = TradeUtil.createChargeTrade(account, this.discountPay, paymentSource, this.getId());
            TradeUtil.success(chargeTradeBill, "充值");
            this.payRequestId = chargeTradeBill.getId();
        }

        //如果订单类型不是充值,那接着再支付此次订单
        if (this.orderType != OrderType.CHARGE) {
            try {
                TradeBill tradeBill = TradeUtil.createOrderTrade(
                        account,
                        this.accountPay,
                        this.discountPay,
                        BigDecimal.ZERO,
                        this.promotionBalancePay,
                        PaymentSource.getBalanceSource(),
                        this.getId());
                TradeUtil.success(tradeBill, this.description);
                this.payRequestId = tradeBill.getId();
            } catch (RuntimeException e) {
                Logger.error("can not pay", e);
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
    private void sendECoupon() {
        if (this.status != OrderStatus.PAID) {
            return;
        }
        if (this.orderItems == null) {
            return;
        }
        for (OrderItems orderItem : this.orderItems) {
            Goods goods = orderItem.goods;
            if (goods == null) {
                continue;
            }
            //如果是电子券
            if (MaterialType.ELECTRONIC.equals(goods.materialType)) {
                List<String> couponCodes = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat(COUPON_EXPIRE_FORMAT);
                for (int i = 0; i < orderItem.buyNumber; i++) {
                    ECoupon eCoupon = null;
                    //支持导入券号
                    if (goods.couponType == GoodsCouponType.IMPORT) {
                        ImportedCoupon importedCoupon = ImportedCoupon.find("byGoodsAndStatus", goods, ImportedCouponStatus.UNUSED).first();
                        if (importedCoupon == null) {
                            throw new RuntimeException("can not find an imported coupon of goods " + goods.getId());
                        } else {
                            eCoupon = new ECoupon(this, goods, orderItem, importedCoupon.coupon).save();
                            Supplier supplier = Supplier.findById(goods.supplierId);
                            SupplierUser supplierUser = SupplierUser.find("bySupplier", supplier).first();
                            if (supplierUser == null) {
                                throw new RuntimeException("can not find a supplierUser of goods " + goods.getId());
                            }
                            eCoupon.consumeAndPayCommission(supplierUser.shop.id, null, supplierUser, VerifyCouponType.IMPORT_VERIFY);
                            eCoupon.save();
                            importedCoupon.status = ImportedCouponStatus.USED;
                            importedCoupon.save();
                        }
                    } else {
                        eCoupon = new ECoupon(this, goods, orderItem).save();
                    }
                    //记录券历史信息
                    new CouponHistory(eCoupon.eCouponSn, AccountType.RESALER.equals(orderItem.order.userType) ? "分销商："+orderItem.order.getResaler().loginName :"消费者:"+ orderItem.order.getUser().getShowName(), "产生券号", ECouponStatus.UNCONSUMED, ECouponStatus.UNCONSUMED, null).save();

                    if (!Play.runingInTestMode() && (goods.isLottery == null || !goods.isLottery)) {
                        SMSUtil.send("【一百券】" + (StringUtils.isNotEmpty(goods.title) ? goods.title : (goods.name +
                                "[" + goods.faceValue + "元]")) + "券号" + eCoupon.eCouponSn + "," +
                                "截止" + dateFormat.format(eCoupon.expireAt) + "客服4006262166",
                                orderItem.phone, eCoupon.replyCode);
                    }
                    couponCodes.add(eCoupon.getMaskedEcouponSn());
                }
                if (goods.isLottery == null || !goods.isLottery) {
                    MailMessage mail = new MailMessage();
                    //分销商
                    if (AccountType.RESALER.equals(orderItem.order.userType)) {
                        mail.addRecipient(orderItem.order.getResaler().email);
                    } else {
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
            }
            goods.save();
        }
    }


    @Transient
    private User user;
    @Transient
    private Resaler resaler;

    @Transient
    public User getUser() {
        if (user == null) {
            user = User.findById(userId);
        }
        return user;
    }

    @Transient
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
        if (user == null) {
            user = new User();
        }

        JPAExtPaginator<Order> orderPage = new JPAExtPaginator<>
                ("Order o", "o", Order.class, condition.getFilter(user),
                        condition.paramsMap)
                .orderBy(condition.getUserOrderByExpress());
        orderPage.setPageNumber(pageNumber);
        orderPage.setPageSize(pageSize);
        System.out.println("orderPage.size():" + orderPage.size());
        return orderPage;
    }

    public static List<Order> findUserListOrders(User user) {
        if (user == null) {
            user = new User();
        }
        List<Order> orderList = null;
        Order order = null;
        if (order.getUser() == user)
            return orderList;
        else
            return null;
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

    @Transient
    static Map totalMap = new HashMap();

    public static Map getTotalMap() {
        return totalMap;
    }

    /**
     * 本月订单总数（成功的和进行中的）
     *
     * @param resaler 分销商
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
                Account supplierAccount = AccountUtil.getSupplierAccount(orderItem.goods.supplierId);
                TradeBill consumeTrade = TradeUtil.createConsumeTrade(
                        orderItem.goods.name,
                        supplierAccount,
                        orderItem.originalPrice.multiply(new BigDecimal(orderItem.buyNumber)),
                        order.getId());
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
                    if (order.userType == AccountType.CONSUMER) {
                        TradeBill uhuilaCommissionTrade = TradeUtil.createCommissionTrade(
                                AccountUtil.getUhuilaAccount(),
                                orderItem.salePrice.subtract(orderItem.resalerPrice).multiply(new BigDecimal(orderItem.buyNumber)),
                                "",
                                order.getId());

                        TradeUtil.success(uhuilaCommissionTrade, order.description);
                    }
                }

                if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
                    //给优惠券平台佣金
                    TradeBill platformCommissionTrade = TradeUtil.createCommissionTrade(
                            AccountUtil.getPlatformCommissionAccount(),
                            platformCommission.multiply(new BigDecimal(orderItem.buyNumber)),
                            "",
                            order.getId());
                    TradeUtil.success(platformCommissionTrade, order.description);
                }
            }
        }

        if (order.freight != null && order.freight.compareTo(BigDecimal.ZERO) >= 0) {
            //给优惠券平台佣金
            TradeBill freightTrade = TradeUtil.createFreightTrade(
                    AccountUtil.getPlatformCommissionAccount(),
                    order.freight,
                    order.getId()
            );
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

    public static Order findOneByUser(String orderNumber, Long userId, AccountType accountType) {
        return Order.find("byOrderNumberAndUserIdAndUserType", orderNumber, userId, accountType).first();
    }

    public static boolean verifyAndPay(String orderNumber, String fee) {
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

        order.payAndSendECoupon();
        return true;
    }

    public static boolean confirmPaymentInfo(Order order, Account account, boolean useBalance, String paymentSourceCode) {
        //有些账号还没有promotionAmount
        if (account.promotionAmount == null) {
            account.promotionAmount = BigDecimal.ZERO;
        }
        //计算使用余额支付和使用银行卡支付的金额
        BigDecimal balancePaymentAmount = BigDecimal.ZERO;
        BigDecimal ebankPaymentAmount = BigDecimal.ZERO;
        //先计算总余额支付和银行卡支付
        if (useBalance && order.orderType == OrderType.CONSUME) {
            balancePaymentAmount = account.amount.add(account.promotionAmount).min(order.needPay);
            ebankPaymentAmount = order.needPay.subtract(balancePaymentAmount);
        } else {
            ebankPaymentAmount = order.needPay;
        }
        //余额支付中再分一下可提现和不可提现支付
        order.accountPay = balancePaymentAmount.subtract(account.promotionAmount.min(balancePaymentAmount));
        order.promotionBalancePay = balancePaymentAmount.subtract(order.accountPay);
        order.discountPay = ebankPaymentAmount;

        //创建订单交易
        //如果使用余额足以支付，则付款直接成功
        if (order.discountPay.compareTo(BigDecimal.ZERO) == 0
                && order.accountPay.add(order.promotionBalancePay).compareTo(order.needPay) == 0) {
            order.payMethod = PaymentSource.getBalanceSource().code;
            order.payAndSendECoupon();
            return true;
        }

        PaymentSource paymentSource = PaymentSource.findByCode(paymentSourceCode);
        order.payMethod = paymentSourceCode;

        //无法确定支付渠道
        if (paymentSource == null) {
            return false;
        }

        order.save();
        return true;
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
     * 计算订单中受邀者应得的返利
     * <p/>
     * FIXME: 这个方法不需要的。
     *
     * @param order
     * @return
     */
    @Deprecated
    public static BigDecimal getPromoteRebateOfTotalECartAmount_todelete(Order order) {
        BigDecimal addAmount = BigDecimal.ZERO;
        BigDecimal invitedUserPrice;
        for (OrderItems item : order.orderItems) {
            BigDecimal rebatePrice = BigDecimal.ZERO;
            //如果没设置被推荐的返利，默认给1%
            invitedUserPrice = item.goods.invitedUserPrice == null ? BigDecimal.ONE : item.goods.invitedUserPrice;
            if (invitedUserPrice.compareTo(BigDecimal.ZERO) > 0 && invitedUserPrice.compareTo(new BigDecimal(5)) <= 0) {
                rebatePrice = rebatePrice.add(item.goods.salePrice.multiply(invitedUserPrice)).multiply(new BigDecimal(0.01));
                rebatePrice = rebatePrice.setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(item.buyNumber));
                addAmount = addAmount.add(rebatePrice);
            }
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
    public static BigDecimal getPromoteRebateOfTotalGoodsAmount(Goods goods, Integer number) {
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
    public static BigDecimal getPromoteRebateOfGoodsAmount(Goods goods, Integer number) {
        //默认给被推荐人1%，如果商品没设置返利
        BigDecimal invitedUserPrice = goods.invitedUserPrice == null || goods.invitedUserPrice.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : goods.invitedUserPrice;
        return goods.salePrice.multiply(invitedUserPrice).multiply(new BigDecimal(number)).multiply(new BigDecimal(0.01));
    }

    public User getUserInfo() {
        User user = this.getUser();
        return user;
    }
}
