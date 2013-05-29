package models.ktv;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 *         Date: 13-5-29
 */
@Entity
@Table(name = "ktv_date_range_price_schedules")
public class KtvDateRangePriceSchedule extends Model {
    @Column(name = "start_day")
    public Date startDay;

    @Column(name = "end_day")
    public Date endDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id")
    public KtvPriceSchedule schedule;
}
