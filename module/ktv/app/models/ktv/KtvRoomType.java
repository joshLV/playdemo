package models.ktv;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * KTV房间类型.
 */
@Entity
@Table(name="ktv_room_types")
public class KtvRoomType extends Model {
    public String name;
}
