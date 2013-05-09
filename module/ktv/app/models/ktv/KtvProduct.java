package models.ktv;

import models.supplier.Supplier;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author likang
 *         Date: 13-5-6
 *         <p/>
 *         KTV 产品，一个产品下有多个价格排期(KtvPriceSchedule)
 */
@Entity
@Table(name = "ktv_products")
public class KtvProduct extends Model {
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    public String name;//产品名称

    public int duration;//欢唱时长
}
