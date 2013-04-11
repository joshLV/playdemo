package models.ktv;

import models.supplier.Supplier;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * KTV房间类型.
 */
@Entity
@Table(name="ktv_room_types")
public class KtvRoomType extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    public Supplier supplier;

    public String name;
}
