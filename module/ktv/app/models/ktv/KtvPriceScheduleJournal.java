package models.ktv;

import models.taobao.OperateType;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午5:45
 */
@Entity
@Table(name = "ktv_price_schdule_journal")
public class KtvPriceScheduleJournal extends Model {
    public KtvProductGoods productGoods;
    public OperateType type;
    public Date createdAt;
    public Date updatedAt;
}
