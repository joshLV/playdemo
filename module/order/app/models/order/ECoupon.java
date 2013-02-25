package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.RandomNumberUtil;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.operator.OperateUser;
import models.admin.SupplierUser;
import models.consumer.User;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.jingdong.groupbuy.JDGroupBuyHelper;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.GoodsCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.taobao.TaobaoCouponUtil;
import models.tsingtuan.TsingTuanOrder;
import models.tsingtuan.TsingTuanSendOrder;
import models.wuba.WubaUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ModelPaginator;
import play.modules.solr.Solr;
import util.common.InfoUtil;
import util.transaction.RemoteCallback;
import util.transaction.RemoteRecallCheck;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "e_coupon")
public class ECoupon extends Model {

    public static final String KEY_USE_PRODUCT_SERIAL_REPLYCODE = "ecoupon.use_product_serial_replycode";

    private static final long serialVersionUID = 16993203113062L;

    public static final String TIME_FORMAT = "HH:mm:ss";
    private static final String COUPON_EXPIRE_FORMAT = "yyyy-MM-dd";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    public static boolean USE_PRODUCT_SERIAL_REPLYCODE;
    public static final String ECOUPON_REFUND_OK = "{\"error\":\"ok\"}";

    static {
        String useProductSerialReplyCode = Play.configuration.getProperty(KEY_USE_PRODUCT_SERIAL_REPLYCODE, "true");
        USE_PRODUCT_SERIAL_REPLYCODE = Boolean.parseBoolean(useProductSerialReplyCode);
    }

