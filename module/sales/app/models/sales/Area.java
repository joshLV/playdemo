package models.sales;

import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
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
    public static List<Area> findTopDistricts(String areaId, int limit) {
        return findTopAreas(areaId, limit);
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
        if (areaId == null || "".equals(areaId)) {
            return find("areaType=? order by displayOrder",
                    AreaType.CITY).fetch();
        }
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

    /**
     * 返回包含指定区域的前n个区域.
     * 如果之前所选的区域未在返回列表中，则将返回的列表的开头加上之前所选的区域，并删除列表中末尾的
     *
     * @param cityId     城市id
     * @param limit      条数限制
     * @param districtId 需要包含的区域的id
     * @return n个区域
     */
    public static List<Area> findTopDistricts(String cityId, int limit,
                                              String districtId) {
        List<Area> districts = findTopDistricts(cityId, limit);
        if (StringUtils.isNotBlank(districtId) && !"0".equals(districtId)) {
            boolean containsSelectedDistrict = false;
            for (Area district : districts) {
                if (district.id.equals(districtId)) {
                    containsSelectedDistrict = true;
                    break;
                }
            }
            if (!containsSelectedDistrict) {
                List<Area> showDistricts = new ArrayList<>();
                showDistricts.add((Area) Area.findById(districtId));
                if (districts.size() == limit) {
                    districts.remove(limit - 1);
                }
                showDistricts.addAll(districts);
                districts = showDistricts;
            }
        }
        return districts;
    }

    /**
     * 返回包含指定商圈的前n个商圈.
     * 如果之前所选的商圈未在返回列表中，则将返回的列表的开头加上之前所选的商圈，并删除列表中末尾的
     *
     * @param districtId 区域id
     * @param limit      条数限制
     * @param areaId     需要包含的商圈的id
     * @return n个商圈
     */
    public static List<Area> findTopAreas(String districtId, int limit,
                                          String areaId) {
        List<Area> areas;
        if (GoodsCondition.isValidAreaId(districtId)) {
            areas = findTopAreas(districtId, limit);
        } else {
            areas = findTopAreas(limit);
        }
        if (GoodsCondition.isValidAreaId(areaId)) {
            boolean containsSelectedArea = false;
            for (Area area : areas) {
                if (area.id.equals(areaId)) {
                    containsSelectedArea = true;
                    break;
                }
            }
            if (!containsSelectedArea) {
                Area area = (Area) Area.findById(areaId);
                List<Area> showAreas = new ArrayList<>();
                showAreas.add(area);
                if (areas.size() == limit) {
                    areas.remove(limit - 1);
                }
                showAreas.addAll(areas);
                areas = showAreas;
            }
        }
        return areas;
    }

    public static Area findParent(String areaId) {
        Area area = findById(areaId);
        return area.parent;
    }
}
