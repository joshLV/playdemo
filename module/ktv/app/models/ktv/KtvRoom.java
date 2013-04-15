package models.ktv;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

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
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;
    /**
     * 包厢房间名称.
     */
    public String name;

    public KtvRoom(KtvRoomType type, Shop shop) {
        this.roomType = type;
        this.shop = shop;
        this.deleted = DeletedStatus.UN_DELETED;
    }

    public static List<KtvRoom> findKtvRoom(KtvRoomType type, Shop shop) {
        return KtvRoom.find("roomType=? and shop=? and deleted=? order by id", type, shop, DeletedStatus.UN_DELETED).fetch();
    }

    public static List<KtvRoom> findKtvRoomByShop(Shop shop) {
        return KtvRoom.find("shop=? and deleted=? group  by roomType order by id", shop, DeletedStatus.UN_DELETED).fetch();
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
        Query q = entityManager.createQuery("SELECT count( id ) FROM KtvRoom WHERE deleted =:deleted and roomType = :roomType and shop = :shop");
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("roomType", roomType);
        q.setParameter("shop", shop);
        Object result = q.getSingleResult();
        return result == null ? 0 : (Long) result;
    }

    public KtvRoom() {
    }
}
