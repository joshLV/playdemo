package models.ktv;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author likang
 *  Date: 13-5-6
 *
 * KTV 产品，一个产品下有多个价格排期(KtvPriceSchedule)
 */
@Entity
@Table(name = "ktv_product")
public class KtvProduct extends Model {
    public String name;//产品名称

    public int duration;//欢唱时长
}
