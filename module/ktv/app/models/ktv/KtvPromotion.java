package models.ktv;

import models.sales.Shop;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * User: tanglq
 * Date: 13-4-10
 * Time: 上午11:55
 */
@Entity
@Table(name="ktv_promotions")
public class KtvPromotion extends Model {

    @OneToMany
    public Set<Shop> shops;

}
