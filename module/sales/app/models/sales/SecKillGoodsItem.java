package models.sales;

import play.data.validation.*;
import play.db.jpa.Model;
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
    @InFuture
    @Column(name = "seckill_begin_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date secKillBeginAt;

    /**
     * 秒杀结束时间
     */
    @Required
    @InFuture
    @Column(name = "seckill_end_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date secKillEndAt;

    @Required
    @MaxSize(60)
    @Column(name = "goods_title")
    public String goodsTitle;

    /**
     * 秒杀价格
     */
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
     * 库存
     */
    @Required
    @Min(0)
    @Max(999999)
    @Column(name = "base_sale")
    public Long baseSale;


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

}
