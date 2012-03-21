/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.ImageSize;
import com.uhuila.common.util.PathUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.validation.InFuture;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "goods")
public class Goods extends Model {

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhlcdndev.net");
    //    private static final String IMAGE_ROOT_GENERATED = Play.configuration
//            .getProperty("image.root", "/p");
    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    static {
        //增加可信标签到白名单
        HTML_WHITE_TAGS.addTags("embed", "object", "param", "span", "div");
        //增加可信属性
        HTML_WHITE_TAGS.addAttributes(":all", "style", "class", "id", "name");
        HTML_WHITE_TAGS.addAttributes("object", "width", "height", "classid", "codebase");
        HTML_WHITE_TAGS.addAttributes("param", "name", "value");
        HTML_WHITE_TAGS.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen", "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
    }

    /**
     * 商品编号
     */
    @MaxSize(value = 30)
    public String no;
    /**
     * 商品名称
     */
    @Required
    @MaxSize(value = 80)
    public String name;
    /**
     * 所属商户ID
     */
    @Column(name = "company_id")
    public Long companyId;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name
            = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
    public Set<Shop> shops;

    public void filterShops() {
        if (shops == null) {
            List<Shop> shopList = Shop.findShopByCompany(companyId);
            shops = new HashSet<>();
            shops.addAll(shopList);
            return;
        }
        Set<Shop> uniqueShops = new HashSet<>();
        for (Shop shop : shops) {
            if (!uniqueShops.contains(shop)) {
                uniqueShops.add(shop);
            }
        }
        this.shops = uniqueShops;
    }

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "category_id"), joinColumns = @JoinColumn(name = "goods_id"))
    @Required
    public Set<Category> categories;

    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

    /**
     * 最小规格图片路径
     */
    @Transient
    public String getImageTinyPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, ImageSize.TINY);
    }

    /**
     * 小规格图片路径
     */
    @Transient
    public String getImageSmallPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, ImageSize.SMALL);
    }

    /**
     * 中等规格图片路径
     */
    @Transient
    public String getImageMiddlePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, ImageSize.MIDDLE);
    }

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, ImageSize.LARGE);
    }

    /**
     * 进货量
     */
    @Column(name = "income_goods_count")
    public Long incomeGoodsCount;
    /**
     * 券有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.DATE)
    public Date effectiveAt;
    /**
     * 券有效结束日
     */
    @Required
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.DATE)
    public Date expireAt;
    /**
     * 商品标题
     */
    //    public String title;
    /**
     * 商品原价
     */
    @Required
    @Min(value = 0.01)
    @Column(name = "original_price")
    public BigDecimal originalPrice;
    /**
     * 商品现价
     */
    @Required
    @Min(value = 0.01)
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    private Integer discount;

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    @Column(name = "discount")
    public Integer getDiscount() {
        if (discount != null && discount > 0) {
            return discount;
        }
        if (originalPrice != null && salePrice != null && originalPrice.compareTo(new BigDecimal(0)) > 0) {
            this.discount = salePrice.divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
        } else {
            this.discount = 0;
        }
        return discount;
    }

    @Transient
    public String getDiscountExpress() {
        int discount = getDiscount();
        if (discount >= 100 || discount <= 0) {
            return "";
        }
        if (discount < 10) {
            return String.valueOf(discount / 10.0);
        }
        if (discount % 10 == 0) {
            return String.valueOf(discount / 10);
        }
        return String.valueOf(discount);
    }

    /**
     * 温馨提示
     */
    @MaxSize(value = 65535)
    private String prompt;

    public String getPrompt() {
        if (StringUtils.isBlank(prompt)) {
            return "";
        }
        return Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    public void setPrompt(String prompt) {
        this.prompt = Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    @Required
    @MaxSize(value = 65535)
    private String details;

    /**
     * 商品详情
     *
     * @return
     */
    public String getDetails() {
        if (StringUtils.isBlank(details)) {
            return "";
        }
        return Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    public void setDetails(String details) {
        this.details = Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public int saleCount;
    /**
     * 售出基数
     */
    @Required
    @Min(value = 0)
    @Column(name = "base_sale")
    public Long baseSale;
    /**
     * 商品状态,
     */
    @Enumerated(EnumType.STRING)
    public GoodsStatus status;
    /**
     * 创建来源
     */
    @Column(name = "created_from")
    public String createdFrom;
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;
    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;
    /**
     * 修改人
     */
    @Column(name = "updated_by")
    public String updatedBy;
    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;
    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    /**
     * 手工排序
     */
    @Column(name = "display_order")
    public String displayOrder;

    @Required
    @ManyToOne
    @JoinColumn(name = "brand_id")
    public Brand brand;

    @Transient
    public String salePriceBegin;
    @Transient
    public String salePriceEnd;
    @Transient
    public int saleCountBegin = -1;
    @Transient
    public int saleCountEnd = -1;
    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTop(int limit) {
        return find("status=? and deleted=? order by createdAt DESC",
                GoodsStatus.ONSALE,
                DeletedStatus.UN_DELETED).fetch(limit);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopByCategory(long categoryId, int limit) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select g from Goods g where g.status=:status and g.deleted=:deleted " +
                "and g.id in (select g.id from g.categories c where c.id = :categoryId) " +
                "order by g.updatedAt, g.createdAt DESC");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("categoryId", categoryId);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public static Goods findUnDeletedById(long id) {
        return find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
    }

    public static JPAExtPaginator<Goods> findByCondition(GoodsCondition condition,
                                                         int pageNumber, int pageSize) {
        JPAExtPaginator<Goods> goodsPage = new JPAExtPaginator<>
                ("Goods g", "g", Goods.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);
        return goodsPage;
    }

    public static void delete(Long... ids) {
        for (Long id : ids) {
            models.sales.Goods goods = models.sales.Goods.findById(id);
            if (goods != null) {
                goods.deleted = DeletedStatus.DELETED;
                goods.save();
            }
        }
    }

    public static void updateStatus(GoodsStatus status, Long... ids) {
        for (Long id : ids) {
            models.sales.Goods goods = models.sales.Goods.findById(id);
            goods.status = status;
            goods.save();
        }
    }

}