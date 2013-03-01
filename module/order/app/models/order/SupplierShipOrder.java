package models.order;

import models.supplier.Supplier;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * 给商户的发货单.
 * <p/>
 * User: sujie
 * Date: 2/27/13
 * Time: 2:52 PM
 */
@Entity
@Table(name = "supplier_ship_order")
public class SupplierShipOrder extends Model {

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @Column(name = "created_at")
    public Date createdAt;

    public Long count;

    public static List<SupplierShipOrder> getTop(int count) {
        return find("").fetch(count);
    }

    public static List<SupplierShipOrder> getTop(long supplierId, int count) {
        return find("supplier.id=?").fetch(count);
    }
}
