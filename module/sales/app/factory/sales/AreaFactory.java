package factory.sales;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import factory.ModelFactory;
import models.sales.Area;
import models.sales.AreaType;

/**
 * 地区测试对象.
 *
 * User: wangjia
 * Date: 12-8-23
 * Time: 上午11:52
 */
public class AreaFactory extends ModelFactory<Area> {
    @Override
    public Area define() {
        return buildOrFindArea(AreaType.AREA, "0210201", "徐家汇", getXihuiDist());
    }

    /**
     * 建立上海市.
     *
     * @return
     */
    public Area getShanghaiCity() {
        return createOrFindArea(AreaType.CITY, "021", "上海市");
    }

    /**
     * 建立徐汇区
     *
     * @return
     */
    public Area getXihuiDist() {
        return createOrFindArea(AreaType.DISTRICT, "02102", "徐汇区", getShanghaiCity());
    }

    /**
     * 建立浦东新区
     *
     * @return
     */
    public Area getPudongDist() {
        return createOrFindArea(AreaType.DISTRICT, "02103", "浦东新区", getShanghaiCity());
    }

    private Area createOrFindArea(AreaType areaType, String id, String name) {
        return createOrFindArea(areaType, id, name, null);
    }


    public Area createOrFindArea(AreaType areaType, String id, String name, Area parent) {
        Area area = buildOrFindArea(areaType, id, name, parent);
        area.save();
        return area;
    }

    public Area buildOrFindArea(AreaType areaType, String id, String name, Area parent) {
        Area area = Area.findById(id);
        if (area != null) {
            return area;
        }

        area = new Area();
        area.id = id;
        area.name = name;
        area.displayOrder = FactoryBoy.sequence(Area.class);
        area.areaType = areaType;
        area.parent = parent;
        area.deleted = DeletedStatus.UN_DELETED;
        return area;
    }
}
