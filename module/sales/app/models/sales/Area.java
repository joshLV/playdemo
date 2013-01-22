package models.sales;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
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
import javax.persistence.Transient;
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
    public static String SHANGHAI = "021";

    private static final long serialVersionUID = 706109123113062L;

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

    /**
     * 是否是热门商圈
     */
    @Column(name = "popular_area")
    public Boolean popularArea = Boolean.FALSE;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_type")
    public AreaType areaType;

    @Transient
    public long goodsCount;

    public Area() {
    }

    public Area(String id) {
        this.id = id;
    }

    public static boolean isArea(String areaId) {
        return (StringUtils.isNotBlank(areaId) && areaId.length() == 8);
    }

    public static boolean isDistrict(String district) {
        return (StringUtils.isNotBlank(district) && district.length() == 5);
    }

    public static final String CACHEKEY = "AREA";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
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

    /**
     * 获取指定城市的前n个商圈，主要用于主页上的显示
     *
     * @param limit 获取的条数限制
     * @return 前n个商圈
     */
    public static List<Area> findTopAreas(int limit, String cityId) {
        return find("deleted=? and areaType=? and popularArea=? and substring(id,1,3)=? order by displayOrder",
                DeletedStatus.UN_DELETED, AreaType.AREA, true, cityId).fetch(limit);
    }

    private static List<Area> findTopByAreaType(int limit, AreaType type) {
        return find("deleted=? and areaType=? order by displayOrder",
                DeletedStatus.UN_DELETED, type).fetch(limit);
    }

    /**
     * 获取所有子区域,
     *
     * @param areaId 区域id，如果是区域，则返回所有的商圈，如果是城市，则返回所有的区域
     * @return 所有子区域
     */
    public static List<Area> findAllSubAreas(String areaId) {
        if (areaId == null || "".equals(areaId)) {
            return find("deleted=? and areaType=? order by displayOrder",
                    DeletedStatus.UN_DELETED, AreaType.CITY).fetch();
        }
        return find("deleted=? and parent=? order by displayOrder",
                DeletedStatus.UN_DELETED, new Area(areaId)).fetch();
    }

    /**
     * 根据区域获取前n个商圈,或根据城市获取前n个区域.
     *
     * @param limit 获取的条数限制
     */
    public static List<Area> findTopAreas(String areaId, int limit) {
        return find("deleted=? and parent=? order by displayOrder",
                DeletedStatus.UN_DELETED, new Area(areaId)).fetch(limit);
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
        if (StringUtils.isEmpty(areaId)) {
            return null;
        }
        Area area = findById(areaId);
        if (area == null) {
            return null;
        }
        return area.parent;
    }

    public static Area findAreaById(final String id) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(Area.CACHEKEY, "AREA_BY_ID" + id), new CacheCallBack<Area>() {
            @Override
            public Area loadData() {
                return Area.findById(id);
            }
        });
    }

    public static void update(String id, Area area, String parentId) {
        models.sales.Area updateArea = models.sales.Area.findById(id);
        if (updateArea == null) {
            return;
        }
        if (parentId != null) {
            updateArea.parent = Area.find("deleted=? and id=? ", DeletedStatus.UN_DELETED, parentId).first();
        }
        updateArea.areaType = area.areaType;
        updateArea.displayOrder = area.displayOrder;
        updateArea.name = area.name;
        updateArea.popularArea = area.popularArea;
        updateArea.save();
    }

    public static void delete(String id) {
        models.sales.Area area = models.sales.Area.find("deleted=? and id=?", DeletedStatus.UN_DELETED, id).first();
        if (area != null) {
            area.deleted = DeletedStatus.DELETED;
            area.save();
        }
    }

    public List<Area> undeletedChildren() {
        return Area.find("parent = ? and  ( deleted = ? or deleted is null)",
                this, DeletedStatus.UN_DELETED).fetch();
    }

    public boolean isBelongTo(String cityId) {
        return id != null && id.startsWith(SHANGHAI);
    }
}
