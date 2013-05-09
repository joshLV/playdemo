package models.ktv;

import models.sales.Shop;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * @author likang
 *
 * shop&schedule 唯一
 */
@Entity
@Table(name = "ktv_shop_price_schedules")
public class KtvShopPriceSchedule extends Model {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    public Shop shop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id")
    public KtvPriceSchedule schedule;

    public int roomCount;
}
