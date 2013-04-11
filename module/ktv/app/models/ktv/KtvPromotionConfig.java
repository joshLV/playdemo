package models.ktv;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
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


}
