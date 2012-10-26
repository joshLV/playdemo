package controllers;

import models.sales.Category;
import operate.rbac.annotations.ActiveNavigation;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
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
    public static void index(List<Category> categoryList) {
        if (categoryList == null || categoryList.size() < 0) {
            categoryList = Category.findByParent(0);//获取顶层分类
        }
        render(categoryList);
    }

    /**
     * 展示添加类别页面
     */
    public static void add(Long parentId) {
        renderInit(null);
        Category parentCategory = null;
        if (parentId != null) {
            parentCategory = Category.findById(parentId);
        }
        render(parentCategory, parentId);
    }

    /**
     * 添加类别
     * .
     */
    @ActiveNavigation("goods_add")
    public static void create(@Valid Category category, Long parentId) {
        if (Validation.hasErrors()) {
            renderInit(category);
            render("CategoryAdmin/add.html");
        }
        Category parentCategory = null;
        if (category != null && parentId != null && parentId != 0) {
            parentCategory = Category.findById(parentId);
        }
        category.parentCategory = parentCategory;
        category.create();
        category.save();
        index(null);
    }

    /**
     * 取得指定总类别信息
     */
    public static void edit(Long id) {
        models.sales.Category category = models.sales.Category.findById(id);
        renderInit(category);
        render(id);
    }

    /**
     * 更新指定总类别信息
     */
    public static void update(Long id, @Valid final models.sales.Category category) {
        if (Validation.hasErrors()) {
            renderInit(category);
            render("CategoryAdmin/add.html", category, id);
        }
        models.sales.Category.update(id, category);
        index(null);
    }

    public static void displaySubcategory(Long parentId) {
        List<Category> categoryList = null;
        Category parentCategory = Category.findById(parentId);
        if (parentCategory != null) {
            categoryList = Category.find("parentCategory.id=?", parentCategory.id).fetch();
        }
        render(categoryList, parentId);
    }


    /**
     * 初始化form界面.
     * 添加和修改页面共用
     */
    private static void renderInit(Category category) {
        if (category != null && category.parentCategory != null) {
            renderArgs.put("category.parentId", category.parentCategory.id);
            renderArgs.put("category.parentName", category.parentCategory.name);
        } else {
            renderArgs.put("category.parentId", null);
            renderArgs.put("category.parentName", "无");
        }
        if (category != null) {
            renderArgs.put("category.name", category.name);
            renderArgs.put("category.displayOrder", category.displayOrder);
            renderArgs.put("category.isInWWWLeft", category.isInWWWLeft);
            renderArgs.put("category.isInWWWFloor", category.isInWWWFloor);
            renderArgs.put("category.display", category.display);
            renderArgs.put("category", category);
        }
    }
}



