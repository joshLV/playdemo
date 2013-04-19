package models.ktv;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import models.ktv.KtvSalesPromotion;

/**
 * Ktv促销活动明细.
 * <p/>
 * User: wangjia
 * Date: 13-4-18
 * Time: 下午2:18
 */
@Entity
@Table(name = "ktv_sales_promotion_items")
public class KtvSalesPromotionItem extends Model {
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_promotion_id")
    public KtvSalesPromotion salesPromotion;

    /**
     * 连续预订总时长
     */
    @Column(name = "continuous_reserved_duration")
    public String continuousReservedDuration;

    /**
     * 连续/提前预订的折扣
     */
    public BigDecimal discount;

    /**
     * 连续/提前预订立减的金额
     */
    public BigDecimal reducedPrice;




}
