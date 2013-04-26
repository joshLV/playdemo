package models.order;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.TradeUtil;
import models.consumer.User;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.MaterialType;
import models.sales.OrderBatch;
import models.sales.SecKillGoods;
import models.sales.Sku;
import models.supplier.Supplier;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.solr.Solr;
import util.common.InfoUtil;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "order_items")
public class OrderItems extends Model {


    private static final long serialVersionUID = 16323208753562L;

    /**
     * 所属商品历史ID
     */
    @Column(name = "goods_history_id")
    public Long goodsHistoryId;

    // ====  价格列表  ====
    @Column(name = "face_value")
    public BigDecimal faceValue;        //商品面值、市场价

    @Column(name = "original_price")
    public BigDecimal originalPrice;    //供应商进货价

    @Column(name = "resaler_price")
    public BigDecimal resalerPrice;     //用户在哪个分销商平台购买的价格，用于计算分销平台的佣金

    @Column(name = "sale_price")
    public BigDecimal salePrice;        //最终成交价,对于普通分销商来说，此成交价与以上分销商价(resalerPrice)相同；

    /**
     * 折扣掉的费用.
     * 注意：这是指总共折扣掉的费用，如果购买多笔，需要计算合计折扣的费用。
     */
    @Column(name = "rebate_value")
    public BigDecimal rebateValue;

    /**
     * 实物订单的发货批次.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_batch_id", nullable = true)
    public OrderBatch orderBatch;

    @Transient
    public String outerOrderId;

    /**
     * 当前订单项总费用：
     * lineValue = salePrice*buyNumber - rebateValue
     */
    @Transient
    public BigDecimal getLineValue() {
        if (rebateValue == null) {
            rebateValue = BigDecimal.ZERO;
        }
        return salePrice.multiply(new BigDecimal(buyNumber)).subtract(rebateValue);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @Column(name = "goods_name")
    public String goodsName;

    @Column(name = "buy_number")
    public Long buyNumber;

    @Column(name = "return_count")
    public Long returnCount = 0L;

    /**
     * 对应实物发货信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_info_id", nullable = true)
    public OrderShippingInfo shippingInfo;

    /**
     * 商品选项，用于实物类订单导入时的商品选项属性，如尺寸，颜色等
     */
    @Column(name = "options")
    public String options;

    /**
     * 导入订单的渠道商品编码，如：京东订单表中的商品ID,新浪的模板、商品ID
     */
    @Column(name = "outer_goods_no")
    public String outerGoodsNo;

    @Column(name = "seckill_goods_item_id", nullable = true)
    public Long secKillGoodsItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seckill_goods_id", nullable = true)
    public SecKillGoods secKillGoods;

    public String phone;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "send_at")
    public Date sendAt;

    @Enumerated(EnumType.STRING)
    public OrderStatus status;