    @ManyToOne
    public Goods goods;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_coupons_id", nullable = true)
    public BatchCoupons batchCoupons;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = true)
    public OrderItems orderItems;

    @Required
    @Column(name = "e_coupon_sn")
    public String eCouponSn;

    /**
     * 导入券的密码
     */
    @Column(name = "e_coupon_password")
    public String eCouponPassword;

    @Column(name = "is_cheated_order")
    public Boolean isCheatedOrder = false;

    @Column(name = "other_reason")
    public String otherReason;

    /**
     * 冻结单张券号时的选项,
     */
    @Enumerated(EnumType.STRING)
    public ECouponFreezedReason freezedReason;

    /**
     * 部分第三方团购可能使用密码，而券市场的券不需要。
     * 记录一下
     */
    public String password;

    /**
     * 已同步标记。
     * 如果为true，则已经同步到第三方网站
     */
    public Boolean synced;

    // ==== 价格列表 ====
    @Column(name = "face_value")
    public BigDecimal faceValue; // 商品面值、市场价

    @Column(name = "original_price")
    public BigDecimal originalPrice; // 供应商进货价

    @Column(name = "resaler_price")
    public BigDecimal resalerPrice; // 用户在哪个分销商平台购买的价格，用于计算分销平台的佣金

    @Column(name = "sale_price")
    public BigDecimal salePrice; // 最终成交价,对于普通分销商来说，此成交价与以上分销商价(resalerPrice)相同；

    /**
     * 折扣掉的费用.
     */
    @Column(name = "rebate_value")
    public BigDecimal rebateValue;
    /**
     * 给推荐人返利费用.
     */
    @Column(name = "promoter_rebate_value")
    public BigDecimal promoterRebateValue;
    // ==== 价格列表 ====

    @Column(name = "create_type")
    @Enumerated(EnumType.STRING)
    public ECouponCreateType createType;

    @Column(name = "auto_consumed")
    public DeletedStatus autoConsumed;//第三方卖的导入券要自动给消费掉

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
    public Integer isFreeze;


    /**
     * 发送过短信的次数.
     */
    @Column(name = "sms_sent_count")
    public Integer smsSentCount;

    /**
     * 这个是用来做什么的？多张券验证时使用？(add by likang)
     * TODO: 感觉可以去掉。  - tangliqun
     */
    @Column(name = "trigger_coupon_sn")
    public String triggerCouponSn;

    /**
     * 不再使用，使用smsSendCount代替.
     */
    @Column(name = "download_times")
    public Integer downloadTimes;

    @Enumerated(EnumType.STRING)
    public VerifyCouponType verifyType;

    @Enumerated(EnumType.STRING)
    public ECouponPartner partner;

    /**
     * 合作方券ID。
     * 如果是第三方发券，记录在这里备查。
     */
    @Column(name = "partner_coupon_id")
    public String partnerCouponId;

    /**
     * 合作方券密码。
     * 如果是第三方发券并且需要密码，记录在这里.
     */
    @Column(name = "partner_coupon_pwd")
    public String partnerCouponPwd;

    /**
     * 消费者预约日期。
     */
    @Column(name = "appointment_date")
    public Date appointmentDate;

    /**
     * 消费者预约备注。
     * 此备注信息将发送此消息者手机.
     */
    @Column(name = "appointment_remark")
    public String appointmentRemark;

    @Transient
    public String verifyTypeInfo;

    @Transient
    public String staff;

    @Transient
    public String refundPriceInfo;

    /**
     * @return
     */
    @Transient
    public boolean isExpired() {
        return expireAt != null && expireAt.before(new Date());
    }

    @Column(name = "verify_tel")
    public String verifyTel;
    /**
     * 用于短信回复的code，将会成为消费者看到的发送手机号的最后4位。
     * <p/>
     * 将会是4位，而且在同一个消费者所有未消费的券号中不重复.
     */
    @Column(name = "reply_code")
    public String replyCode;

    /**
     * 第三方（JD，WB）虚拟验证标志和虚拟验证时间（即财务对帐时间）
     */
    @Column(name = "virtual_verify")
    public Boolean virtualVerify = Boolean.FALSE;
    @Column(name = "virtual_verify_at")
    public Date virtualVerifyAt;


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
        this(order, goods, orderItems, null, "");
    }

    public ECoupon(Order order, Goods goods, OrderItems orderItems, String couponSn, String password) {
        this.order = order;
        this.batchCoupons = batchCoupons;

        this.goods = goods;

        this.faceValue = orderItems.faceValue;
        this.originalPrice = orderItems.originalPrice;
        this.resalerPrice = orderItems.resalerPrice;
        this.salePrice = orderItems.salePrice;
        if (order.promoteUserId != null) {
            //记录给推荐人的返利，每个券占用的返利
            this.promoterRebateValue = getLinePromoterRebateValue();
        }
        this.rebateValue = orderItems.rebateValue;
        this.createdAt = new Date();
        this.effectiveAt = goods.effectiveAt;
        this.expireAt = goods.expireAt;
        this.consumedAt = null;
        this.refundAt = null;
        this.refundPrice = BigDecimal.ZERO;
        this.status = ECouponStatus.UNCONSUMED;
        if (couponSn == null) {
            this.eCouponSn = generateAvailableEcouponSn();
            this.createType = ECouponCreateType.GENERATE;
        } else {
            if (StringUtils.isNotBlank(password)) {
                this.eCouponSn = couponSn;
                this.eCouponPassword = password;
            } else {
                this.eCouponSn = couponSn;
            }
            this.createType = ECouponCreateType.IMPORT;
        }
        this.orderItems = orderItems;
        this.smsSentCount = 0;
        this.isFreeze = 0;
        this.lockVersion = 0;
        this.autoConsumed = DeletedStatus.UN_DELETED;

        if (USE_PRODUCT_SERIAL_REPLYCODE) {
            this.replyCode = generateAvailableReplayCode(order.userId, order.userType, goods);
        } else {
            this.replyCode = generateAvailableReplayCode(order.userId, order.userType);
        }
    }


    /**
     * 券修改时更新库存。
     */
    @Override
    public void _save() {
        this.goods.refreshSaleCount();
        super._save();
    }


    private BigDecimal getLineRebateValue() {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal invitedRebatePrice;
        for (OrderItems item : order.orderItems) {
            //如果商品没设置返利,默认给推荐人1%
            invitedRebatePrice = item.goods.invitedUserPrice == null || item.goods.invitedUserPrice.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : item.goods.invitedUserPrice;
            amount = amount.add(item.goods.salePrice.multiply(invitedRebatePrice).multiply(new BigDecimal(0.01)));
        }

        return amount;
    }

    private BigDecimal getLinePromoterRebateValue() {
        //如果商品没设置返利,默认给推荐人2%
        BigDecimal promoterPrice = this.goods.promoterPrice == null ? new BigDecimal(2) : this.goods.promoterPrice;
        return this.goods.salePrice.multiply(promoterPrice).multiply(new BigDecimal(0.01));
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
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // do nothing.
            }
            randomNumber = RandomNumberUtil.generateSerialNumber(10);
        } while (isNotUniqueEcouponSn(randomNumber));
        return randomNumber;
    }

    private boolean isNotUniqueReplyCode(String randomNumber, long userId, AccountType userType) {
        if ("0000".equals(randomNumber)) {
            return true;
        }
        return ECoupon.find("from ECoupon where replyCode=? and order.userId=? and order.userType=?", randomNumber, userId, userType).fetch().size() > 0;
    }

    private boolean isNotUniqueEcouponSn(String randomNumber) {
        return ECoupon.find("from ECoupon where eCouponSn=?", randomNumber).fetch().size() > 0;
    }

    /**
     * 生成当前用户相对于商户某一系列商品的ReplyCode，用于发送短信. 即用户在一个商户购买的同一系列商品都在一个短信下.
     *
     * @param userId
     * @param userType
     * @param goods    商品，通过供应商和系列号分配，如果无系列号，使用商品ID，这样同一个商品会使用相同的replyCode
     * @return
     */
    private String generateAvailableReplayCode(long userId, AccountType userType, Goods goods) {
        ECoupon ecoupon = getLastECoupon(userId, userType, goods);
        if (ecoupon != null) {
            return ecoupon.replyCode;
        }

        // 似乎不需要锁 ~ by TangLiqun
        synchronized (ECoupon.class) {
            ecoupon = getLastECoupon(userId, userType, goods);
            if (ecoupon != null) {
                return ecoupon.replyCode;
            }
            return generateAvailableReplayCode(userId, userType);
        }
    }

    private ECoupon getLastECoupon(long userId, AccountType userType, Goods goods) {
        if (goods.groupCode != null) {
            return ECoupon.find("from ECoupon where order.userId=? and order.userType=? and goods.groupCode=?", userId, userType, goods.groupCode).first();
        }
        return ECoupon.find("from ECoupon where order.userId=? and order.userType=? and goods.id=?", userId, userType, goods.id).first();
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
            params.put("eCouponSn", eCouponSn.trim());
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
        // 判断该券是否属于所在消费门店
        if (this.goods.isAllShop) {
            return true;
        }
        for (Shop shop : this.goods.shops) {
            if (shop.id.compareTo(shopId) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeAndPayCommission(Long shopId, SupplierUser supplierUser, VerifyCouponType type) {
        return consumeAndPayCommission(shopId, supplierUser, type, this.eCouponSn);
    }

    public boolean consumeAndPayCommission(Long shopId, SupplierUser supplierUser, VerifyCouponType type,
                                           String triggerCouponSn) {
        return consumeAndPayCommission(shopId, null, supplierUser, type, triggerCouponSn, null, null);
    }

    public boolean consumeAndPayCommission(Long shopId, OperateUser operateUser, SupplierUser supplierUser, VerifyCouponType type,
                                           String triggerCouponSn, Date consumedAt, String remark) {

        //===================判断是否第三方订单产生的券=并且不是导入券============================
        if (this.createType != ECouponCreateType.IMPORT) {
            if (verifyAndCheckOnPartnerResaler()) return false;
        }
        //===================券消费处理开始=====================================
        if (consumed(shopId, operateUser, supplierUser, type, consumedAt, remark)) {
            payCommission();
            this.triggerCouponSn = triggerCouponSn; //记录批量验证时所使用的券号
            this.save();
        }

        return true;

    }

    /**
     * 使用RemoteRecallCheck.call包装一下，这样在下次进来时会检查是否成功过，如果成功过就不再调用verifyOnPartnerResaler.
     * @return 如果返回true，表示调用失败.
     */
    private Boolean verifyAndCheckOnPartnerResaler() {
        return RemoteRecallCheck.call("verify_coupon", new RemoteCallback<Boolean>() {
            @Override
            public Boolean doCall() {
                Boolean result = verifyOnPartnerResaler();
                // 需要重试.
                RemoteRecallCheck.setNeedRecall(result);
                return result;
            }
        });
    }

    /**
     * 调用第三方渠道券验证，并返回是否失败的标识。
     * @return TRUE表示验证失败；FALSE表示验证成功
     */
    private Boolean verifyOnPartnerResaler() {
        if (this.partner == ECouponPartner.DD) {
            if (!DDGroupBuyUtil.verifyOnDangdang(this)) {
                Logger.info("verify on dangdang failed");
                return true;
            }
        }

        if (this.partner == ECouponPartner.JD) {
            if (!JDGroupBuyHelper.verifyOnJingdong(this)) {
                Logger.info("verify on jingdong failed");
                return true;
            }
        } else if (this.partner == ECouponPartner.WB) {
            if (!WubaUtil.verifyOnWuba(this)) {
                Logger.info("verify on wuba failed");
                return true;
            }
        } else if (this.partner == ECouponPartner.TB) {
            if (!TaobaoCouponUtil.verifyOnTaobao(this)) {
                Logger.info("verify on taobao failed");
                return true;
            }
        }
        return false;
    }

    /**
     * 优惠券被消费。 修改优惠券状态、发佣金、给商户打钱
     *
     * @return
     */
    private boolean consumed(Long shopId, OperateUser operateUser, SupplierUser supplierUser, VerifyCouponType type,
                             Date consumedAt, String remark) {
        if (this.status != ECouponStatus.UNCONSUMED) {
            return false;
        }
        if (shopId != null) {
            this.shop = Shop.findById(shopId);
        }
        this.status = ECouponStatus.CONSUMED;
        this.consumedAt = consumedAt == null ? new Date() : consumedAt;
        String operator = "";
        if (supplierUser != null) {
            this.supplierUser = supplierUser;
            operator = supplierUser.loginName;
        }
        else if (operateUser != null) {
            this.operateUserId = operateUser.id;
            operator = "运营人员:" + operateUser.userName;
        }
        this.verifyType = type;
        this.save();
        if (this.order != null && this.order.promoteUserId != null) {
            User invitedUser = User.findById(this.order.userId);
            PromoteRebate promoteRebate = PromoteRebate.find("invitedUser=? and order =?", invitedUser, this.order).first();
            //消费的时候更新返利表已经返利的金额字段
            promoteRebate.partAmount = promoteRebate.partAmount.add(promoterRebateValue);
            if (promoteRebate.rebateAmount.compareTo(promoteRebate.partAmount) > 0) {
                promoteRebate.status = RebateStatus.PART_REBATE;
            } else {
                promoteRebate.status = RebateStatus.ALREADY_REBATE;
            }

            promoteRebate.rebateAt = new Date();
            promoteRebate.save();
        }
        //记录券历史信息
        String historyRemark = StringUtils.isBlank(remark) ? "消费" : remark;
        ECouponHistoryMessage.with(this).operator(operator).remark(historyRemark)
                .fromStatus(this.status).toStatus(ECouponStatus.CONSUMED).sendToMQ();
        return true;
    }

    public void payCommission() {
        Account supplierAccount = AccountUtil.getSupplierAccount(orderItems.goods.supplierId);

        // 给商户打钱
        TradeBill consumeTrade = TradeUtil.createConsumeTrade(eCouponSn, supplierAccount, originalPrice, order.getId());
        TradeUtil.success(consumeTrade, "券消费(" + order.description + ")");

        BigDecimal platformCommission = BigDecimal.ZERO;

        if (salePrice.compareTo(resalerPrice) < 0) {
            // 如果成交价小于分销商成本价（这种情况只有在一百券网站上才会发生），
            // 那么一百券就没有佣金，平台的佣金也变为成交价减成本价
            platformCommission = salePrice.subtract(originalPrice);
        } else {
            // 平台的佣金等于分销商成本价减成本价
            platformCommission = resalerPrice.subtract(originalPrice);
            // 如果是在一百券网站下的单，还要给一百券佣金
            if (order.userType == AccountType.CONSUMER) {
                TradeBill uhuilaCommissionTrade = TradeUtil.createCommissionTrade(AccountUtil.getUhuilaAccount(), salePrice.subtract(resalerPrice), eCouponSn, order.getId());

                TradeUtil.success(uhuilaCommissionTrade, order.description);
            }
        }

        if (platformCommission.compareTo(BigDecimal.ZERO) >= 0) {
            // 给优惠券平台佣金
            TradeBill platformCommissionTrade = TradeUtil.createCommissionTrade(AccountUtil.getPlatformCommissionAccount(), platformCommission, eCouponSn, order.getId());
            TradeUtil.success(platformCommissionTrade, order.description);
        }

        if (rebateValue != null && rebateValue.compareTo(BigDecimal.ZERO) > 0) {
            TradeBill rabateTrade = TradeUtil.createTransferTrade(AccountUtil.getUhuilaAccount(), AccountUtil.getPlatformIncomingAccount(), rebateValue, BigDecimal.ZERO);
            rabateTrade.orderId = this.order.id;
            TradeUtil.success(rabateTrade, "活动折扣费" + rebateValue);
        } else if (salePrice.compareTo(originalPrice) < 0) {
            BigDecimal detaPrice = originalPrice.subtract(salePrice);
            // 如果售价低于进价，从活动金账户出
            TradeBill rabateTrade = TradeUtil.createTransferTrade(AccountUtil.getPromotionAccount(), AccountUtil.getPlatformIncomingAccount(), detaPrice, BigDecimal.ZERO);
            rabateTrade.orderId = this.order.id;
            TradeUtil.success(rabateTrade, "低价销售补贴" + detaPrice);
        }

        //给推荐人返利金额
        if (this.order != null && this.order.promoteUserId != null) {
            User promoteUser = User.findById(this.order.promoteUserId);
            User invitedUser = User.findById(this.order.userId);
            if (promoteUser == null || invitedUser == null) {
                throw new IllegalArgumentException("promoteUser or invitedUser is not existed");
            }

            PromoteRebate promoteRebate = PromoteRebate.find("invitedUser=? and order =?", invitedUser, this.order).first();
            if (promoteRebate != null) {
                Account account = AccountUtil.getConsumerAccount(promoteUser.getId());
                TradeBill rabateTrade = TradeUtil.createTransferTrade(AccountUtil.getUhuilaAccount(), account, promoterRebateValue, BigDecimal.ZERO);
                rabateTrade.orderId = this.order.id;
                TradeUtil.success(rabateTrade, "推荐获得的返利" + rebateValue);
            }

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
     * 商户后台v2
     *
     * @param condition  条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return couponsPage 券记录
     */
    public static JPAExtPaginator<ECoupon> findByCondition(CouponsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>("ECoupon e", "e", ECoupon.class, condition.getFilter(), condition.getParamMap()).orderBy("e.consumedAt desc,e.createdAt desc");

        couponsPage.setPageNumber(pageNumber);
        couponsPage.setPageSize(pageSize);
        return couponsPage;
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
        JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>("ECoupon e", "e", ECoupon.class, condition.getFilter(), condition.getParamMap()).orderBy("e.consumedAt desc,e.createdAt desc");

        couponsPage.setPageNumber(pageNumber);
        couponsPage.setPageSize(pageSize);
        return couponsPage;
    }

    public static JPAExtPaginator<ECoupon> queryBatchCoupons(BatchExportCouponsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>("ECoupon e", "e", ECoupon.class, condition.getFilter(), condition.getParamMap()).orderBy("e.consumedAt desc,e.createdAt desc");

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
        JPAExtPaginator<ECoupon> couponsPage = new JPAExtPaginator<>("ECoupon e", "e", ECoupon.class, condition.getFilter(), condition.getParamMap()).orderBy("e.createdAt desc");

        couponsPage.setPageNumber(pageNumber);
        couponsPage.setPageSize(pageSize);
        return couponsPage;
    }

    public static String applyRefund(ECoupon eCoupon, Long userId, AccountType accountType) {
        return applyRefund(eCoupon, userId, accountType, null, null);
    }

    /**
     * 退款
     *
     * @param eCoupon 券信息
     * @param userId  用户信息
     * @return
     */
    public static String applyRefund(ECoupon eCoupon, Long userId, AccountType accountType, String userName, String refundComment) {
        String returnFlg = ECOUPON_REFUND_OK;

        if (eCoupon == null || eCoupon.order.userId != userId || eCoupon.order.userType != accountType) {
            returnFlg = "{\"error\":\"no such eCoupon\"}";
            return returnFlg;
        }

        if (eCoupon.status == ECouponStatus.CONSUMED || eCoupon.status == ECouponStatus.REFUND) {
            returnFlg = "{\"error\":\"can not apply refund with this goods\"}";
            return returnFlg;
        }
        if (eCoupon.order.refundedAmount == null) {
            eCoupon.order.refundedAmount = BigDecimal.ZERO;
        }
        if (eCoupon.order.promotionBalancePay == null) {
            eCoupon.order.promotionBalancePay = BigDecimal.ZERO;
        }

        Account account = AccountUtil.getAccount(userId, accountType);

        /*
        System.out.println("===coupon.salePrice" + eCoupon.salePrice);
        System.out.println("===coupon.rebate" + eCoupon.rebateValue);
        System.out.println("===order.account" + eCoupon.order.accountPay);
        System.out.println("===order.disaccount" + eCoupon.order.discountPay);
        System.out.println("===order.promotion" + eCoupon.order.promotionBalancePay);
        */

        //先计算已消费的金额
        BigDecimal consumedAmount = BigDecimal.ZERO;
        List<ECoupon> eCoupons = ECoupon.find("byOrderAndStatus", eCoupon.order, ECouponStatus.CONSUMED).fetch();
        for (ECoupon c : eCoupons) {
            consumedAmount = consumedAmount.add(getLintRefundPrice(c));
        }
        //        System.out.println("===consumedAmount" + consumedAmount);

        //已消费的金额加上已退款的金额作为垫底
        BigDecimal onTheBottom = consumedAmount.add(eCoupon.order.refundedAmount);
        //        System.out.println("===onTheBottom" + onTheBottom);

        //再来看看去掉垫底的资金后，此订单还能退多少活动金和可提现余额
        BigDecimal refundOrderTotalCashAmount = eCoupon.order.accountPay.add(eCoupon.order.discountPay);
        BigDecimal refundOrderTotalPromotionAmount = eCoupon.order.promotionBalancePay;
        if (refundOrderTotalPromotionAmount.compareTo(onTheBottom) > 0) {
            //如果该订单的活动金大于垫底资金
            refundOrderTotalPromotionAmount = refundOrderTotalPromotionAmount.subtract(onTheBottom);
        } else {
            refundOrderTotalCashAmount = refundOrderTotalCashAmount.add(refundOrderTotalPromotionAmount).subtract(onTheBottom);
            refundOrderTotalPromotionAmount = BigDecimal.ZERO;
            if (refundOrderTotalCashAmount.compareTo(BigDecimal.ZERO) < 0) {
                refundOrderTotalCashAmount = BigDecimal.ZERO;
            }
        }
        //        System.out.println("===refundOrderTotalCashAmount" + refundOrderTotalCashAmount);
        //        System.out.println("===refundOrderTotalPromotionAmount" + refundOrderTotalPromotionAmount);

        //用户为此券实际支付的金额,也就是从用户为该券付的钱来看，最多能退多少
        BigDecimal refundAtMostCouponAmount = getLintRefundPrice(eCoupon);
        //        System.out.println("===refundAtMostCouponAmount" + refundAtMostCouponAmount);

        //最后我们来看看最终能退多少
        BigDecimal refundPromotionAmount = BigDecimal.ZERO;
        BigDecimal refundCashAmount = BigDecimal.ZERO;

        if (refundOrderTotalPromotionAmount.compareTo(refundAtMostCouponAmount) > 0) {
            refundPromotionAmount = refundOrderTotalPromotionAmount.subtract(refundAtMostCouponAmount);
        } else {
            refundPromotionAmount = refundOrderTotalPromotionAmount;
            refundCashAmount = refundAtMostCouponAmount.subtract(refundPromotionAmount);
            refundCashAmount = refundCashAmount.min(refundOrderTotalCashAmount);
        }
        //        System.out.println("===refundCashAmount" + refundCashAmount);
        //        System.out.println("===refundPromotionAmount" + refundPromotionAmount);


        // 创建退款交易
        TradeBill tradeBill = TradeUtil.createRefundTrade(account, refundCashAmount, refundPromotionAmount, eCoupon.order.getId(), eCoupon.eCouponSn);

        if (!TradeUtil.success(tradeBill, "退款成功.券号:" + eCoupon.getMaskedEcouponSn() + ",商品:" + eCoupon.goods.shortName)) {
            returnFlg = "{\"error\":\"refound failed\"}";
            return returnFlg;
        }

        // 更新已退款的活动金金额
        eCoupon.order.refundedAmount = eCoupon.order.refundedAmount.add(refundCashAmount).add(refundPromotionAmount);
        eCoupon.order.save();

        if (StringUtils.isBlank(userName)) {
            if (AccountType.RESALER == accountType) {
                Resaler resaler = Resaler.findById(userId);
                userName = "分销商:" + (resaler != null ? resaler.loginName : "");
            }
            if (AccountType.CONSUMER == accountType) {
                User user = User.findById(userId);
                userName = "消费者:" + user.getShowName();
            }
        }
        //记录券历史信息
        if (refundComment == null) {
            ECouponHistoryMessage.with(eCoupon).operator(userName).remark("未消费券退款").toStatus(ECouponStatus.REFUND).sendToMQ();
        } else {
            ECouponHistoryMessage.with(eCoupon).operator(userName).remark("未消费券退款:" + refundComment).toStatus(ECouponStatus.REFUND).sendToMQ();
        }


        // 更改订单状态
        eCoupon.status = ECouponStatus.REFUND;
        eCoupon.refundAt = new Date();
        eCoupon.refundPrice = refundCashAmount.add(refundPromotionAmount);
        eCoupon.save();

        TsingTuanOrder tsingTuanOrder = TsingTuanOrder.from(eCoupon);
        // 是清团券
        if (tsingTuanOrder != null) {
            TsingTuanSendOrder.refund(tsingTuanOrder);
        }

        // 更改搜索服务中的库存
        Solr.save(eCoupon.goods);

        return returnFlg;
    }

    /**
     * 得到券折扣金额
     *
     * @param coupon
     * @return
     */
    public static BigDecimal getLintRefundPrice(ECoupon coupon) {
        BigDecimal refundPrice = coupon.salePrice;
        //折扣金额
        BigDecimal rebateValue = coupon.rebateValue;
        rebateValue = rebateValue == null ? BigDecimal.ZERO : rebateValue;
        if (refundPrice.compareTo(rebateValue) > 0) {
            refundPrice = refundPrice.subtract(rebateValue);
        }
        return refundPrice;
    }

    /**
     * 得到隐藏处理过的券号
     *
     * @return 券号
     */
    @Transient
    public String getMaskedEcouponSn() {
        return InfoUtil.getMaskedEcouponSn(eCouponSn);
    }

    /**
     * 得到无空格、中文的券号.
     * 京东等平台不支持
     *
     * @return
     */
    public String getSafeECouponSN() {
        return InfoUtil.getFirstCharSequence(this.eCouponSn);
    }

    public String getSafeECouponPassword() {
        return InfoUtil.getFirstCharSequence(this.eCouponPassword);
    }

    /**
     * 获取后n位券号.
     *
     * @param count
     * @return
     */
    @Transient
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

    @Transient
    public Integer getAvaiableSendSMsCount() {
        if (smsSentCount > 3) {
            return 0;
        }
        return 3 - smsSentCount;
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
     * 判断指定时间是否是指定消费日并且在可用的验证时间范围.
     *
     * @param currentTime
     * @return
     */
    public boolean checkVerifyTimeRegion(Date currentTime) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(currentTime);
        int w = ca.get(Calendar.DAY_OF_WEEK);
        if (w == 1)
            w = 7;
        else
            w = w - 1;
        String useBeginTime = this.goods.useBeginTime;
        String useEndTime = this.goods.useEndTime;
        //如果选择了指定日期，并且现在时间不在指定时间范围内的，返回false
        String useWeekDay = this.goods.useWeekDay;
        if (useWeekDay != null && !"".equals(useWeekDay)) {
            //在指定日期范围内
            if (useWeekDay.contains(String.valueOf(w))) {
                if (StringUtils.isNotBlank(useBeginTime) && StringUtils.isNotBlank(useEndTime)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
                    String date = dateFormat.format(currentTime);
                    //在跨天的时间范围内，比如20：00~02:00
                    if (useBeginTime.compareTo(useEndTime) > 0) {
                        return date.compareTo(useBeginTime) > 0 || date.compareTo(useEndTime) < 0;
                    } else {
                        return (date.compareTo(useBeginTime) >= 0 && date.compareTo(useEndTime) <= 0);
                    }
                }
            } else {
                //不包含指定日期
                return false;
            }
        }

        return true;
    }


    /**
     * 判断开始使用时间和结束时间的大小
     *
     * @param currentTime
     * @return
     */
    public boolean checkUseBeginTimeAndUseEndTime(Date currentTime) {

        String useBeginTime = this.goods.useBeginTime;
        String useEndTime = this.goods.useEndTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        String date = dateFormat.format(currentTime);
        //判断是否显示次日
        if (useBeginTime.compareTo(useEndTime) < 0 && date.compareTo(useBeginTime) >= 0 && date.compareTo(useEndTime) <= 0) {
            return false;

        } else if (useBeginTime.compareTo(useEndTime) > 0) {
            return true;
        }

        return false;
    }

    /**
     * 按手机号及replyCode查出可用的ECoupon
     *
     * @param mobile    手机号
     * @param replyCode 返回码
     * @return 电子券
     */
    public static List<ECoupon> findByMobileAndCode(String mobile, String replyCode) {
        return ECoupon.find("from ECoupon where orderItems.phone=? and replyCode like ?", mobile, replyCode + "%").fetch();
    }

    public static void freeze(long id, String userName, ECoupon coupon) {
        update(id, 1, userName, coupon);
    }

    /**
     * 冻结此券
     *
     * @param id
     */
    public static void freeze(long id, String userName) {
        update(id, 1, userName);
    }

    /**
     * 冻结此券(刷单）
     *
     * @param id
     */
    public static void freeze(long id, String userName, Boolean isCheatedOrder) {
        update(id, 1, userName, isCheatedOrder);
    }

    /**
     * 解冻此券
     *
     * @param id
     */
    public static void unfreeze(long id, String userName) {
        update(id, 0, userName);
    }

    /**
     * 更新券 是否冻结
     * 在冻结单条券号时，提供三个单选选项：刷单、无法验证、其他，选择"其他"时需要填写文本 保存此信息
     */
    private static void update(long id, Integer isFreeze, String userName, ECoupon coupon) {
        ECoupon eCoupon = ECoupon.findById(id);
        //记录券历史信息
        eCoupon.isFreeze = isFreeze;
        eCoupon.freezedReason = coupon.freezedReason;
        switch (coupon.freezedReason) {
            case ISCHEATEDORDER:
                eCoupon.isCheatedOrder = true;
                ECouponHistoryMessage.with(eCoupon).operator(userName)
                        .remark(isFreeze == 0 ? "解冻券号" : "冻结券号(刷单)").sendToMQ();
                break;
            case UNABLEVERIFY:
                ECouponHistoryMessage.with(eCoupon).operator(userName)
                        .remark(isFreeze == 0 ? "解冻券号" : "冻结券号(无法验证)").sendToMQ();
                break;
            case OTHERS:
                eCoupon.otherReason = coupon.otherReason;
                ECouponHistoryMessage.with(eCoupon).operator(userName)
                        .remark(isFreeze == 0 ? "解冻券号" : "冻结券号(其他原因:" + coupon.otherReason + ")").sendToMQ();
                break;
        }
        eCoupon.save();
    }

    /**
     * 更新券(刷单订单)是否冻结 并且标记是刷单
     */
    private static void update(long id, Integer isFreeze, String userName, Boolean isCheatedOrder) {

        ECoupon eCoupon = ECoupon.findById(id);
        //记录券历史信息
        ECouponHistoryMessage.with(eCoupon).operator(userName)
                .remark(isFreeze == 0 ? "解冻券号" : "冻结券号(刷单)").sendToMQ();
        eCoupon.isFreeze = isFreeze;
        eCoupon.isCheatedOrder = isCheatedOrder;
        eCoupon.save();
    }

    /**
     * 更新券是否冻结
     */
    private static void update(long id, Integer isFreeze, String userName) {

        ECoupon eCoupon = ECoupon.findById(id);
        //记录券历史信息
        if (eCoupon.isCheatedOrder != true) {
            ECouponHistoryMessage.with(eCoupon).operator(userName)
                    .remark(isFreeze == 0 ? "解冻券号" : "冻结券号").sendToMQ();
            eCoupon.isFreeze = isFreeze;
            eCoupon.save();
        }
    }

    /**
     * 发送短信
     */
    public static void send(ECoupon eCoupon, String phone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(COUPON_EXPIRE_FORMAT);
        if (StringUtils.isBlank(phone)) {
            phone = eCoupon.orderItems.phone;
        }
        SMSUtil.send( (StringUtils.isNotEmpty(eCoupon.goods.title) ? eCoupon.goods.title : (eCoupon.goods.name + "[" + eCoupon.goods.faceValue + "元]")) + "券号" + eCoupon.eCouponSn + "," +
                "截止" + dateFormat.format(eCoupon.expireAt) + ",客服：4006262166", phone, eCoupon.replyCode);
    }

    /**
     * 发送短信
     */
    public static void sendInfo(ECoupon eCoupon, String phone, String couponshopsId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(COUPON_EXPIRE_FORMAT);
        List<Shop> shopList = new ArrayList<>();
        String content = "";
        if (StringUtils.isBlank(phone)) {
            phone = eCoupon.orderItems.phone;
        }
        if (StringUtils.isNotBlank(couponshopsId)) {
            String c[] = couponshopsId.split(",");
            content = ",";
            for (int i = 0; i < c.length; i++) {
                Shop shop = Shop.findById(Long.parseLong(c[i]));
                shopList.add(shop);
            }
            for (Shop s : shopList) {
                content += "[" + s.name + "]" + s.address + " " + s.phone + ";";
            }
        }
        if (StringUtils.isBlank(phone)) {
            phone = eCoupon.orderItems.phone;
        }
        //        send(eCoupon, phone);

        SMSUtil.send( (StringUtils.isNotEmpty(eCoupon.goods.title) ? eCoupon.goods.title : (eCoupon.goods.name + "[" + eCoupon.goods.faceValue + "元]")) + "券号"
                + eCoupon.eCouponSn + "," +
                "截止" + dateFormat.format(eCoupon.expireAt) + content + "客服：4006262166", phone, eCoupon.replyCode);
    }

    public static void sendUserMessageInfoWithoutCheck(String phone, ECoupon eCoupon, String couponshopsId) {
        sendInfo(eCoupon, phone, couponshopsId);
        eCoupon.smsSentCount++;
        eCoupon.save();
    }

    /**
     * 会员中心发送短信
     *
     * @param id
     * @return
     */
    public static boolean sendUserMessageInfo(long id, String couponshopsId) {
        ECoupon eCoupon = ECoupon.findById(id);
        boolean sendFlag = false;
        if (eCoupon != null && eCoupon.canSendSMSByConsumer()) {
            sendUserMessageInfoWithoutCheck(null, eCoupon, couponshopsId);
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

    /**
     * 从一组券中返回符合指定金额条件的券。
     *
     * @param payValue
     * @param ecoupons
     * @return
     */
    public static List<ECoupon> selectCheckECoupons(BigDecimal payValue, List<ECoupon> ecoupons) {
        return selectCheckECoupons(payValue, ecoupons, null);
    }

    /**
     * 从一组券中返回符合指定金额条件的券，需要包含ecoupon.
     *
     * @param payValue
     * @param ecoupons
     * @param eCoupon
     * @return
     */
    public static List<ECoupon> selectCheckECoupons(BigDecimal payValue, List<ECoupon> ecoupons, ECoupon eCoupon) {
        List<ECoupon> newECoupons = new ArrayList<>();
        for (ECoupon e : ecoupons) {
            if (eCoupon == null || !e.id.equals(eCoupon.id)) {
                newECoupons.add(e);
            }
        }

        Collections.sort(newECoupons, new Comparator<ECoupon>() {
            @Override
            public int compare(ECoupon e1, ECoupon e2) {
                return e2.faceValue.compareTo(e1.faceValue);
            }
        });
        BigDecimal totalValue = BigDecimal.ZERO;

        List<ECoupon> selectECoupons = new ArrayList<>();
        if (eCoupon != null && eCoupon.faceValue.compareTo(payValue) <= 0) {
            selectECoupons.add(eCoupon);
            payValue = payValue.subtract(eCoupon.faceValue);
        }

        for (ECoupon ecoupon : newECoupons) {
            int res = totalValue.add(ecoupon.faceValue).compareTo(payValue);
            if (res <= 0) {
                totalValue = totalValue.add(ecoupon.faceValue);
                selectECoupons.add(ecoupon);
                if (res == 0) {
                    break;
                }
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
            amount = amount.add(coupon.salePrice);
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

    public String getWeek() {
        String week = "";
        String useWeekDay = this.goods.useWeekDay;
        String[] arrWeek = StringUtils.isBlank(useWeekDay) ? null : useWeekDay.split(",");
        String[] weekDay = {"星期一,", "星期二,", "星期三,", "星期四,", "星期五,", "星期六,", "星期日,"};
        if (arrWeek != null) {
            for (String day : arrWeek) {
                week += weekDay[Integer.parseInt(day) - 1];
            }
        }
        return week;
    }

    /**
     * 拼接验证消息
     *
     * @return
     */
    public String getCheckInfo() {
        String info = "对不起，该券只能在";
        String useWeekDay = this.goods.useWeekDay;
        boolean isWeekDayAll = false;
        if ((useWeekDay != null && useWeekDay.length() == 13)) {
            isWeekDayAll = true;
        }

        String week = this.getWeek();
        if (!isWeekDayAll) {
            if (!"".equals(week))
                week = week.substring(0, week.length() - 1);
            info += week;
        } else {
            info += "每天";
        }
        info += "的" + this.goods.useBeginTime + "~";

        if (this.checkUseBeginTimeAndUseEndTime(new Date())) {
            info += "次日";
        }
        info += this.goods.useEndTime + "时间内使用！";
        return info;
    }

    /**
     * 取得推荐购买并消费额的金额
     *
     * @param promoteUserId
     * @return
     */
    public static BigDecimal getConsumedPromoteRebateAmount(Long promoteUserId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( e.salePrice ) FROM ECoupon e,Order o,PromoteRebate p" + " WHERE e.order=o and p.order=o and e.status=:status and o.promoteUserId=:promoteUserId and p.status=:p_status");
        q.setParameter("status", ECouponStatus.CONSUMED);
        q.setParameter("promoteUserId", promoteUserId);
        q.setParameter("p_status", RebateStatus.ALREADY_REBATE);
        Object result = q.getSingleResult();
        return result == null ? BigDecimal.ZERO : (BigDecimal) result;
    }

    /**
     * 用户已节省的总金额.
     *
     * @param userId
     * @param userType
     * @return
     */
    public static BigDecimal getSavedMoney(Long userId, AccountType userType) {
        Query q = JPA.em().createQuery("select sum(e.faceValue - e.salePrice) from ECoupon e " +
                "where e.order.userId = :userId and e.order.userType = :userType and e.goods.isLottery = :isLottery " +
                "and (e.status = :unconsumed or e.status = :consumed)");
        q.setParameter("isLottery", false);
        q.setParameter("userId", userId);
        q.setParameter("userType", userType);
        q.setParameter("unconsumed", ECouponStatus.UNCONSUMED);
        q.setParameter("consumed", ECouponStatus.CONSUMED);
        BigDecimal savedMoney = (BigDecimal) q.getSingleResult();
        if (savedMoney == null) {
            return BigDecimal.ZERO;
        }
        return savedMoney;
    }

    /**
     * 获取待消费笔数.
     *
     * @param userId
     * @param userType
     * @return
     */
    public static long getUnConsumedCount(Long userId, AccountType userType) {
        return ECoupon.count("order.userId = ? and order.userType = ? and status = ? and goods.isLottery=?", userId, userType, ECouponStatus.UNCONSUMED, false);
    }

    /**
     * 查询同一组商品的券.
     * 先按groupCode查，如果没groupCode，则查同一个goodsId的券.
     *
     * @param eCoupon
     * @return
     */
    public static List<ECoupon> queryUnconsumedCouponsWithSameGoodsGroups(ECoupon eCoupon) {
        if (eCoupon == null) {
            return new ArrayList<>();
        }
        Goods goods = eCoupon.goods;

        if (!StringUtils.isBlank(goods.groupCode)) {
            return ECoupon.find("order=? and orderItems.phone=? and status = ? and goods.isLottery=? and goods.groupCode=? and goods.supplierId=? order by id", eCoupon.order, eCoupon.orderItems.phone, ECouponStatus.UNCONSUMED, false, goods.groupCode, goods.supplierId).fetch();
        }
        return ECoupon.find("order=? and orderItems.phone=? and status=? and goods.isLottery=? and goods.id=? and goods.supplierId=? order by id", eCoupon.order, eCoupon.orderItems.phone, ECouponStatus.UNCONSUMED, false, goods.id, goods.supplierId).fetch();
    }

    /**
     * 得到券的可验证状态信息，如果为null，则可验证，否则不允许验证
     *
     * @param ecoupon
     * @return
     */
    public static String getECouponStatusDescription(ECoupon ecoupon, Long targetShopId) {
        if (ecoupon == null) {
            return "对不起，未找到此券!";
        }
        if (targetShopId == null) {
            return "对不起，该券有使用门店限制!";
        }
        if (ecoupon.isFreeze == 1) {
            return "对不起，该券已被冻结!";
        }
        if (ecoupon.status == models.order.ECouponStatus.CONSUMED) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日HH点mm分");
            Shop shop = Shop.findById(targetShopId);
            if (ecoupon.consumedAt == null) {
                return "对不起，该券已使用过。";
            }
            if (shop == null) {
                return "对不起，该券已使用过。 消费时间为" + format.format(ecoupon.consumedAt);
            }
            return "对不起，该券已使用过。 消费门店为" + shop.name + "，消费时间为" + format.format(ecoupon.consumedAt);
        }
        if (ecoupon.status == models.order.ECouponStatus.REFUND) {
            return "对不起，该券已退款!";
        }
        final Date now = new Date();
        if (ecoupon.expireAt != null && ecoupon.expireAt.before(now)) {
            return "对不起，该券已过期!";
        }
        if (ecoupon.effectiveAt != null && ecoupon.effectiveAt.after(now)) {
            return "对不起，该券有效期还没开始！";
        }
        //        if (!ecoupon.checkVerifyTimeRegion(new Date())) {
        //            //现在已经不在检查时间范围，所以先不处理
        //            Logger.error("券ID" + ecoupon.id + "(goodsId:" + ecoupon.goods.id + ")出现了时间段检查，但现在不建议使用时间段配置，请联系运营编辑。");
        //            return ecoupon.getCheckInfo();
        //        }
        return null;
    }

    /**
     * 得到券的可虚拟验证状态信息，如果为null，则可验证，否则不允许验证
     *
     * @param ecoupon
     * @return
     */
    public static String checkOtherECouponInfo(ECoupon ecoupon) {
        if (ecoupon == null) {
            return "对不起，未找到此券!";
        }
        if (ecoupon.status == models.order.ECouponStatus.CONSUMED) {
            return "对不起，该券已使用过。";
        }
        if (ecoupon.status == models.order.ECouponStatus.REFUND) {
            return "对不起，该券已退款!";
        }
        final Date now = new Date();
        if (ecoupon.expireAt != null && ecoupon.expireAt.before(now)) {
            return "对不起，该券已过期!";
        }
        if (ecoupon.effectiveAt != null && ecoupon.effectiveAt.after(now)) {
            return "对不起，该券有效期还没开始！";
        }
        return null;
    }

    /**
     * 获取预付款的已消费总额.
     *
     * @param prepayment 预付款记录
     * @return 预付款的已消费总额
     */
    public static BigDecimal getConsumedAmount(Prepayment prepayment) {
        BigDecimal usedAmount = find("select sum(originalPrice) from ECoupon " + "where goods.supplierId=? and status=? and consumedAt>=? and consumedAt<?", prepayment.supplier.id, models.order.ECouponStatus.CONSUMED, prepayment.effectiveAt, prepayment.expireAt).first();
        return usedAmount == null ? BigDecimal.ZERO : usedAmount;
    }

    public static BigDecimal findConsumedByDay(long supplierId, Date beginAt, Date endAt) {
        BigDecimal usedAmount = find("select sum(originalPrice) from ECoupon " + "where goods.supplierId=? and status=? and consumedAt>=? and consumedAt<?", supplierId, models.order.ECouponStatus.CONSUMED, DateUtil.getBeginOfDay(beginAt), DateUtil.getEndOfDay(endAt)).first();
        return usedAmount == null ? BigDecimal.ZERO : usedAmount;
    }

    /**
     * 获取最近验证过的n个券号.
     *
     * @param supplierUser
     * @param count
     * @return
     */
    public static List<String> getRecentVerified(SupplierUser supplierUser, int count) {
        return find("select eCouponSn from ECoupon where supplierUser=? and status=? order by consumedAt desc",
                supplierUser, ECouponStatus.CONSUMED).fetch(count);
    }


    /**
     * 发送券通知短信的方法，所以渠道都应使用此方法.
     *
     * @param phone
     * @param remark
     */
    public void sendOrderSMS(String phone, String remark) {
        OrderECouponMessage.with(this).phone(phone).remark(remark).sendToMQ();
    }

    public void sendOrderSMS(String remark) {
        OrderECouponMessage.with(this).remark(remark).sendToMQ();
    }

    /**
     * 判断运营人员是否可重新发送券号.
     *
     * @return
     */
    public boolean canSendSMSByOperate() {
        if (this.goods.isLottery) {
            return false;
        }
        if (this.status == ECouponStatus.UNCONSUMED) {
            return true;
        }
        if (this.goods.couponType == GoodsCouponType.IMPORT && this.status == ECouponStatus.CONSUMED) {
            // 导入券可继续发送
            return true;
        }
        return false;
    }

    /**
     * 判断是否可由消费者发送短信
     *
     * @return
     */
    public boolean canSendSMSByConsumer() {
        if (canSendSMSByOperate() && this.smsSentCount <= 3) {
            return true;
        }
        return false;
    }

    /**
     * 查询三天后京东，WB未消费并且是不可退款的券
     *
     * @return
     */
    public static List<ECoupon> findVirtualCoupons(CouponsCondition condition) {
        String sql = "select e from ECoupon e where e.virtualVerify=0 and e.goods.isLottery=false";
        Query query = ECoupon.em().createQuery(sql + condition.getQueryFitter() + " order by e.partner");
        for (Map.Entry<String, Object> entry : condition.getParamMap().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }

    /**
     * 虚拟验证券号
     *
     * @return
     */
    public boolean virtualVerify(Long operateUserId) {
        if (this.partner == ECouponPartner.JD) {
            if (!JDGroupBuyHelper.verifyOnJingdong(this)) {
                Logger.info("virtual verify on jingdong failed");
                return false;
            }
        } else if (this.partner == ECouponPartner.WB) {
            if (!WubaUtil.verifyOnWuba(this)) {
                Logger.info("virtual verify on wuba failed");
                return false;
            }
        } else if (this.partner == ECouponPartner.TB) {
            if (!TaobaoCouponUtil.verifyOnTaobao(this)) {
                Logger.info("verify on taobao failed");
                return false;
            }
        }
        this.virtualVerify = true;
        this.virtualVerifyAt = new Date();

        String operator = "";
        if (operateUserId != null) {
            this.operateUserId = operateUserId;
            operator = "操作员ID:" + operateUserId.toString();
        }
        this.save();
        ECouponHistoryMessage.with(this).operator(operator).remark("虚拟验证").sendToMQ();
        return true;
    }
}
