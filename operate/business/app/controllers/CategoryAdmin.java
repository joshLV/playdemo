package controllers;

import models.sales.Category;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-25
 * Time: 上午10:14
 * To change this template use File | Settings | File Templates.
 */
@With(OperateRbac.class)
@ActiveNavigation("category_admin_index")
public class CategoryAdmin extends Controller {
    public static void index() {

        render();
    }

    /**
     * 展示添加类别页面
     */
    @ActiveNavigation("category_add")
    public static void add() {
        List<Category> categoryList = Category.findByParent(0);//获取顶层分类


        render();
    }

    /**
     * 添加类别
     * .
     */
    @ActiveNavigation("goods_add")
    public static void create() {

        index();
    }
}
