package models.ktv;

import models.sales.Shop;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;
import java.util.Set;

/**
 * KTV促销活动
 */
@Entity
@Table(name="ktv_promotions")
public class KtvPromotion extends Model {

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "ktv_promotions_shops",
            inverseJoinColumns = @JoinColumn(name = "shop_id"),
            joinColumns = @JoinColumn(name = "ktv_promotion_id"))
    public Set<Shop> shops;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "ktvPromotion")
    @OrderBy("id")
    public List<KtvPromotionConfig> promotionConfigs;

}
