package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商品分类.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 12:53 PM
 */
@Entity
@Table(name = "categories")
public class Category extends Model {
    /**
     * 类目名称
     */
    public String name;
    /**
     * 推荐度,显示顺序
     */
    @Column(name = "display_order")
    public int displayOrder;

    /**
     * 所属分类Id
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    public Category parentCategory;

    /**
     * 商品标识.
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "goods_id"), joinColumns = @JoinColumn(name = "category_id"))
    public Set<Goods> goodsSet;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<CategoryProperty> properties = new HashSet<CategoryProperty>();

    public Category() {
    }

    public Category(long categoryId) {
        this.id = categoryId;
    }

    /**
     * 获取顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findTop(int limit) {
        return find("parent = null order by displayOrder").fetch(limit);
    }

    public static List<Category> findTop(int limit, long categoryId) {
        if (categoryId == 0) {
            return findTop(limit);
        }
        List<Category> categories = findByParent(limit, categoryId);
        boolean containsCategory = false;
        for (Category category : categories) {
            if (category.id == categoryId) {
                containsCategory = true;
                break;
            }
        }
        if (!containsCategory) {
            List<Category> showCategories = new ArrayList<>();
            showCategories.add((Category) findById(categoryId));
            if (categories.size() == limit) {
                categories.remove(limit - 1);
            }
            showCategories.addAll(categories);
            categories = showCategories;
        }
        return categories;
    }

    public static List<Category> findByParent(int limit, long categoryId) {
        Category category = categoryId == 0 ? null : new Category(categoryId);
        if (limit > 0) {
            if (category != null) {
                return find("parentCategory = ? order by displayOrder", category).fetch(limit);
            } else {
                return find("parentCategory = null order by displayOrder").fetch(limit);
            }
        } else {
            if (category != null) {
                return find("parentCategory = ? order by displayOrder", category).fetch();
            } else {
                return find("parentCategory = null order by displayOrder").fetch();
            }
        }
    }

    public static List<Category> findByParent(long categoryId) {
        return findByParent(0, categoryId);
    }
}
