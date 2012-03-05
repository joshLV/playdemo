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

    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    public Set<Brand> brands;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<CategoryProperty> properties = new HashSet<CategoryProperty>();

    public Category() {
    }

    public Category(long categoryId) {
        this.id = categoryId;
    }

    /**
     * 获取前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findTop(int limit) {
        return find("order by displayOrder").fetch(limit);
    }

    public static List<Category> findTop(int limit, long categoryId) {
        List<Category> categories = findTop(limit);
        if (categoryId != 0) {
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
                    categories.remove(limit-1);
                }
                showCategories.addAll(categories);
                categories = showCategories;
            }
        }
        return categories;
    }
}
