package models.sales;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;

/**
 * 商圈区域.
 * <p/>
 * User: sujie
 * Date: 2/28/12
 * Time: 10:23 AM
 */
@Entity
@Table(name = "areas")
public class Area extends GenericModel {
    @Id
    public String id;

    public String getId() {
        return id;
    }

    @Override
    public Object _key() {
        return getId();
    }

    public String name;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    public Area parent;

    @Column(name = "display_order")
    public int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_type")
    public AreaType areaType;

    public Area() {
    }

    public Area(String id) {
        this.id = id;
    }

    /**
     * 获取前n个城市，主要用于主页上的显示.
     *
     * @param limit 获取的条数限制
     * @return 前n个城市
     */
    public static List<Area> findTopCities(int limit) {
        return findTopByAreaType(limit, AreaType.CITY);
    }

    /**
     * 获取前n个区域，主要用于主页上的显示.
     *
     * @param limit 获取的条数限制
     * @return 前n个区域
     */
    public static List<Area> findTopDistricts(int limit) {
        return findTopByAreaType(limit, AreaType.DISTRICT);
    }

    /**
     * 获取前n个商圈，主要用于主页上的显示
     *
     * @param limit 获取的条数限制
     * @return 前n个商圈
     */
    public static List<Area> findTopAreas(int limit) {
        return findTopByAreaType(limit, AreaType.AREA);
    }

    private static List<Area> findTopByAreaType(int limit, AreaType type) {
        return find("areaType=? order by displayOrder",
                type).fetch(limit);
    }

    /**
     * 获取所有子区域,
     *
     * @param areaId 区域id，如果是区域，则返回所有的商圈，如果是城市，则返回所有的区域
     * @return 所有子区域
     */
    public static List<Area> findAllSubAreas(String areaId) {
        return find("parent=? order by displayOrder",
                new Area(areaId)).fetch();
    }

    /**
     * 根据区域获取前n个商圈,或根据城市获取前n个区域.
     *
     * @param limit 获取的条数限制
     */
    public static List<Area> findTopAreas(String areaId, int limit) {
        return find("parent=? order by displayOrder",
                new Area(areaId)).fetch(limit);
    }
}
