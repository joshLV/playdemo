package models.sales;

import play.data.binding.As;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Money;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午10:17
 */
@Entity
@Table(name = "seckill_goods_item")
public class SecKillGoodsItem extends Model {
    private static final long serialVersionUID = 9063231063912330562L;
    private static final int MAX_SHOW_COUNT = 4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seckill_goods_id", nullable = true)
    public SecKillGoods secKillGoods;

    /**
     * 秒杀开始时间
     */
    @Required
    @Column(name = "seckill_begin_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date secKillBeginAt;

    /**
     * 秒杀结束时间
     */
    @Required
    @Column(name = "seckill_end_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date secKillEndAt;

    @Required
    @MaxSize(255)
    @Column(name = "goods_title")
    public String goodsTitle;

    /**
     * 秒杀价格
     */
    @Required
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    /**
     * 只需要上下架两个状态
     */
    @Enumerated(EnumType.STRING)
    public SecKillGoodsStatus status;

    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public int saleCount;
    /**
     * 实际库存
     */
    @Required
    @Min(0)
    @Max(999999)
    @Column(name = "base_sale")
    public Long baseSale;

    /**
     * 虚拟库存
     */
    @Min(0)
    @Max(999999)
    @Column(name = "virtual_nventory")
    public Long virtualInventory;
    /**
     * 虚拟售出数量
     */
    @Min(0)
    @Max(999999)
    @Column(name = "virtual_sale")
    public Long virtualSale;

    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    /**
     * 返回当前的秒杀商品
     *
     * @return
     */
    public static SecKillGoodsItem getCurrentSecKillGoods() {
        long count = SecKillGoodsItem.count("secKillEndAt>? and status=?", new Date(),
                SecKillGoodsStatus.ONSALE);
        if (count > 0) {
            return SecKillGoodsItem.find("secKillEndAt>? and status =? order by secKillBeginAt",
                    new Date(), SecKillGoodsStatus.ONSALE).first();
        } else {
            List<SecKillGoodsItem> items = findSecKillGoods();
            return items.get(items.size() - 1);
        }
    }

    /**
     * 返回除了给出秒杀商品的其他商品
     *
     * @return
     */
    public static List<SecKillGoodsItem> findSecKillGoods() {
        List<SecKillGoodsItem> items = SecKillGoodsItem.find("status =? order by secKillBeginAt",
                SecKillGoodsStatus.ONSALE).fetch(MAX_SHOW_COUNT);
//        List<SecKillGoodsItem> resultItems = new ArrayList<>();
//        for (SecKillGoodsItem secKillGoodsItem : items) {
//            if (secKillGoodsItem.id.longValue() != item.id.longValue()){
//                resultItems.add(secKillGoodsItem);
//            }
//        }
//        return resultItems;
        return items;
    }


    public static void update(Long id, SecKillGoodsItem secKillGoodsItem) {
        SecKillGoodsItem dbItem = SecKillGoodsItem.findById(id);
        dbItem.baseSale = secKillGoodsItem.baseSale;
        dbItem.goodsTitle = secKillGoodsItem.goodsTitle;
        dbItem.saleCount = secKillGoodsItem.saleCount;
        dbItem.salePrice = secKillGoodsItem.salePrice;
        dbItem.secKillBeginAt = secKillGoodsItem.secKillBeginAt;
        dbItem.secKillEndAt = secKillGoodsItem.secKillEndAt;
        dbItem.virtualSale = secKillGoodsItem.virtualSale;
        dbItem.status = secKillGoodsItem.status;
        dbItem.virtualInventory = secKillGoodsItem.virtualInventory;
        dbItem.save();
    }

    public static JPAExtPaginator<SecKillGoodsItem> findByCondition(SecKillGoodsCondition condition, Long seckillId, int pageNumber, int pageSize) {

        JPAExtPaginator<SecKillGoodsItem> goodsPage = new JPAExtPaginator<>
                ("SecKillGoodsItem g", "g", SecKillGoodsItem.class, condition.getItemFilter(seckillId),
                        condition.getParamMap())
                .orderBy("g.secKillBeginAt");
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);

        return goodsPage;
    }

    public static void updateStatus(SecKillGoodsStatus status, Long id) {
        SecKillGoodsItem goods = SecKillGoodsItem.findById(id);
        goods.status = status;
        goods.save();
    }

    /**
     * 是否过期.
     *
     * @return
     */
    @Transient
    public boolean isExpired() {
        return secKillEndAt != null && secKillEndAt.before(new Date());
    }

    @Transient
    public long getTotalCount() {
        return (virtualSale == null ? 0 : virtualSale.longValue()) + (virtualInventory == null ? 0 : virtualInventory
                .longValue());
    }

    /**
     * 节省多少钱
     *
     * @return
     */
    @Transient
    public BigDecimal getSavePrice() {
        return secKillGoods.goods.faceValue.subtract(salePrice);
    }


    @Column(name = "discount")
    public BigDecimal getDiscount() {
        BigDecimal discount;
        if (secKillGoods.goods.faceValue != null && salePrice != null && secKillGoods.goods.faceValue.compareTo(BigDecimal.ZERO) > 0) {
            discount = salePrice.divide(secKillGoods.goods.faceValue, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.TEN);
            if (discount.compareTo(BigDecimal.TEN) >= 0) {
                discount = BigDecimal.TEN;
            }
        } else {
            discount = BigDecimal.ZERO;
        }
        return discount;
    }

    @Transient
    public String getDiscountExpress() {
        BigDecimal discount = getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return "0折";
        }
        if (discount.compareTo(BigDecimal.TEN) >= 0) {
            return "无优惠";
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return "";
        }
        DecimalFormat format = new DecimalFormat("#.#");
        return format.format(discount.doubleValue()) + "折";
    }

