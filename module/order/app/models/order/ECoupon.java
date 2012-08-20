package models.order;

import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.RandomNumberUtil;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.admin.SupplierUser;
import models.sales.Goods;
import models.sales.Shop;
import models.sms.SMSUtil;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ModelPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "e_coupon")
public class ECoupon extends Model {

    private static final long serialVersionUID = 16993203113062L;

    public static final String TIME_FORMAT = "HH:mm:ss";
    private static final String COUPON_EXPIRE_FORMAT = "yyyy-MM-dd";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @ManyToOne
    public Goods goods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = true)
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

    @Column(name = "refund_price")
    public BigDecimal refundPrice;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "effective_at")
    public Date effectiveAt;

    @Column(name = "expire_at")
    public Date expireAt;

    @Column(name = "consumed_at")
    public Date consumedAt;

    @Column(name = "refund_at")
    public Date refundAt;

    @Enumerated(EnumType.STRING)
    public ECouponStatus status;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "is_freeze")
    /** 1：冻结 0：解冻*/
    public int isFreeze;

    @Column(name = "download_times")
    public Integer downloadTimes;
    @Enumerated(EnumType.STRING)
    public VerifyCouponType verifyType;

    @Column(name = "verify_tel")
    public String verifyTel;
    /**
     * 用于短信回复的code，将会成为消费者看到的发送手机号的最后4位。
     * <p/>
     * 将会是4位，而且在同一个消费者所有未消费的券号中不重复.
     */
    @Column(name = "reply_code")
    public String replyCode;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = true)
    public Shop shop;

    @Transient
    public String shopName;
    @Transient
    public String clerkInfo;
    @Transient
    public String verifyName;
    @Transient
    public String statusInfo;
    /**
     * 验证店员
     */
    @ManyToOne
    @JoinColumn(name = "supplier_user_id", nullable = true)
    public SupplierUser supplierUser;
    /**
     * 代理验证ID
     */
    @Column(name = "operate_user_id")
    public Long operateUserId;
    @Transient
    public String operateUserName;

    public ECoupon(Order order, Goods goods, OrderItems orderItems) {
        this.order = order;
        this.goods = goods;

        this.faceValue = orderItems.faceValue;
        this.originalPrice = orderItems.originalPrice;
        this.resalerPrice = orderItems.resalerPrice;
        this.salePrice = ((orderItems.rebateValue == null) ? orderItems.salePrice : orderItems.salePrice.subtract(orderItems.rebateValue));

        this.createdAt = new Date();
        this.effectiveAt = goods.effectiveAt;
        this.expireAt = goods.expireAt;

        this.consumedAt = null;
        this.refundAt = null;
        this.refundPrice = new BigDecimal(0);
        this.status = ECouponStatus.UNCONSUMED;
        this.eCouponSn = generateAvailableEcouponSn();
        this.orderItems = orderItems;
        this.downloadTimes = 3;
        this.isFreeze = 0;
        this.lockVersion = 0;
        this.replyCode = generateAvailableReplayCode(order.userId, order.userType);
    }


    /**
     * 生成当前用户唯一的ReplyCode，用于发送短信.
     *
     * @param userId
     * @param userType
     * @return
     */
    private String generateAvailableReplayCode(long userId, AccountType userType) {
        String randomNumber;
        do {
            randomNumber = RandomNumberUtil.generateSerialNumber(4);
        } while (isNotUniqueReplyCode(randomNumber, userId, userType));
        return randomNumber;
    }

    /**
     * 生成消费者唯一的券号.
     */
    private String generateAvailableEcouponSn() {
        String randomNumber;
        do {
            randomNumber = RandomNumberUtil.generateSerialNumber(10);
        } while (isNotUniqueEcouponSn(randomNumber));
        return randomNumber;
    }


    private boolean isNotUniqueReplyCode(String randomNumber, long userId, AccountType userType) {
        if ("0000".equals(randomNumber)) {
            return true;
        }
        return ECoupon.find("from ECoupon where replyCode=? and order.userId=? and order.userType=?",
                randomNumber, userId, userType).fetch().size() > 0;
    }

    private boolean isNotUniqueEcouponSn(String randomNumber) {
        return ECoupon.find("from ECoupon where eCouponSn=?", randomNumber).fetch().size() > 0;
    }

    /**
     * 生成当前用户相对于商户的ReplyCode，用于发送短信.
     * 即用户在一个商户购买的所有商品都在一个短信下
     * 暂时不使用。。。。
     *
     * @param userId
     * @param userType
     * @return
     */
    private String generateAvailableReplayCode(long userId, AccountType userType, long supplierId) {
        ECoupon ecoupon = getLastECoupon(userId, userType, supplierId);
        if (ecoupon != null) {
            return ecoupon.replyCode;
        }

        // 似乎不需要锁 ~ by TangLiqun
        synchronized (ECoupon.class) {
            ecoupon = getLastECoupon(userId, userType, supplierId);
            if (ecoupon != null) {
                return ecoupon.replyCode;
            }
            return RandomNumberUtil.generateSerialNumber(4);
        }
    }


    private ECoupon getLastECoupon(long userId, AccountType userType, long supplierId) {
        return ECoupon.find("from ECoupon where order.userId=? and order.userType=? and goods.supplierId=?",
                userId, userType, supplierId).first();
    }


    /**
     * 根据页面录入券号查询对应信息
     *
     * @param eCouponSn  券号
     * @param supplierId 商户ID
     * @return ECoupon 券信息
     */
    public static ECoupon query(String eCouponSn, Long supplierId) {
        EntityManager entityManager = play.db.jpa.JPA.em();
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        sql.append("select distinct e from ECoupon e where 1=1 ");
        if (supplierId != null) {
            sql.append(" and e.goods.supplierId = :supplierId");
            params.put("supplierId", supplierId);
        }
        if (StringUtils.isNotBlank(eCouponSn)) {
            sql.append(" and e.eCouponSn = :eCouponSn");
            params.put("eCouponSn", eCouponSn);
        }
        Query couponQuery = entityManager.createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            couponQuery.setParameter(entry.getKey(), entry.getValue());
        }
        sql.append(" order by e.createdAt desc");

        List<ECoupon> queryList = couponQuery.getResultList();
        if (queryList.size() == 0) {
            return null;
        }

        return queryList.get(0);
    }


    /**
     * 是否属于所在消费门店
     *
     * @param shopId 门店ID
     * @return
     */
    public boolean isBelongShop(Long shopId) {
        //判断该券是否属于所在消费门店
        if (!this.goods.isAllShop) {
            for (Shop shop : this.goods.shops) {
                if (shop.id.compareTo(shopId) == 0) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void consumeAndPayCommission(Long shopId, Long operateUserId, SupplierUser supplierUser, VerifyCouponType type) {
        consumed(shopId, operateUserId, supplierUser, type);
        payCommission();
    }

    /**
     * 优惠券被消费。
     * 修改优惠券状态、发佣金、给商户打钱
     *
     * @return
     */
    private void consumed(Long shopId, Long operateUserId, SupplierUser supplierUser, VerifyCouponType type) {
        if (this.status != ECouponStatus.UNCONSUMED) {
            return;
        }

        if (shopId != null) {
            this.shop = Shop.findById(shopId);
        }
        this.status = ECouponStatus.CONSUMED;
        this.consumedAt = new Date();
        this.supplierUser = supplierUser != null ? supplierUser : null;
        this.operateUserId = operateUserId != null ? operateUserId : null;
        this.verifyType = type;
        this.save();
    }

    public void payCommission() {
        Account supplierAccount = AccountUtil.getSupplierAccount(orderItems.goods.supplierId);

        //给商户打钱
        TradeBill consumeTrade = TradeUtil.createConsumeTrade(eCouponSn, supplierAccount, originalPrice, order.getId());
        TradeUtil.success(consumeTrade, "券消费(" + order.description + ")");

        BigDecimal platformCommission = BigDecimal.ZERO;
        if (salePrice.compareTo(resalerPrice) < 0) {
            //如果成交价小于分销商成本价（这种情况只有在一百券网站上才会发生），
            //那么一百券就没有佣金，平台的佣金也变为成交价减成本价
            platformCommission = salePrice.subtract(originalPrice);
        } else {
            //平台的佣金等于分销商成本价减成本价
            platformCommission = resalerPrice.subtract(originalPrice);
            //如果是在一百券网站下的单，还要给一百券佣金
            if (order.userType == AccountType.CONSUMER) {
                TradeBill uhuilaCommissionTrade = TradeUtil.createCommissionTrade(
                        AccountUtil.getUhuilaAccount(),
                        salePrice.subtract(resalerPrice),
                        eCouponSn,
                        order.getId());

                TradeUtil.success(uhuilaCommissionTrade, order.description);
            }
        }
        if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
            //给优惠券平台佣金
            TradeBill platformCommissionTrade = TradeUtil.createCommissionTrade(
                    AccountUtil.getPlatformCommissionAccount(),
                    platformCommission,
                    eCouponSn,
                    order.getId());
            TradeUtil.success(platformCommissionTrade, order.description);
        }
    }

    /**
     * 商家券号列表
     *
     * @param supplierId 商户ID
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return ordersPage 列表信息
     */
    public static ModelPaginator<ECoupon> queryCoupons(Long supplierId, int pageNumber, int pageSize) {
        ModelPaginator ordersPage;
        if (supplierId != null) {
            ordersPage = new ModelPaginator(ECoupon.class, "goods.supplierId = ?", supplierId).orderBy("createdAt desc");
        } else {
            ordersPage = new ModelPaginator(ECoupon.class).orderBy("createdAt desc");
        }

        ordersPage.setPageNumber(pageNumber);
        ordersPage.setPageSize(pageSize);
        ordersPage.setBoundaryControlsEnabled(true);
        return ordersPage;
    }

    /**
     * 券号列表
     *
     * @param condition  条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return couponsPage 券记录
     */
    public static JPAExtPaginator<ECoupon> query(CouponsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>
                ("ECoupon e", "e", ECoupon.class, condition.getFilter(),
                        condition.getParamMap()).orderBy("e.consumedAt desc,e.createdAt desc");

        couponsPage.setPageNumber(pageNumber);
        couponsPage.setPageSize(pageSize);
        return couponsPage;
    }

    /**
     * 会员中心 券号列表
     *
     * @param condition  条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return couponsPage 券记录
     */
    public static JPAExtPaginator<ECoupon> getUserCoupons(CouponsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>
                ("ECoupon e", "e", ECoupon.class, condition.getFilter(),
                        condition.getParamMap()).orderBy("e.createdAt desc");

        couponsPage.setPageNumber(pageNumber);
        couponsPage.setPageSize(pageSize);
        return couponsPage;
    }

    /**
     * 退款
     *
     * @param eCoupon 券信息
     * @param userId  用户信息
     * @return
     */
    public static String applyRefund(ECoupon eCoupon, Long userId, AccountType accountType) {
        String returnFlg = "{\"error\":\"ok\"}";

        if (eCoupon == null || eCoupon.order.userId != userId || eCoupon.order.userType != accountType) {
            returnFlg = "{\"error\":\"no such eCoupon\"}";
            return returnFlg;
        }

        if (eCoupon.status == ECouponStatus.CONSUMED || eCoupon.status == ECouponStatus.REFUND) {
            returnFlg = "{\"error\":\"can not apply refund with this goods\"}";
            return returnFlg;
        }
        //查找原订单信息

        Account account = AccountUtil.getAccount(userId, accountType);
        //计算需要退款的活动金金额
        //计算方法：本订单中，抛开已消费的和已经退款过的活动金，先退活动金
        //例如，订单金额100，用活动金支付40，用余额支付60，若此时退款，则首先退到活动金中。
        //如果已经消费了20， 那仍然首先退到活动金，但是最多退40-20=20元，也就是说，视用户消费时首先消费的是活动金
        BigDecimal cashAmount = eCoupon.salePrice;
        BigDecimal promotionAmount = BigDecimal.ZERO;
        BigDecimal consumedAmount = BigDecimal.ZERO;
        if (eCoupon.order.refundedPromotionAmount == null) {
            eCoupon.order.refundedPromotionAmount = BigDecimal.ZERO;
        }
        List<ECoupon> eCoupons = ECoupon.find("byOrderAndStatus", eCoupon.order, ECouponStatus.CONSUMED).fetch();
        for(ECoupon c : eCoupons){
            consumedAmount = consumedAmount.add(eCoupon.salePrice);
        }
        BigDecimal usedPromotionAmount = eCoupon.order.refundedPromotionAmount.add(consumedAmount);
        if(eCoupon.order.promotionBalancePay != null && eCoupon.order.promotionBalancePay.compareTo(usedPromotionAmount) > 0) {
            promotionAmount = cashAmount.min(eCoupon.order.promotionBalancePay.subtract(usedPromotionAmount));
            cashAmount = cashAmount.subtract(promotionAmount);
        }

        //创建退款交易
        TradeBill tradeBill = TradeUtil.createRefundTrade(account, cashAmount, promotionAmount, eCoupon.order.getId(), eCoupon.eCouponSn);

        if (!TradeUtil.success(tradeBill, "退款成功.券号:" + eCoupon.getMaskedEcouponSn() + ",商品:" + eCoupon.goods.name)) {
            returnFlg = "{\"error\":\"refound failed\"}";
            return returnFlg;
        }

        //更新已退款的活动金金额
        if (promotionAmount.compareTo(BigDecimal.ZERO) > 0) {
            eCoupon.order.refundedPromotionAmount = eCoupon.order.refundedPromotionAmount.add(promotionAmount);
            eCoupon.order.save();
        }

        //更改库存
        eCoupon.goods.baseSale += 1;
        eCoupon.goods.saleCount -= 1;
        eCoupon.goods.save();

        //更改订单状态
        eCoupon.status = ECouponStatus.REFUND;
        eCoupon.refundAt = new Date();
        eCoupon.refundPrice = eCoupon.salePrice;
        eCoupon.save();

        return returnFlg;
    }

    /**
     * 得到隐藏处理过的券号
     *
     * @return 券号
     */
    public String getMaskedEcouponSn() {
        StringBuilder sn = new StringBuilder();
        int len = eCouponSn.length();
        if (len > 4) {
            for (int i = 0; i < len - 4; i++) {
                sn.append("*");
            }
            sn.append(eCouponSn.substring(len - 4, len));
        }
        return sn.toString();
    }

    /**
     * 获取后n位券号.
     *
     * @param count
     * @return
     */
    public String getLastCode(int count) {
        return eCouponSn.substring(eCouponSn.length() - count);
    }

    public static List<ECoupon> findByOrder(Order order) {
        return ECoupon.find("byOrder", order).fetch();
    }

    public static List<ECoupon> findByUserAndIds(List<Long> ids, Long userId, AccountType accountType) {
        String sql = "select e from ECoupon e where e.id in :ids and e.order.userId = :userId and e.order.userType = :userType";
        Query query = ECoupon.em().createQuery(sql);
        query.setParameter("ids", ids);
        query.setParameter("userId", userId);
        query.setParameter("userType", accountType);
        return query.getResultList();
    }

    /**
     * 得到可以消费的门店
     *
     * @return 券号
     */
    public String getConsumedShop() {
        String shopName = "";
        Shop sp;
        if (shop != null && shop.id != null) {
            sp = Shop.findById(shop.id);
            shopName = sp.name;
        }
        return shopName;
    }

    /**
     * 判断当日的时间是否在指定时间范围内
     *
     * @return 在该范围内：true
     */
    public boolean getTimeRegion(String timeBegin, String timeEnd) {
        boolean timeFlag = false;
        if (StringUtils.isNotBlank(timeBegin) && StringUtils.isNotBlank(timeEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            String date = dateFormat.format(calendar.getTime());
            if (date.compareTo(timeBegin) > 0 && date.compareTo(timeEnd) < 0) {
                timeFlag = true;
            }
        } else {//没设定消费时间的场合
            timeFlag = true;
        }

        return timeFlag;
    }

    /**
     * 按手机号及replyCode查出可用的ECoupon
     *
     * @param mobile    手机号
     * @param replyCode 返回码
     * @return 电子券
     */
    public static ECoupon findByMobileAndCode(String mobile, String replyCode) {
        return ECoupon.find("from ECoupon where orderItems.phone=? and replyCode=?", mobile, replyCode).first();
    }

    /**
     * 冻结此券
     *
     * @param id
     */
    public static void freeze(long id) {
        update(id, 1);
    }

    /**
     * 解冻此券
     *
     * @param id
     */
    public static void unfreeze(long id) {
        update(id, 0);
    }

    /**
     * 更新券是否冻结
     */
    private static void update(long id, Integer isFreeze) {
        ECoupon eCoupon = ECoupon.findById(id);
        eCoupon.isFreeze = isFreeze;
        eCoupon.save();
    }

    /**
     * 发送短信
     */
    private static void send(ECoupon eCoupon) {
//        SMSUtil.send(eCoupon.goods.name + "券号:" + eCoupon.eCouponSn, eCoupon.orderItems.phone, eCoupon.replyCode);
        SimpleDateFormat dateFormat = new SimpleDateFormat(COUPON_EXPIRE_FORMAT);
        SMSUtil.send("【券市场】" + (StringUtils.isNotEmpty(eCoupon.goods.title) ? eCoupon.goods.title : (eCoupon.goods.name +
                "[" + eCoupon.goods.faceValue + "元]")) + "券号" + eCoupon.eCouponSn + "," +
                "截止" + dateFormat.format(eCoupon.expireAt) + ",客服：4006262166",
                eCoupon.orderItems.phone, eCoupon.replyCode);
    }

    /**
     * 运营后台发送短信
     *
     * @param id
     * @return
     */
    public static boolean sendMessage(long id) {
        ECoupon eCoupon = ECoupon.findById(id);
        boolean sendFalg = false;
        if (eCoupon != null && eCoupon.status == ECouponStatus.UNCONSUMED) {
            send(eCoupon);
            sendFalg = true;
        }
        return sendFalg;
    }

    /**
     * 会员中心发送短信
     *
     * @param id
     * @return
     */
    public static boolean sendUserMessage(long id) {
        ECoupon eCoupon = ECoupon.findById(id);
        boolean sendFlag = false;
        if (eCoupon != null && eCoupon.status == ECouponStatus.UNCONSUMED && eCoupon.downloadTimes > 0 && eCoupon
                .downloadTimes < 4) {
            send(eCoupon);
            eCoupon.downloadTimes--;
            eCoupon.save();
            sendFlag = true;
        }
        return sendFlag;
    }

    /**
     * 返回券的总金额.
     *
     * @param condition 券查询条件
     * @return
     */
    public static BigDecimal sum(CouponsCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select sum(e.originalPrice) " +
                "from ECoupon e where " + condition.getFilter());
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }
        return (BigDecimal) q.getSingleResult();
    }

    public static List<ECoupon> selectCheckECoupons(BigDecimal payValue,
                                                    List<ECoupon> ecoupons) {
        Collections.sort(ecoupons, new Comparator<ECoupon>() {
            @Override
            public int compare(ECoupon e1, ECoupon e2) {
                return e2.faceValue.compareTo(e1.faceValue);
            }
        });
        BigDecimal totalValue = new BigDecimal(0);
        List<ECoupon> selectECoupons = new ArrayList<>();

        for (ECoupon ecoupon : ecoupons) {
            if (totalValue.add(ecoupon.faceValue).compareTo(payValue) > 0) {
                continue;
            } else {
                totalValue = totalValue.add(ecoupon.faceValue);
                selectECoupons.add(ecoupon);
            }
        }

        return selectECoupons;
    }

    /**
     * 统计券金额
     *
     * @param couponPage
     * @return
     */
    public static BigDecimal summary(JPAExtPaginator<ECoupon> couponPage) {
        BigDecimal amount = BigDecimal.ZERO;

        for (ECoupon coupon : couponPage) {
            if (coupon.status == ECouponStatus.REFUND) {
                amount = amount.add(coupon.refundPrice == null ? BigDecimal.ZERO : coupon.refundPrice);
            } else {
                amount = amount.add(coupon.salePrice);
            }
        }
        return amount;
    }

    /**
     * 取得过期的天数
     *
     * @return
     */
    public Long getExpiredAt() {
        return DateUtil.diffDay(this.expireAt);
    }
}
