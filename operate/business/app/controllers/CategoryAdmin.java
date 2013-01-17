package controllers;

import com.sun.org.apache.bcel.internal.generic.LNEG;
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
 * User: wangjia
 * Date: 12-10-25
 * Time: 上午10:14
 */
@With(OperateRbac.class)
@ActiveNavigation("category_admin_index")
public class CategoryAdmin extends Controller {
    public static void index(Long parentId) {
        Category parentCategory = null;
        List<Category> categoryList;
        if (parentId != null) {
            parentCategory = Category.find("deleted=? and id=?", DeletedStatus.UN_DELETED, parentId).first();
            categoryList = Category.find("deleted=? and parentCategory.id=?", DeletedStatus.UN_DELETED, parentId).fetch();
        } else {
            categoryList = Category.findByParent(0);//获取顶层分类
        }
        render(categoryList, parentCategory);
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

    public static void create(@Valid Category category, Long parentId) {
        Category parentCategory = null;
        if (category.parentCategory != null) {
            parentId = category.parentCategory.id;
        }
        if (parentId != null) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        if (Validation.hasErrors()) {
            renderInit(category);
            render("CategoryAdmin/add.html", parentCategory);
        }
        category.parentCategory = parentCategory;
        category.deleted = DeletedStatus.UN_DELETED;
        category.create();
        category.save();
        index(parentId);
    }

    /**
     * 取得指定类别信息
     */
    public static void edit(Long id) {
        models.sales.Category category = models.sales.Category.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
        Category parentCategory = null;
        Long parentId = null;
        if (category.parentCategory != null) {
            parentId = category.parentCategory.id;
        }
        if (parentId != null) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        renderInit(category);
        render(id, parentCategory);
    }

    /**
     * 更新指定类别信息
     */
    public static void update(Long id, @Valid final models.sales.Category category) {
        Category parentCategory = null;
        Category currentCategory = Category.findById(id);
        Long parentId = null;
        if (currentCategory.parentCategory != null) {
            parentId = currentCategory.parentCategory.id;
        }
        if (parentId != null) {
            parentCategory = Category.find("id=? and deleted=?", parentId, DeletedStatus.UN_DELETED).first();
        }
        if (Validation.hasErrors()) {
            renderInit(category);
            render("CategoryAdmin/edit.html", category, id, parentCategory);
        }
        models.sales.Category.update(id, category, parentId);
        index(parentId);
    }


    /**
     * 删除指定类别
     */
    public static void delete(Long id) {
        models.sales.Category category = models.sales.Category.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
        Long parentId = null;
        if (category.parentCategory != null) {
            parentId = category.parentCategory.id;
        }
        List<Category> childCategoryList = Category.find("parentCategory=? and deleted = ?", category, DeletedStatus.UN_DELETED).fetch();
        Category parentCategory = category.parentCategory;
        if (childCategoryList.size() > 0) {
            render(parentCategory);
        } else {
            models.sales.Category.delete(id);
            index(parentId);
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



