package models.sales;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.modules.solr.SolrEmbedded;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;

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
@SolrSearchable
public class Category extends Model {

    private static final long serialVersionUID = 7114320609113062L;

    /**
     * 类目名称
     */
//    @Unique
    @Required
    @SolrField
    public String name;

    /**
     * 推荐度,显示顺序
     */
    @Required
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

    /**
     * 是否在WWW首页左上角显示
     */
    @Column(name = "is_in_www_left")
    public Boolean isInWWWLeft = false;

    /**
     * 是否在WWW首页楼层显示
     */
    @Column(name = "is_in_www_floor")
    public Boolean isInWWWFloor = false;

    /**
     * 是否显示在WWW首页.
     */
    public Boolean display = false;

    /**
     * 所属分类Id
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @SolrField
    public Category parentCategory;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;


    /**
     * 商品标识.
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "goods_id"), joinColumns = @JoinColumn(name = "category_id"))
    public Set<Goods> goodsSet;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @SolrEmbedded
    public Set<CategoryProperty> properties = new HashSet<>();

    public Object[] getShowKeywordsList(int limit) {
        if (StringUtils.isNotBlank(showKeywords)) {
            System.out.println("showKeywords:" + showKeywords);
            return ArrayUtils.subarray(StringUtils.split(showKeywords, ","), 0, limit);
        }
        return new Object[0];
    }

    /**
     * 该分类包含的商品数量.
     * 用于在首页和商品列表页显示
     */
    @Transient
    public int goodsCount;

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

    //-------------------------------------- 数据库操作 ------------------------------------

    /**
     * 获取顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findTop(int limit) {
        return find("parentCategory = null and display = true order by displayOrder").fetch(limit);
    }

    /**
     * 获取首页左边显示的顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findLeftTop(int limit) {
        return find("parentCategory = null and isInWWWLeft = true and display = true order by displayOrder").fetch(limit);
    }

    /**
     * 获取首页楼层显示的顶层的前n个分类.
     *
     * @param limit 获取的条数限制
     * @return 前n个分类
     */
    public static List<Category> findFloorTop(int limit) {
        return find("parentCategory = null and isInWWWFloor = true and display = true  order by displayOrder").fetch(limit);
    }

    public static List<Category> findTop(int limit, long categoryId) {
        if (categoryId == 0) {
            return findTop(limit);
        }
        List<Category> categories = findByParent(limit, categoryId, true);
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

    public static List<Category> findByParent(int limit, long categoryId, Boolean display) {
        Category category = categoryId == 0 ? null : new Category(categoryId);
        JPAQuery query;
        if (category == null) {
            if (display != null) {
                query = find("parentCategory = null and display = ? and deleted = ? order by displayOrder", display, DeletedStatus.UN_DELETED);
            } else {
                query = find("parentCategory = null and deleted = ? order by displayOrder", DeletedStatus.UN_DELETED);
            }
        } else {
            if (display != null) {
                query = find("parentCategory = ? and display = ? and deleted = ? order by displayOrder", category, display, DeletedStatus.UN_DELETED);
            } else {
                query = find("parentCategory = ? and deleted = ? order by displayOrder", category, DeletedStatus.UN_DELETED);
            }
        }

        if (limit > 0) {
            return query.fetch(limit);
        } else {
            return query.fetch();
        }
    }

    public static List<Category> findByParent(long categoryId) {
        return findByParent(0, categoryId, null);
    }

    /**
     * 查询商品分类的前n个品牌.
     * WWW首页显示分类楼层中的品牌时调用
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

    /**
     * 获取分类中的前n个商品.
     * WWW首页显示分类楼层中的商品时调用
     *
     * @param limit
     * @return
     */
    public List<Goods> getTopGoods(final int limit) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(Goods.CACHEKEY, "WWW_TOP_GOODS_BY_CATEGORY" + id + "_" + limit), new CacheCallBack<List<Goods>>() {
            @Override
            public List<Goods> loadData() {
                return Goods.findTopByCategory(id, limit, true);
            }
        });
    }

    /**
     * 获取指定分类的所有子分类，分类中需要标明商品数量.
     *
     * @param categoryId
     * @return
     */
    public static List<Category> getCategoriesBy(final long categoryId) {
        return CacheHelper.getCache(CacheHelper.getCacheKey(Category.CACHEKEY, "WWW_SUB_CATEGORIES" + categoryId), new CacheCallBack<List<Category>>() {
            @Override
            public List<Category> loadData() {
                List<Category> categoryList = Category.findByParent(categoryId);
                for (Category category : categoryList) {
//                    category.goodsCount =
                }
                return categoryList;
            }
        });
    }

    public static void update(Long id, Category category, Long parentId) {
        models.sales.Category updateCategory = models.sales.Category.findById(id);
        if (updateCategory == null) {
            return;
        }
        updateCategory.name = category.name;
        updateCategory.displayOrder = category.displayOrder;
        updateCategory.keywords = category.keywords;
        updateCategory.showKeywords = category.showKeywords;
        updateCategory.isInWWWLeft = category.isInWWWLeft;
        updateCategory.isInWWWFloor = category.isInWWWFloor;
        updateCategory.display = category.display;
        Category parentCategory = null;
        if (category != null && parentId != null && parentId != 0) {
            parentCategory = Category.findById(parentId);
        }
        updateCategory.parentCategory = parentCategory;
        updateCategory.save();
    }

    public static void delete(Long id) {
        models.sales.Category category = models.sales.Category.findById(id);
        if (category != null) {
            category.deleted = DeletedStatus.DELETED;
            category.save();
        }
    }
}



