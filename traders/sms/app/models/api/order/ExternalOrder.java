package models.api.order;

import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import org.hibernate.annotations.Index;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author likang
 */
@Entity
@Table(name = "external_order")
public class ExternalOrder extends Model {
    @Column(name = "serial_number")
    @Index(name = "serial_number")
    public String serialNumber;

    @Column(name = "external_number", nullable = false)
    public String externalNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = false)
    public Goods goods;

    @Column(name = "number", nullable = false)
    public int number;

    @Column(name = "mobile", nullable = false)
    public String mobile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resaler_id", nullable = false)
    public Resaler resaler;

    @Column(name = "created_at")
    public Date createdAt;

    public ExternalOrder(){
        this.createdAt = new Date();
    }
}
