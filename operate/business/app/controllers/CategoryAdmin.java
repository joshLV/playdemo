package controllers;

import com.uhuila.common.constants.DeletedStatus;
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
        Long parentId = null;
        render(categoryList, parentId);
    }

    /**
     * 展示添加类别页面
     */
    public static void add(Long parentId) {
        renderInit(null);
        Category parentCategory = null;
        if (parentId != null) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        render(parentCategory, parentId);
    }

    /**
     * 添加类别
     */
    @ActiveNavigation("goods_add")
    public static void create(@Valid Category category, Long parentId) {
        Category parentCategory = null;
        if (category != null && parentId != null && parentId != 0) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        if (Validation.hasErrors()) {
            renderInit(category);
            render("CategoryAdmin/add.html", parentId, parentCategory);
        }
        category.parentCategory = parentCategory;
        category.deleted = DeletedStatus.UN_DELETED;
        category.create();
        category.save();
        displaySubcategory(parentId);
    }

    /**
     * 取得指定类别信息
     */
    public static void edit(Long id, Long parentId) {
        models.sales.Category category = models.sales.Category.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
        Category parentCategory = null;
        if (parentId != null) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        renderInit(category);
        render(id, parentId, parentCategory);
    }

    /**
     * 更新指定类别信息
     */
    public static void update(Long id, @Valid final models.sales.Category category, Long parentId) {
        Category parentCategory = null;
        if (category != null && parentId != null && parentId != 0) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        if (Validation.hasErrors()) {
            renderInit(category);
            render("CategoryAdmin/edit.html", category, id, parentId, parentCategory);
        }
        models.sales.Category.update(id, category, parentId);
        displaySubcategory(parentId);
    }

    public static void displaySubcategory(Long parentId) {
        List<Category> categoryList = null;
        Category parentCategory = null;

        if (parentId != null) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        if (parentCategory != null) {
            categoryList = Category.find("parentCategory.id=? and deleted = ?", parentCategory.id, DeletedStatus.UN_DELETED).fetch();
        }
        if (parentId == null) {
            index(categoryList);
        }
        render(categoryList, parentId, parentCategory);
    }


    /**
     * 删除指定类别
     */
    public static void delete(Long id, Long parentId) {
        models.sales.Category category = models.sales.Category.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
        List<Category> childCategoryList = Category.find("parentCategory=? and deleted = ?", category, DeletedStatus.UN_DELETED).fetch();
        if (childCategoryList.size() > 0) {
            render(parentId);
        } else {
            models.sales.Category.delete(id);
            displaySubcategory(parentId);
        }
    }

    /**
     * 初始化form界面.
     * 添加和修改页面共用
     */
    private static void renderInit(Category category) {
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



