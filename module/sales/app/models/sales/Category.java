package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.HashSet;
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
    public Set<CategoryProperty> properties = new HashSet<>();
}
