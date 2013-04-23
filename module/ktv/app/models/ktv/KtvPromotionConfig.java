package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 促销活动明细
 * User: tanglq
 * Date: 13-4-10
 * Time: 上午11:55
 */
@Entity
@Table(name="ktv_promotion_configs")
public class KtvPromotionConfig extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    public KtvPromotion promotion;

    /**
     * 连续预订总时长
     */
    @Column(name = "continuous_reserved_duration")
    public String continuousReservedDuration;

    /**
     * 连续/提前预订的折扣
     */
    public BigDecimal discount = BigDecimal.ONE;

    /**
     * 连续/提前预订立减的金额
     */
    public BigDecimal reducedPrice = BigDecimal.ONE;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    /**
     * 促销类型
     */
    @Required
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type")
    public KtvPromotionType promotionType;

}
