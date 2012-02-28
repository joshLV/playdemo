package models.sales;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.Model;

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
}
