package models.sales;

import cache.CacheCallBack;
import cache.CacheHelper;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;
import play.modules.solr.SolrEmbedded;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;

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
import javax.persistence.Transient;
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
@SolrSearchable
public class Category extends Model {

    private static final long serialVersionUID = 7114320609113062L;

    /**
     * 类目名称
     */
    @SolrField
    public String name;
    /**
     * 推荐度,显示顺序
     */
    @Column(name = "display_order")
    public int displayOrder;

    /**
     * SEO关键字.
     */
    @Column(name = "keywords")
    public String keywords;

    /**
     * 网站上显示的关键字.
     */
    @Column(name = "show_keywords")
    public String showKeywords;

    @SolrField
    public String tuan800name;

    /**
     * 所属分类Id
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @SolrField
    public Category parentCategory;

    /**
     * 商品标识.
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "goods_id"), joinColumns = @JoinColumn(name = "category_id"))
    public Set<Goods> goodsSet;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @SolrEmbedded
    public Set<CategoryProperty> properties = new HashSet<CategoryProperty>();

    @Transient
    public String[] getShowKeywordsList() {
        if (StringUtils.isNotBlank(showKeywords)) {
            return StringUtils.split(showKeywords, ",");
        }
        return null;
    }

    public Category() {
    }

    public Category(long categoryId) {
        this.id = categoryId;
    }

    public static final String CACHEKEY = "CATEGORY";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }

    /**
     * 获取顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findTop(int limit) {
        return find("parentCategory = null order by displayOrder").fetch(limit);
    }

    /**
     * 获取首页左边显示的顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findLeftTop(int limit) {
        //todo
        return find("parentCategory = null order by displayOrder").fetch(limit);
    }

    /**
     * 获取首页楼层显示的顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findFloorTop(int limit) {
        //todo
        return find("parentCategory = null order by displayOrder").fetch(limit);
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

    /**
     * 查询商品分类的前n个品牌.
     *
     * @param limit
     * @return
     */
    public List<Brand> getTopBrands(final int limit) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(Brand.CACHEKEY, "WWW_TOP_BRANDS" + id + "_" + limit), new CacheCallBack<List<Brand>>() {
            @Override
            public List<Brand> loadData() {
                return Goods.findBrandByCondition(new GoodsCondition(String.valueOf(id)), limit);
            }
        });

    }

    public List<Goods> getTopGoods(final int limit) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(Goods.CACHEKEY, "WWW_TOP_GOODS_BY_CATEGORY" + id + "_" + limit), new CacheCallBack<List<Goods>>() {
            @Override
            public List<Goods> loadData() {
                return Goods.findTopByCategory(id, limit, true);
            }
        });
    }
}
