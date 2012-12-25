package models.order;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.sales.Goods;
import models.sales.GoodsHistory;
import models.sales.MaterialType;
import models.sales.SecKillGoods;
import play.db.jpa.JPA;
import play.db.jpa.Model;

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
import java.util.Date;
import java.util.List;

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
        Query query = play.db.jpa.JPA.em().createQuery(
                "select e from ECoupon e where e.order = :order and e.goods =:goods ");
        query.setParameter("order", this.order);
        query.setParameter("goods", this.goods);
        List<ECoupon> favs = query.getResultList();
        StringBuilder sn = new StringBuilder();
        for (ECoupon e : favs) {
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
        Query query = play.db.jpa.JPA.em().createQuery(
                "select e from ECoupon e where e.order = :order and e.goods =:goods ");
        query.setParameter("order", this.order);
        query.setParameter("goods", this.goods);
        List<ECoupon> favs = query.getResultList();
        StringBuilder sn = new StringBuilder();
        for (ECoupon e : favs) {
            sn.append(e.eCouponSn);
            sn.append("\n");
        }
        return sn.toString();
    }

    /**
     * 取得购买过得手机号
     *
     * @param user
     * @return
     */
    public static List<String> getMobiles(User user) {
        Query query = play.db.jpa.JPA.em().createQuery(
                "select o.phone from OrderItems o where o.order.userId = :userId and o.order.userType =:userType group by o.phone order by o.order desc ");
        query.setParameter("userId", user.id);
        query.setParameter("userType", AccountType.CONSUMER);
        query.setFirstResult(0);
        query.setMaxResults(10);
        return query.getResultList();
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
//        long count =  count("from OrderItems o where o.order.userId=? and o.order.userType=? and o.status=? group by o.order",userId, userType, OrderStatus.UNPAID);
//        return count;
//        Query q = entityManager.createQuery("select count(o) from OrderItems o where o.order.userId=:userId " +
//                "and o.order.userType=:userType and o.status=:status group by o.order");
//        q.setParameter("userId", userId);
//        q.setParameter("userType", userType);
//        q.setParameter("status", OrderStatus.UNPAID);
//
//        return CollectionUtils.isEmpty(q.getResultList()) ? 0l : (Long) q.getSingleResult();*/
    }

    public static List<OrderItems> findBySupplierOrder(long supplierId, long orderId) {
        Query query = OrderItems.em().createQuery("select o from OrderItems o where o.order = :order and o.goods.supplierId = :supplier");
        query.setParameter("order", Order.findById(orderId));
        query.setParameter("supplier", supplierId);
        return query.getResultList();
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
}
