package models.ktv;

import models.sales.Shop;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * KTV价格策略.
 */
@Entity
@Table(name="ktv_price_schedues")
public class KtvPriceSchedue extends Model {

    @OneToMany
    public Set<Shop> shops;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ktv_room_type_id", nullable = true)
    public KtvRoomType ktvRoomType;

}