    /**
     * 是否正在秒杀.
     *
     * @return
     */
    @Transient
    public boolean isBegin() {
        final Date now = new Date();
        return now.after(secKillBeginAt) && now
                .before(secKillEndAt) && baseSale > 0;
    }

    /**
     * 是否尚未开始秒杀.
     *
     * @return
     */
    @Transient
    public boolean isFuture() {
        final Date now = new Date();
        return now.before(secKillBeginAt);
    }

    /**
     * 是否秒杀结束.
     */
    @Transient
    public boolean isOver() {
        return isExpired() || baseSale <= 0;
    }

    /**
     * 随机产生一个虚拟数量.
     * <p/>
     * 根据实际库存n将虚拟库存平均分为n个格子，
     *
     * @param count
     * @param baseSale
     * @param saleCount
     * @param virtualSale
     * @param virtualInventory 虚拟库存
     * @param baseSale         实际库存
     * @return
     */
    public static long getRandomCount(long count, long baseSale, int saleCount, long virtualSale,
                                      long virtualInventory) {
        if (baseSale == 1) {
            return virtualInventory;
        }
        final long virtualTotal = virtualInventory + virtualSale;
        final long average = virtualTotal / (baseSale + saleCount - 1);
        long virtualTailCount = Math.abs(new Random().nextInt((int) average));
        System.out.println("virtualTailCount:" + virtualTailCount);
        virtualTailCount = virtualTailCount <= 0 ? 1 : virtualTailCount;
        return virtualInventory - (average * (baseSale - count - 1) + virtualTailCount);
    }

    /**
     * 修改库存.
     *
     * @param count 本次订单的实际售出数量
     */
    public void updateInventory(long count) {
        long virtualCount = getRandomCount(count, baseSale.longValue(), saleCount,
                virtualSale == null ? 0 : virtualSale.longValue(),
                virtualInventory.longValue());
        baseSale -= count;
        saleCount += count;
        virtualSale = virtualSale == null ? 0 : virtualSale;
        virtualSale += virtualCount;
        virtualInventory -= virtualCount;
        save();
    }

}
