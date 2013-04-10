package models.ktv;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * KTV房间.
 */
@Entity
@Table(name="ktv_rooms")
public class KtvRoom extends Model {

}