    public static final String CACHEKEY = "ORDERITEM";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Transient
    public BigDecimal getAmount() {
        if (salePrice != null && buyNumber != null) {
            return salePrice.multiply(new BigDecimal(buyNumber));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 订单项中的sku数量.
     *
     * @return
     */
    @Transient
    public long getSkuCount() {
        return (goods.skuCount == null ? 0L : goods.skuCount) *
                (buyNumber == null ? 0L : buyNumber);
    }

    /**
     * 获取退货状态.
     * 便于页面显示
     *
     * @return
     */
    @Transient
    public RealGoodsReturnStatus getReturnStatus() {
        if (goods.materialType == MaterialType.ELECTRONIC) {
            return null;
        }
        return RealGoodsReturnEntry.getReturnStatus(this);
    }

    /**
     * 获取退货单.
     * 便于页面显示
     *
     * @return
     */
    @Transient
    public RealGoodsReturnEntry getReturnEntry() {
        return RealGoodsReturnEntry.findByOrderItem(this.id);
    }

    public OrderItems() {
    }

    public OrderItems(Order order, Goods goods, long buyNumber, String phone, BigDecimal salePrice, BigDecimal resalerPrice) {
        this.order = order;
        this.goods = goods;
        this.goodsHistoryId = getLastHistoryId(goods.id);
        this.faceValue = goods.faceValue;
        this.originalPrice = goods.originalPrice;
        this.salePrice = salePrice;
        this.resalerPrice = resalerPrice;
        this.goodsName = goods.shortName;
        this.buyNumber = buyNumber;
        this.phone = phone;
        this.status = OrderStatus.UNPAID;
        this.createdAt = new Date();
    }

    public static Long getLastHistoryId(Long goodsId) {
        GoodsHistory goodsHistory = GoodsHistory.find("goodsId=? order by id desc", goodsId).first();
        return goodsHistory == null ? null : goodsHistory.id;
    }

    public static long itemsNumber(Order order) {
        long itemsNumber = 0L;
        if (order == null) {
            return itemsNumber;
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE order = :order");
        q.setParameter("order", order);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    public static long itemsNumberElectronic(Order order) {
        long itemsNumber = 0L;
        if (order == null) {
            return itemsNumber;
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE order = :order and goods.materialType=:materialType");
        q.setParameter("order", order);
        q.setParameter("materialType", MaterialType.ELECTRONIC);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    /**
     * 取出该用户购买指定商品的数量
     *
     * @param user    用户
     * @param goodsId 商品ID
     * @return
     */
    public static long itemsNumber(User user, Long goodsId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE goods.id=:goodsId and " +
                "order.consumerId=:consumerId");
        q.setParameter("goodsId", goodsId);
        q.setParameter("consumerId", user.id);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    /**
     * 取出该用户购买指定商品的数量
     *
     * @param phone   用户手机
     * @param goodsId 商品ID
     * @return
     */
    public static long getBuyNumberByPhone(String phone, Long goodsId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE goods.id=:goodsId and phone=:phone ");
        q.setParameter("goodsId", goodsId);
        q.setParameter("phone", phone);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    public static long getBoughtNumberOfSecKillGoods(User user, Long goodsId, Long secKillGoodsId) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT sum( buyNumber ) FROM OrderItems WHERE goods.id=:goodsId and " +
                "order.consumerId=:consumerId and secKillGoods.id = :secKillGoodsId");
        q.setParameter("goodsId", goodsId);
        q.setParameter("consumerId", user.id);
        q.setParameter("secKillGoodsId", secKillGoodsId);
        Object result = q.getSingleResult();

        return result == null ? 0 : (Long) result;
    }

    /**
     * 处理券号
     *
     * @return 券号
     */
    public String getEcouponSn() {
        List<ECoupon> ecoupons = getECoupons();
        StringBuilder sn = new StringBuilder();
        for (ECoupon e : ecoupons) {
            sn.append(e.getMaskedEcouponSn() + "\n");
        }
        return sn.toString();
    }

    /**
     * 处理券号
     *
     * @return 券号
     */
    public String getWebEcouponSn() {
        List<ECoupon> ecoupons = getECoupons();
        StringBuilder sn = new StringBuilder();
        for (ECoupon e : ecoupons) {
            sn.append(e.eCouponSn);
            sn.append("\n");
        }
        return sn.toString();
    }

    public List<ECoupon> getECoupons() {
        Query query = play.db.jpa.JPA.em().createQuery(
                "select e from ECoupon e where e.order = :order and e.goods =:goods ");
        query.setParameter("order", this.order);
        query.setParameter("goods", this.goods);
        return query.getResultList();
    }

    /**
     * 取得购买过得手机号
     *
     * @param user
     * @return
     */
    public static List<String> getMobiles(User user) {
        Query query = play.db.jpa.JPA.em().createQuery("select o.phone from OrderItems o where o.order.consumerId = :consumerId group by o.phone order by o.order desc ");
        query.setParameter("consumerId", user.id);
        query.setFirstResult(0);
        query.setMaxResults(10);
        return query.getResultList();
    }

    /**
     * 得到掩码过的手机号
     */
    @Transient
    public String getMaskedPhone() {
        return InfoUtil.getMaskedPhone(this.phone);
    }

    /**
     * 计算会员订单明细中已购买的商品
     *
     * @param user    用户ID
     * @param goodsId 商品ID
     * @param number  购买数量
     * @return
     */
    public static boolean checkLimitNumber(User user, Long goodsId, Long secKillGoodsId,
                                           long number) {

        long boughtNumber = OrderItems.getBoughtNumberOfSecKillGoods(user, goodsId, secKillGoodsId);
        //取出商品的限购数量
        models.sales.SecKillGoods goods = SecKillGoods.findById(secKillGoodsId);
        int limitNumber = 0;
        if (goods.personLimitNumber != null) {
            limitNumber = goods.personLimitNumber;
        }

        //超过限购数量,则表示已经购买过该商品
        return (limitNumber > 0 && (number > limitNumber || limitNumber <= boughtNumber));
    }

    /**
     * 获取未付款笔数
     */
    public static long getUnpaidOrderCount(User user) {
        Query query = OrderItems.em().createQuery("select o from OrderItems o where o.order.consumerId=:consumerId " +
                "and o.status=:status group by o.order");

        query.setParameter("consumerId", user.id);
        query.setParameter("status", OrderStatus.UNPAID);
        List result = query.getResultList();
        return (result == null || result.isEmpty()) ? 0l : result.size();
    }

    public static List<OrderItems> findBySupplierOrder(long supplierId, long orderId) {
        Query query = OrderItems.em().createQuery("select o from OrderItems o where o.order = :order and o.goods.supplierId = :supplier");
        query.setParameter("order", Order.findById(orderId));
        query.setParameter("supplier", supplierId);
        return query.getResultList();
    }

    /**
     * 查询待发货的指定sku缺货的订单项.
     *
     * @param deficientCountMap
     * @param toDate
     * @return
     */
    public static List<OrderItems> findDeficientOrderItemList(Map<Sku, Long> deficientCountMap, Date toDate) {
        //获取缺货货品的相关的所有的订单项
        List<OrderItems> allOrderItems = getAllPaidOrderItemListInSkus(deficientCountMap.keySet(), toDate);

        Map<Sku, Long> skuDeficientCountMap = new HashMap<>();   //存放已经计算过的缺货的sku及数量

        Map<Sku, ArrayList<OrderItems>> deficientOrderItemMap = new HashMap<>();

        for (OrderItems orderItem : allOrderItems) {
            final Sku sku = orderItem.goods.sku;
            Long addedCount = skuDeficientCountMap.get(sku);
            if (addedCount != null && addedCount >= deficientCountMap.get(sku)) {
                continue;
            }
            addedCount = (addedCount == null) ? 0L : addedCount;

            Logger.info("OrderItems.findDeficientOrderItemList sku:" + sku.name + ", addedCount:" + addedCount);

            ArrayList<OrderItems> deficientItemSet = deficientOrderItemMap.get(sku);
            ArrayList<OrderItems> items = null;
            if (deficientItemSet == null) {
                items = new ArrayList<>();
                items.add(orderItem);
                deficientOrderItemMap.put(sku, items);

            } else {
                deficientItemSet.add(orderItem);
                deficientOrderItemMap.put(sku, deficientItemSet);
            }

            skuDeficientCountMap.put(sku, addedCount + orderItem.getSkuCount());
        }

        List<OrderItems> deficientOrderItems = new ArrayList<>();
        for (Sku sku : deficientOrderItemMap.keySet()) {
            ArrayList<OrderItems> deficientItemSet = deficientOrderItemMap.get(sku);
            Collections.sort(deficientItemSet, new Comparator<OrderItems>() {

                @Override
                public int compare(OrderItems o1, OrderItems o2) {
                    if (o1.getSkuCount() == o2.getSkuCount()) {
                        return 0;
                    }
                    return (o1.getSkuCount() < o2.getSkuCount()) ? 1 : -1;
                }
            });

            long deficientCurrentTotalCount = 0L;
            for (OrderItems deficientOrderItem : deficientItemSet) {
                deficientCurrentTotalCount += deficientOrderItem.getSkuCount();
                deficientOrderItems.add(deficientOrderItem);
                if (deficientCurrentTotalCount >= deficientCountMap.get(sku)) {
                    break;
                }
            }
        }
        return deficientOrderItems;
    }

    /**
     * 获取视惠指定货品的付款状态的订单项.
     *
     * @param skus
     * @param toDate
     * @return
     */
    private static List<OrderItems> getAllPaidOrderItemListInSkus(Set<Sku> skus, Date toDate) {
        StringBuilder sql = new StringBuilder("select o from OrderItems o where " +
                "goods.supplierId=:supplierId and goods.materialType=:materialType and o.createdAt <= :toDate and o.goods.sku in (");
        for (int i = 0; i < skus.size(); i++) {
            sql.append(":sku").append(i);
            if (i != skus.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(") order by createdAt desc");
        Query query = OrderItems.em().createQuery(sql.toString());
        query.setParameter("supplierId", Supplier.getShihui().id);
        query.setParameter("materialType", MaterialType.REAL);
        query.setParameter("toDate", toDate);
        Iterator<Sku> iterator = skus.iterator();
        for (int i = 0; i < skus.size(); i++) {
            query.setParameter("sku" + i, iterator.next());
        }
        return query.getResultList();
    }

    public static long countPaidOrders(Goods goods) {
        Long count = find("select sum(buyNumber-returnCount) from OrderItems where goods.materialType=? and order.orderType=? and status=? " +
                "and goods=? and goods.skuCount is not null", MaterialType.REAL, OrderType.CONSUME, OrderStatus.PAID, goods).first();
        return count == null ? 0 : count;
    }

    public static List<Order> findPaidRealGoodsOrders(Date toDate) {
        Query q = OrderItems.em().createQuery("select o.order from OrderItems o where o.goods.supplierId=:supplierId " +
                "and o.goods.materialType=:materialType " +
                "and o.order.orderType=:orderType and o.status=:status " +
                "and o.createdAt<=:createdAt group by o.order");
        q.setParameter("supplierId", Supplier.getShihui().id);
        q.setParameter("materialType", MaterialType.REAL);
        q.setParameter("orderType", OrderType.CONSUME);
        q.setParameter("status", OrderStatus.PAID);
        q.setParameter("createdAt", toDate);

        return q.getResultList();
    }


    public static List<Order> getStockOutOrders(List<Order> allPaidOrders, List<Order> deficientOrders) {
        Map<Long, Order> deficientOrderMap = new HashMap<>();
        for (Order deficientOrder : deficientOrders) {
            deficientOrderMap.put(deficientOrder.id, deficientOrder);
        }
        List<Order> stockoutOrders = new ArrayList<>();
        for (Order paidOrder : allPaidOrders) {
            if (!deficientOrderMap.containsKey(paidOrder.id)) {
                stockoutOrders.add(paidOrder);
            }
        }

        return stockoutOrders;
    }

    /**
     * 查询指定时间之前的sku出库表.
     */
    public static Map<Sku, Long> findTakeout(Date toDate) {
        List<TakeoutItem> takeoutItems = find("select new models.order.TakeoutItem(o.goods.sku, sum(o.buyNumber*o.goods.skuCount)) " +
                "from OrderItems o where o.order.orderType=? and o.goods.supplierId=? and o.status=? and o.goods.materialType=? and o.order.paidAt <= ? " +
                "group by o.goods.sku", OrderType.CONSUME, Supplier.getShihui().id, OrderStatus.PAID, MaterialType.REAL, toDate).fetch();
        Map<Sku, Long> takeoutMap = new HashMap<Sku, Long>();
        for (TakeoutItem takeoutItem : takeoutItems) {
            if (takeoutItem.sku != null && takeoutItem.count != null && takeoutItem.count.longValue() > 0) {
                takeoutMap.put(takeoutItem.sku, takeoutItem.count);
            }
        }
        return takeoutMap;
    }

    /**
     * 获取预付款的已销售总额.
     *
     * @param prepayment 预付款记录
     * @return 预付款的已销售总额
     */
    public static BigDecimal getSoldAmount(Prepayment prepayment) {
        BigDecimal soldAmount = find("select sum(originalPrice*buyNumber) from OrderItems " +
                "where goods.supplierId=? and (status=? or status=?) and createdAt>=? and createdAt <?", prepayment.supplier.id, OrderStatus.PAID, OrderStatus.SENT, prepayment.effectiveAt, prepayment.expireAt).first();
        return soldAmount == null ? BigDecimal.ZERO : soldAmount;
    }

    public static BigDecimal findSoldByDay(long supplierId, Date beginAt, Date endAt) {
        BigDecimal soldAmount = find("select sum(originalPrice*buyNumber) from OrderItems " +
                "where goods.supplierId=? and (status=? or status=?) and createdAt>=? and createdAt <?",
                supplierId, OrderStatus.PAID, OrderStatus.SENT, DateUtil.getBeginOfDay(beginAt), DateUtil.getEndOfDay(endAt)).first();
        return soldAmount == null ? BigDecimal.ZERO : soldAmount;
    }

    /**
     * 检查订单中的实物商品是否有对应的sku.
     *
     * @param orderId
     * @return
     */
    public static Goods findNoSkuGoods(Long orderId) {
        List<OrderItems> orderItemsList = find("order.id=? and goods.materialType=?", orderId, MaterialType.REAL).fetch();
        for (OrderItems orderItems : orderItemsList) {
            if (orderItems.goods.sku == null) {
                return orderItems.goods;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.order).append(this.goods)
                .append(this.id).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderItems other = (OrderItems) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.order, other.order)
                .append(this.goods, other.goods).append(this.id, other.id).isEquals();
    }

    /**
     * 计算平均售价.
     *
     * @param stockoutOrderList
     * @param takeoutSkuMap
     * @return
     */
    public static Map<Sku, BigDecimal> getSkuAveragePriceMap(List<Order> stockoutOrderList, Map<Sku, Long> takeoutSkuMap) {
        Map<Sku, BigDecimal> averagePriceMap = new HashMap<>();
        Map<Sku, BigDecimal> amountMap = new HashMap<>();

        for (Order order : stockoutOrderList) {
            for (OrderItems orderItem : order.orderItems) {
                BigDecimal amount = amountMap.get(orderItem.goods.sku);
                final BigDecimal buyNumber = BigDecimal.valueOf(orderItem.buyNumber);
                final BigDecimal sum = orderItem.salePrice.multiply(buyNumber);
                if (amount == null) {
                    amount = sum;
                } else {
                    amount = amount.add(sum);
                }
                amountMap.put(orderItem.goods.sku, amount);
            }
        }

        BigDecimal average = BigDecimal.ZERO;
        for (Sku sku : amountMap.keySet()) {
            final BigDecimal skuAmount = amountMap.get(sku);
            if ((takeoutSkuMap.get(sku) != null) && (takeoutSkuMap.get(sku) > 0l)) {
                average = skuAmount.divide(BigDecimal.valueOf(takeoutSkuMap.get(sku)), 2, BigDecimal.ROUND_HALF_UP);
                averagePriceMap.put(sku, average);
            }
        }

        return averagePriceMap;
    }

    /**
     * 实物退款处理.
     *
     * @param orderItems
     */
    public static String handleRefund(OrderItems orderItems, Long returnedCount) {
        String returnFlg = "";

        if (orderItems == null || orderItems.id == null || returnedCount == null || returnedCount <= 0L || returnedCount > orderItems.buyNumber) {
            returnFlg = "{\"error\":\"no such eCoupon\"}";
            return returnFlg;
        }
        //todo 刷单的不可退款
//        if (orderItems.isCheatedOrder) {
//            returnFlg = "{\"error\":\"cheated order can't refund\"}";
//            return returnFlg;
//        }
        //未付款和已取消的订单不能退款
        if (orderItems.status == OrderStatus.CANCELED || orderItems.status == OrderStatus.UNPAID) {
            returnFlg = "{\"error\":\"can not apply refund with this goods\"}";
            return returnFlg;
        }


        //先计算已消费的金额
        BigDecimal consumedAmount = BigDecimal.ZERO;
        Order order = orderItems.order;
        if (order.isWebsiteOrder()) {
            consumedAmount = orderItems.salePrice.multiply(new BigDecimal(returnedCount));
        } else {
            consumedAmount = orderItems.resalerPrice.multiply(new BigDecimal(returnedCount));
        }

        //最后我们来看看最终能退多少
        BigDecimal refundPromotionAmount = BigDecimal.ZERO;

        // 创建退款交易
        Account account = orderItems.order.getBuyerAccount();
        Logger.info("account=" + account.id + ", refundCashAmount=" + consumedAmount + ", " +
                "refundPromotionAmount=" + refundPromotionAmount);
        TradeBill tradeBill = TradeUtil.refundTrade()
                .toAccount(account)
                .balancePaymentAmount(consumedAmount)
                .promotionPaymentAmount(refundPromotionAmount)
                .orderId(orderItems.order.getId())
                .make();

        if (!TradeUtil.success(tradeBill, "退款成功. 商品:" + orderItems.goods.shortName)) {
            returnFlg = "{\"error\":\"refound failed\"}";
            return returnFlg;
        }

        // 更新已退款的活动金金额
        order.refundedAmount = order.refundedAmount.add(consumedAmount).add(refundPromotionAmount);
        order.save();

        orderItems.returnCount += returnedCount;
        orderItems.status = OrderStatus.RETURNED;
        orderItems.save();

        // 更改搜索服务中的库存
        Solr.save(orderItems.goods);

        return returnFlg;

    }

    @Transient
    public Long getUnusedECouponNumber() {
        final OrderItems thisOrderItems = this;
        return CacheHelper.getCache(CacheHelper.getCacheKey(CACHEKEY + this.id, "UNUSEDECouponNumber"), new CacheCallBack<Long>() {
            @Override
            public Long loadData() {
                return ECoupon.count("orderItems=? and status<>?", thisOrderItems, ECouponStatus.CONSUMED);
            }
        });
    }


}
