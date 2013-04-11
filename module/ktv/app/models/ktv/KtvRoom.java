package models.ktv;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * KTV房间.
 */
@Entity
@Table(name="ktv_rooms")
public class KtvRoom extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    public KtvRoomType roomType;
}
