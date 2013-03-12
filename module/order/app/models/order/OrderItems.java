package models.order;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.MaterialType;
import models.sales.OrderBatch;
import models.sales.SecKillGoods;
import models.sales.Sku;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import play.db.jpa.JPA;
import play.db.jpa.Model;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 对应实物发货信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_info_id", nullable = true)
    public Logistic shippingInfo;

    /**
     * 商品选项，用于实物类订单导入时的商品选项属性，如尺寸，颜色等
     */
    @Column(name = "options")
    public String options;

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

    @Transient
    public BigDecimal getAmount() {
        if (salePrice != null && buyNumber != null) {
            return salePrice.multiply(new BigDecimal(buyNumber));
        }
        return BigDecimal.ZERO;
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
                "order.userId=:userId and order.userType=:userType");
        q.setParameter("goodsId", goodsId);
        q.setParameter("userId", user.id);
        q.setParameter("userType", AccountType.CONSUMER);
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
                "order.userId=:userId and order.userType=:userType and secKillGoods.id = :secKillGoodsId");
        q.setParameter("goodsId", goodsId);
        q.setParameter("userId", user.id);
        q.setParameter("userType", AccountType.CONSUMER);
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
        Query query = play.db.jpa.JPA.em().createQuery("select o.phone from OrderItems o where o.order.userId = :userId and o.order.userType =:userType group by o.phone order by o.order desc ");
        query.setParameter("userId", user.id);
        query.setParameter("userType", AccountType.CONSUMER);
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
    public static long getUnpaidOrderCount(Long userId, AccountType userType) {
        EntityManager entityManager = JPA.em();
        Query query = entityManager.createQuery("select o from OrderItems o where o.order.userId=:userId " +
                "and o.order.userType=:userType and o.status=:status group by o.order");

        query.setParameter("userId", userId);
        query.setParameter("userType", userType);
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
     * @param skuMap
     * @param toDate
     * @return
     */
    public static List<OrderItems> findPaid(Map<Sku, Long> skuMap, Date toDate) {
         StringBuilder sql = new StringBuilder("select o from OrderItems o where " +
                 "goods.materialType=:materialType and o.createdAt <= :toDate and o.goods.sku in (");
        for (int i = 0; i < skuMap.size(); i++) {
            sql.append(":sku").append(i);
            if (i != skuMap.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(") order by createdAt desc");
        Query query = OrderItems.em().createQuery(sql.toString());
        query.setParameter("materialType", MaterialType.REAL);
        query.setParameter("toDate", toDate);
        for (int i = 0; i < skuMap.keySet().size(); i++) {
            query.setParameter("sku" + i, skuMap.keySet().iterator().next());
        }

        Map<Sku, Long> skuCountMap = new HashMap<>();   //存放已经计算过的缺货的sku及数量

        List<OrderItems> allOrderItems = query.getResultList();

        List<OrderItems> stockoutOrderItems = new ArrayList<>();
        for (OrderItems orderItem : allOrderItems) {
            final Long count = skuCountMap.get(orderItem.goods.sku);
            if (count != null && count >= skuMap.get(orderItem.goods.sku)) {
                continue;
            }
            stockoutOrderItems.add(orderItem);
            skuCountMap.put(orderItem.goods.sku, count + orderItem.buyNumber);
        }
        return stockoutOrderItems;
    }

    public static long countPaidOrders(Date toDate) {
        Long count = count("order.orderType=? and status=? " +
                "and createdAt<=? group by order", OrderType.CONSUME, OrderStatus.PAID, toDate);
        return count == null ? 0 : count;
    }

    public static List<Order> findPaidOrders(Date toDate) {
        return find("select distinct(order) from OrderItems where order.orderType=? and status=? " +
                "and createdAt<=? group by order", OrderType.CONSUME, OrderStatus.PAID, toDate).fetch();
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
        List<TakeoutItem> takeoutItems = find("select new models.order.OrderItems.TakeoutItem(distinct(goods.sku),sum(buyNumber)) " +
                "from OrderItems where status=? and createdAt = ? and goods.materialType=? " +
                "group by goods.sku", OrderStatus.PAID, toDate, MaterialType.REAL).fetch();
        Map<Sku, Long> takeoutMap = new HashMap<Sku, Long>();
        for (TakeoutItem takeoutItem : takeoutItems) {
            if (takeoutItem.count > 0) {
                takeoutMap.put(takeoutItem.sku, takeoutItem.count);
            }
        }
        return takeoutMap;
    }

    /**
     * 出库表
     */
    class TakeoutItem {
        Sku sku;
        Long count;

        TakeoutItem(Sku sku, Long count) {
            this.sku = sku;
            this.count = count;
        }
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
}
