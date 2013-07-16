package models.sales;

import models.resale.Resaler;
import play.data.validation.Max;
import play.data.validation.Min;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * User: yan
 * Date: 13-7-9
 * Time: 下午4:31
 */
@Table(name = "goods_resaler_commissions")
@Entity
public class GoodsResalerCommission extends Model {
    @ManyToOne
    public Goods goods;

    @ManyToOne
    public Resaler resaler;
    /**
     * 佣金比例
     */
    @Column(name = "commission_ratio")
    @Min(0)
    @Max(100)
    public BigDecimal commissionRatio = new BigDecimal(0);
}
