package models.sales;

import play.data.binding.As;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

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
     * @return
     */
    @Transient
    public boolean isExpired() {
        return secKillEndAt != null && secKillEndAt.before(new Date());
    }
}