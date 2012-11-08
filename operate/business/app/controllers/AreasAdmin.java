package controllers;

import models.sales.Area;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-8
 * Time: 下午2:25
 * To change this template use File | Settings | File Templates.
 */
@With(OperateRbac.class)
@ActiveNavigation("areas_index")
public class AreasAdmin  extends Controller {
    public static void index(Long parentId) {
        Area
        List<Area> areaList;
        if (parentId != null) {
            parentCategory = Category.find("deleted=? and id=?", DeletedStatus.UN_DELETED, parentId).first();
            categoryList = Category.find("deleted=? and parentCategory.id=?", DeletedStatus.UN_DELETED, parentId).fetch();
        } else {
            categoryList = Category.findByParent(0);//获取顶层分类
        }
        render(categoryList, parentCategory);
    }
}
