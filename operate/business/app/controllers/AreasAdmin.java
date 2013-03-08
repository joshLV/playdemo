package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Area;
import models.sales.AreaType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-8
 * Time: 下午2:25
 */
@With(OperateRbac.class)
@ActiveNavigation("areas_index")
public class AreasAdmin extends Controller {


    public static void index(String parentId) {
        if (StringUtils.isBlank(parentId)) {
            parentId = null;
        }
        List<Area> areaList;
        AreaType areaType = getAreaType(parentId);
        if (parentId != null) {
            areaList = Area.find("deleted=? and areaType=? and parent.id=? order by displayOrder", DeletedStatus.UN_DELETED, areaType, parentId).fetch();
        } else {
            areaList = Area.find("deleted=? and areaType=? and parent is null order by displayOrder", DeletedStatus.UN_DELETED, areaType).fetch();
//            areaList = Area.find("deleted=? and areaType=? order by displayOrder", DeletedStatus.UN_DELETED, areaType).fetch();
        }
        render(areaList, parentId, areaType);

    }

    /**
     * 展示添加区域页面
     */
    public static void add(String parentId) {
        if (StringUtils.isBlank(parentId)) {
            parentId = null;
        }
        AreaType areaType = getAreaType(parentId);
        List<Area> areaList = Area.find("deleted=? and areaType=? order by displayOrder", DeletedStatus.UN_DELETED, areaType).fetch();
        render(areaList, parentId, areaType);
    }

    /**
     * 添加类别
     */
    public static void create(@Valid Area area, String parentId) {
        if (StringUtils.isBlank(parentId)) {
            parentId = null;
        }
        AreaType areaType = getAreaType(parentId);

        List<Area> areaList = Area.find("deleted=? and areaType=? order by displayOrder", DeletedStatus.UN_DELETED, areaType).fetch();

        if (Validation.hasErrors()) {
            renderInit(area);
            render("AreasAdmin/add.html", areaList);
        }
        Area parentArea = Area.find("deleted=? and id=?", DeletedStatus.UN_DELETED, parentId).first();
        area.parent = parentArea;
        area.deleted = DeletedStatus.UN_DELETED;
        area.create();
        area.save();
        if (area.parent != null) {
            index(area.parent.id);
        } else {
            index(null);
        }

    }

    /**
     * 取得指定区域信息
     */
    public static void edit(String id) {
        models.sales.Area area = models.sales.Area.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
        AreaType areaType = area.areaType;
        renderInit(area);
        render(id, area, areaType);
    }

    /**
     * 更新指定区域信息
     */
    public static void update(String id, @Valid final models.sales.Area area) {
        Area updateArea = Area.find("deleted=? and id=?", DeletedStatus.UN_DELETED, id).first();
        area.areaType = updateArea.areaType;
        if (Validation.hasErrors()) {
            renderInit(area);
            render("AreasAdmin/edit.html", area, id);
        }
        if (updateArea.parent != null) {
            models.sales.Area.update(id, area, updateArea.parent.id);
            index(updateArea.parent.id);
        } else {
            models.sales.Area.update(id, area, null);
            index(null);
        }
    }

    /**
     * 删除指定区域
     */
    public static void delete(String id) {
        models.sales.Area area = models.sales.Area.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
        List<Area> childAreaList = Area.find("parent=? and deleted = ?", area, DeletedStatus.UN_DELETED).fetch();

        if (childAreaList.size() > 0) {
            render(area);
        } else {
            models.sales.Area.delete(id);
            if (area.parent != null) {
                index(area.parent.id);
            } else {
                index(null);
            }
        }
    }

    /**
     * 初始化form界面.
     * 添加和修改页面共用
     */
    private static void renderInit(Area area) {
        if (area != null) {
            renderArgs.put("area.id", area.id);
            renderArgs.put("area.displayOrder", area.displayOrder);
            renderArgs.put("area.name", area.name);
            renderArgs.put("area.popularArea", area.popularArea);
        }
    }

    public static AreaType getAreaType(String parentId) {
        AreaType areaType = null;
        if (parentId == null) {
            areaType = AreaType.CITY;
        } else {
            Area area = Area.find("deleted=? and id=?", DeletedStatus.UN_DELETED, parentId).first();
            if (area.areaType == AreaType.DISTRICT) {
                areaType = AreaType.AREA;
            } else if (area.areaType == AreaType.CITY) {
                areaType = AreaType.DISTRICT;
            }
        }
        return areaType;
    }

}
