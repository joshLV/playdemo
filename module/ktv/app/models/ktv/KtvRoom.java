package models.ktv;

import models.sales.Shop;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * KTV房间.
 */
@Entity
@Table(name = "ktv_rooms")
public class KtvRoom extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    public KtvRoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    public Shop shop;

    /**
     * 包厢房间名称.
     */
    public String name;

    public KtvRoom(KtvRoomType type, Shop shop) {
        this.roomType = type;
        this.shop = shop;
    }

    public static KtvRoom findKtvRoom(KtvRoomType type, Shop shop) {
        return KtvRoom.find("roomType=? and shop=?",type , shop).first();
    }

    /**
     * 取得各门店相应包厢数量
     */
    public static Long getRoomNumber(KtvRoomType roomType, Shop shop) {
        Long roomNumber = 0L;
        if (roomType == null || shop.id == null) {
            return roomNumber;
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("SELECT count( id ) FROM KtvRoom WHERE roomType = :roomType and shop = :shop");
        q.setParameter("roomType", roomType);
        q.setParameter("shop", shop);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }
}
