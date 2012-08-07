/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.resale.ResalerLevel;
import models.supplier.Supplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.validation.InFuture;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Money;
import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.PathUtil;

@Entity
@Table(name = "goods")
public class Goods extends Model {
    private static final long serialVersionUID = 7063232063912330652L;

    public static final String PREVIEW_IMG_ROOT = "/9999/9999/9999/";

    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";

    //  ========= 不同的价格列表 =======
    /**
     * 商户填写的商品市场价
     */
    @Required
    @Min(0.01)
    @Max(999999)
    @Money
    @Column(name = "face_value")
    public BigDecimal faceValue;

    /**
     * 商户填写的进货价，采购价
     */
    @Required
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "original_price")
    public BigDecimal originalPrice;


    /**
     * 运营人员填写的一百券网站价格
     */
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, fetch = FetchType.LAZY,
            mappedBy = "goods")
    @OrderBy("level")
    public List<GoodsLevelPrice> levelPrices;

    //  ======  价格列表结束 ==========

    /**
     * 商品编号
     */
    @MaxSize(30)
    public String no;
    /**
     * 商品名称
     */
    @Required
    @MaxSize(60)
    public String name;

    /**
     * 商品标题（短信发送用）
     */
    @Required
    @MaxSize(60)
    public String title;
    /**
     * 所属商户ID
     */
    @Column(name = "supplier_id")
    public Long supplierId;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name
            = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
    public Set<Shop> shops;

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
     * 进货量
     */
    @Column(name = "income_goods_count")
    public Long incomeGoodsCount;
    /**
     * 券有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;
    /**
     * 券有效结束日
     */
    @Required
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    @Column(name = "use_begin_time")
    public String useBeginTime;

    @Column(name = "use_end_time")
    public String useEndTime;


    /**
     * 商品标题
     */
    //    public String title;

    private BigDecimal discount;

    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    private String details;

    /**
     * 温馨提示
     */
    @MaxSize(65000)
    @Lob
    private String prompt;

    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public int saleCount;
    /**
     * 售出基数
     */
    @Required
    @Min(0)
    @Max(999999)
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
     * 最早上架时间
     */
    @Column(name = "first_onsale_at")
    public Date firstOnSaleAt;
    
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

    /**
     * 限购数量
     * 
     */
    @Column(name = "limit_number")
    public Integer limitNumber = 0;

    /**
     * 推荐指数.
     */
    public Integer recommend = 0;

    /**
     * 优先指数.
     */
    public Integer priority = 0;
    /**
     * 收藏指数.
     */
    public Integer favorite = 0;


    @Transient
    public String salePriceBegin;
    @Transient
    public String salePriceEnd;
    @Transient
    public int saleCountBegin = -1;
    @Transient
    public int saleCountEnd = -1;
    @Transient
    public GoodsStatistics statistics;

    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    /**
     * SEO关键字.
     */
    @Column(name = "keywords")
    public String keywords;

    /**
     * 不允许发布的电子商务网站.
     * 设置后将不允许自动发布到这些电子商务网站上
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "goods")
    public Set<GoodsUnPublishedPlatform> unPublishedPlatforms;

    public static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");
    //    private static final String IMAGE_ROOT_GENERATED = Play.configuration
    //            .getProperty("image.root", "/p");
    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    static {
        //增加可信标签到白名单
        HTML_WHITE_TAGS.addTags("embed", "object", "param", "span", "div", "table", "tbody", "tr", "td",
                "background-color", "width", "figure", "figcaption", "strong", "p", "dl", "dt", "dd");
        //增加可信属性
        HTML_WHITE_TAGS.addAttributes(":all", "style", "class", "id", "name");
        HTML_WHITE_TAGS.addAttributes("table", "style", "cellpadding", "cellspacing", "border", "bordercolor", "align");
        HTML_WHITE_TAGS.addAttributes("span", "style", "border", "align");
        HTML_WHITE_TAGS.addAttributes("object", "width", "height", "classid", "codebase");
        HTML_WHITE_TAGS.addAttributes("param", "name", "value");
        HTML_WHITE_TAGS.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen",
                "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
    }


    /**
     * 获取商品所属的商户信息.
     *
     * @return
     */
    @Transient
    public Supplier getSupplier() {
        if (supplierId == null) {
            return null;
        }
        return CacheHelper.getCache(CacheHelper.getCacheKey(Supplier.CACHEKEY + this.supplierId, "GOODS_SUPPLIER"), new CacheCallBack<Supplier>() {
            @Override
            public Supplier loadData() {
                return Supplier.findById(supplierId);
            }
        });
    }

    @Column(name = "is_all_shop")
    public Boolean isAllShop = true;

    @Transient
    public Long topCategoryId;

    /**
     * 是否抽奖商品
     */
    @Column(name = "is_lottery")
    public Boolean isLottery = false;

    @Transient
    public boolean skipUpdateCache = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id")
    public List<ResalerFav> resalerFavs;

    /**
     * 商品详情
     *
     * @return
     */
    public String getDetails() {
        if (StringUtils.isBlank(details) || "<br />".equals(details)) {
            return "";
        }
        return Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    public void setDetails(String details) {
        this.details = Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    public void setDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) >= 0 && discount.compareTo(BigDecimal.TEN) <= 0) {
            this.discount = discount;
        } else if (discount.compareTo(BigDecimal.ZERO) < 0) {
            this.discount = BigDecimal.ZERO;
        } else if (discount.compareTo(BigDecimal.TEN) > 0) {
            this.discount = BigDecimal.TEN;
        }
    }

    @Column(name = "discount")
    public BigDecimal getDiscount() {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            return discount;
        }
        if (faceValue != null && salePrice != null && faceValue.compareTo(BigDecimal.ZERO) > 0) {
            this.discount = salePrice.divide(faceValue, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.TEN);
            if (this.discount.compareTo(BigDecimal.TEN) >= 0) {
                this.discount = BigDecimal.TEN;
            }
        } else {
            this.discount = BigDecimal.ZERO;
        }
        return discount;
    }

    @Transient
    public String getDiscountExpress() {
        BigDecimal discount = getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return "0折";
        }
        if (discount.compareTo(BigDecimal.TEN) >= 0) {
            return "无优惠";
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return "";

        }
        DecimalFormat format = new DecimalFormat("#.#");
        return format.format(discount.doubleValue()) + "折";
    }

    @Transient
    public String getDiscountExpress1() {
        BigDecimal discount = getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        if (discount.compareTo(BigDecimal.TEN) >= 0) {
            return "";
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return "";

        }
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(discount.doubleValue());
    }

    public void setLevelPrices(List<GoodsLevelPrice> levelPrices) {
        this.levelPrices = levelPrices;
        safeSetLevelPrices();
    }

    public void setLevelPrices(BigDecimal[] prices) {
        if (prices == null || prices.length == 0) {
            return;
        }
        safeSetLevelPrices();
        for (int i = 0; i < prices.length; i++) {
            levelPrices.get(i).price = prices[i];
        }
    }

    /**
     * 不同分销商等级所对应的价格, 此方法可确保返回的价格数量与分销等级的数量相同
     */
    public List<GoodsLevelPrice> safeGetLevelPrices() {
        if (levelPrices != null && levelPrices.size() == ResalerLevel.values().length) {
            return levelPrices;
        }
        safeSetLevelPrices();
        return levelPrices;
    }

    private void safeSetLevelPrices() {
        if (levelPrices == null) {
            levelPrices = new ArrayList<>();
        }
        if (levelPrices.size() == 0) {
            for (ResalerLevel level : ResalerLevel.values()) {
                GoodsLevelPrice levelPrice = new GoodsLevelPrice(this, level, BigDecimal.ZERO);
                levelPrices.add(levelPrice);
            }
        }
//        else {
//            for (GoodsLevelPrice levelPrice : levelPrices) {
//                if (levelPrice.price == null) {
//                    levelPrice.price = BigDecimal.ZERO;
//                }
//            }
//        }

        if (levelPrices.size() < ResalerLevel.values().length) {
            int zeroLevelCount = ResalerLevel.values().length - levelPrices.size();
            int originalSize = levelPrices.size();
            for (int i = 0; i < zeroLevelCount; i++) {
                levelPrices.add(new GoodsLevelPrice(this, ResalerLevel.values()[i + originalSize], BigDecimal.ZERO));
            }
        }
    }

    /**
     * 最小规格图片路径
     */
    @Transient
    public String getImageTinyPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_TINY);
    }

    /**
     * 小规格图片路径
     */
    @Transient
    public String getImageSmallPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_SMALL);
    }


    /**
     * 中等规格图片路径
     */
    @Transient
    public String getImageMiddlePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_MIDDLE);
    }

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_LARGE);
    }

    @Transient
    public String getImageOriginalPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_ORIGINAL);
    }

    public String getPrompt() {
        /*if (id!= null && id.intValue() == 110) {
            System.out.println("get prompt:" + prompt);
        }*/
        if (StringUtils.isBlank(prompt)) {
            return "";
        }
        return Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    public void setPrompt(String prompt) {
        /*if (id!= null && id.intValue() == 110) {
            System.out.println("set prompt:" + prompt);
        }*/
        this.prompt = Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    /**
     * 获取商品允许发布的电子商务平T台.
     *
     * @return
     */
    @Transient
    public List<GoodsPublishedPlatformType> getPublishedPlatforms() {
        List<GoodsPublishedPlatformType> publishedPlatforms = new ArrayList<>();
        for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
            if (!containsUnPublishedPlatform(type)) {
                publishedPlatforms.add(type);
            }
        }
        return publishedPlatforms;
    }

    public boolean containsUnPublishedPlatform(GoodsPublishedPlatformType type) {
        if (unPublishedPlatforms == null) {
            return true;
        }

        for (GoodsUnPublishedPlatform unPublishedPlatform : unPublishedPlatforms) {
            if (unPublishedPlatform != null && unPublishedPlatform.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public GoodsStatus getStatus() {
        if (status != null && GoodsStatus.ONSALE.equals(status) &&
                (expireAt != null && expireAt.before(new Date())) || (baseSale != null && baseSale <= 0)) {
            status = GoodsStatus.OFFSALE;
        }
        return status;
    }

    /**
     * @return
     */
    @Transient
    public boolean isExpired() {
        return expireAt != null && expireAt.before(new Date());
    }


    //=================================================== 数据库操作 ====================================================

    @Override
    public boolean create() {
        deleted = DeletedStatus.UN_DELETED;
        saleCount = 0;
        incomeGoodsCount = 0L;
        createdAt = new Date();
        if (isAllShop) {
            shops = null;
        }
        expireAt = DateUtil.getEndOfDay(expireAt);
        if (unPublishedPlatforms == null) {
            unPublishedPlatforms = new HashSet<>();
        }
        return super.create();
    }

    public static void update(Long id, Goods goods, boolean noLevelPrices) {
        models.sales.Goods updateGoods = models.sales.Goods.findById(id);
        if (updateGoods == null) {
            return;
        }
        updateGoods.name = goods.name;

        updateGoods.no = goods.no;
        updateGoods.effectiveAt = goods.effectiveAt;
        updateGoods.expireAt = DateUtil.getEndOfDay(goods.expireAt);
        updateGoods.faceValue = goods.faceValue;
        updateGoods.originalPrice = goods.originalPrice;
        goods.discount = null;
        updateGoods.setDiscount(goods.getDiscount());
        updateGoods.salePrice = goods.salePrice;
        updateGoods.baseSale = goods.baseSale;
        System.out.println(goods.materialType+">>>>>>>>>>>>>>>>>>>");
        updateGoods.materialType = goods.materialType;
        updateGoods.topCategoryId = goods.topCategoryId;
        updateGoods.categories = goods.categories;
        updateGoods.setPrompt(goods.getPrompt());
        updateGoods.setDetails(goods.getDetails());
        updateGoods.updatedAt = new Date();
        updateGoods.updatedBy = goods.updatedBy;
        updateGoods.brand = goods.brand;
        updateGoods.isAllShop = goods.isAllShop;
        updateGoods.status = goods.status;
        updateGoods.keywords = goods.keywords;
        updateGoods.limitNumber = goods.limitNumber;
        if (!StringUtils.isEmpty(goods.imagePath)) {
            updateGoods.imagePath = goods.imagePath;
        }
        if (goods.supplierId != null) {
            updateGoods.supplierId = goods.supplierId;
        }
        updateGoods.shops = goods.shops;
        updateGoods.title = goods.title;
        updateGoods.setPublishedPlatforms(goods.getPublishedPlatforms());

        updateGoods.useBeginTime = goods.useBeginTime;
        updateGoods.useEndTime = goods.useEndTime;
        if (!noLevelPrices) {
            for (int i = 0; i < goods.levelPrices.size(); i++) {
                updateGoods.safeGetLevelPrices().get(i).price = goods.levelPrices.get(i).price;
            }
        }
        updateGoods.isLottery = goods.isLottery;
        updateGoods.save();
    }


    public static final String CACHEKEY = "SALES_GOODS";

    public static final String CACHEKEY_BASEID = "SALES_GOODS_ID";

    @Override
    public void _save() {
        if (!this.skipUpdateCache) {
            CacheHelper.delete(CACHEKEY);
            CacheHelper.delete(CACHEKEY + this.id);
            CacheHelper.delete(CACHEKEY_BASEID + this.id);
        }
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_BASEID + this.id);
        super._delete();
    }

    public static void update(Long id, Goods goods) {
        update(id, goods, false);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTop(int limit) {
        return find("status=? and deleted=? and baseSale >=1 and expireAt > ? order by priority DESC,createdAt DESC",
                GoodsStatus.ONSALE,
                DeletedStatus.UN_DELETED,
                new Date()).fetch(limit);
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
                "and g.baseSale >= 1 and g.expireAt > :now and g.id in (select g.id from g.categories c where c.id = :categoryId) " +
                "order by priority DESC,createdAt DESC");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("now", new Date());
        q.setParameter("categoryId", categoryId);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public static List<Goods> findInIdList(List<Long> goodsIds) {
        if(goodsIds == null || goodsIds.size() == 0) {
            return new ArrayList<>();
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select g from Goods g where g.status=:status and g.deleted=:deleted " +
                "and g.id in :ids");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("ids", goodsIds);
        return q.getResultList();
    }

    public static Goods findUnDeletedById(long id) {
        return find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
    }

    public static Goods findOnSale(long id) {
        return find("id=? and deleted=? and status=? and baseSale >= 1 and expireAt > ?", id,
                DeletedStatus.UN_DELETED, GoodsStatus.ONSALE, new Date()).first();
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


    public static List<Brand> findBrandByCondition(GoodsCondition condition) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select distinct g.brand from Goods g where " + condition.getFilter() + " order by g.brand.displayOrder desc");
        for (String key : condition.getParamMap().keySet()) {
            q.setParameter(key, condition.getParamMap().get(key));
        }

        return q.getResultList();
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
            
            if (status == GoodsStatus.ONSALE && goods.firstOnSaleAt == null) {
                goods.firstOnSaleAt = new Date();
            }

            goods.save();
        }
    }

    private static final String expiration = "30mn";

    /**
     * 将预览商品存入缓存.
     * <p/>
     * 商品图片移动到指定目录下，指定目录下的预览用的商品图片将通过后台crontab的方式定时删除.
     *
     * @param goods
     * @return uuid
     */
    public static String preview(Long id, Goods goods, File imageFile, String rootDir) throws IOException {
        goods.status = GoodsStatus.UNCREATED;
        if (goods.isAllShop) {
            goods.shops = new HashSet<>();

            goods.shops.addAll(Shop.findShopBySupplier(goods.supplierId));
        }
        if (id == null && imageFile == null) {
            goods.imagePath = null;
        } else if (imageFile == null || imageFile.getName() == null) {
            Goods originalGoods = Goods.findById(id);
            goods.imagePath = originalGoods.imagePath;
        } else {
            String ext = imageFile.getName().substring(imageFile.getName().lastIndexOf("."));
            String imagePath = PREVIEW_IMG_ROOT + FileUploadUtil.generateUniqueId() + ext;
            File targetDir = new File(rootDir + PREVIEW_IMG_ROOT);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            FileUtils.moveFile(imageFile, new File(rootDir + imagePath));
            goods.imagePath = imagePath;
        }
        UUID cacheId = UUID.randomUUID();
        play.cache.Cache.set(cacheId.toString(), goods.id, expiration);
        return cacheId.toString();
    }

    /**
     * 获取商品预览对象.
     *
     * @param uuid 缓存键值
     * @return 预览商品
     */
    public static Goods getPreviewGoods(String uuid) {
        return (Goods) play.cache.Cache.get(uuid);
    }

    /**
     * 分销商查询
     *
     * @param condition  查询条件
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return
     */
    public static JPAExtPaginator<Goods> findByResaleCondition(Resaler resaler,
                                                               GoodsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<Goods> goodsPage = new JPAExtPaginator<>
                ("Goods g", "g", Goods.class, condition.getResaleFilter(resaler),
                        condition.getParamMap())
                .orderBy(condition.getOrderByExpress());
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);
        return goodsPage;
    }

    /**
     * 根据分销商等级和商品ID计算分销商现价
     *
     * @param level 等级
     * @return resalePrice 分销商现价
     */
    @Transient
    public BigDecimal getResalePrice(ResalerLevel level) {
        BigDecimal resalePrice = faceValue;

        for (GoodsLevelPrice goodsLevelPrice : safeGetLevelPrices()) {
            if (goodsLevelPrice.level == level) {
                resalePrice = originalPrice.add(goodsLevelPrice.price);
                break;
            }
        }
        return resalePrice;
    }

    public BigDecimal getResalerPriceOfUhuila() {
        return getResalePrice(ResalerLevel.VIP3);
    }

    /**
     * 判断分销商是否已经把商品加入分销库
     *
     * @return isExist true 已经存在，false 不存在
     */
    @Transient
    public boolean isExistLibrary(Resaler resaler) {
        boolean isExist = false;
        Query query = play.db.jpa.JPA.em().createQuery(
                "select r from ResalerFav r where r.resaler = :resaler and r.goods =:goods");
        query.setParameter("resaler", resaler);
        query.setParameter("goods", this);
        List<ResalerFav> favs = query.getResultList();
        if (favs.size() > 0) {
            isExist = true;
        }


        return isExist;
    }

    public void setLevelPrices(BigDecimal[] prices, Long id) {
        if (prices == null || prices.length == 0) {
            return;
        }
        safeGetLevelPrices();
        for (int i = 0; i < prices.length; i++) {
            this.id = id;
            levelPrices.get(i).price = prices[i];
        }
    }

    public BigDecimal[] getLevelPriceArray() {
        BigDecimal[] prices = new BigDecimal[ResalerLevel.values().length];
        if (levelPrices == null || levelPrices.size() == 0) {
            Arrays.fill(prices, BigDecimal.ZERO);
            return prices;
        }

        List<GoodsLevelPrice> levelPriceList = safeGetLevelPrices();
        for (int i = 0; i < levelPriceList.size(); i++) {
            GoodsLevelPrice levelPrice = levelPriceList.get(i);
            prices[i] = (levelPrice == null || levelPrice.price == null) ? BigDecimal.ZERO : levelPrice.price;
        }
        return prices;
    }

    public Collection<Shop> getShopList() {
        if (isAllShop) {
            return CacheHelper.getCache(CacheHelper.getCacheKey(Shop.CACHEKEY_SUPPLIERID + this.supplierId, "GOODS_SHOP_LIST"), new CacheCallBack<List<Shop>>() {
                @Override
                public List<Shop> loadData() {
                    return Shop.findShopBySupplier(supplierId);
                }
            });
        }
        final long goodsId = this.id;
        return CacheHelper.getCache(CacheHelper.getCacheKey(Goods.CACHEKEY_BASEID + goodsId, "GOODS_SHOPS"), new CacheCallBack<Set<Shop>>() {
            @Override
            public Set<Shop> loadData() {
                Goods goods1 = Goods.findById(goodsId);
                if (goods1.shops.size() == 0) {
                    return new HashSet<Shop>();
                }
                return goods1.shops;
            }
        });
    }

    /**
     * 获取最近成交的n个商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTradeRecently(int limit) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select i.goods from OrderItems i where i.goods.status = :status " +
                "and i.goods.deleted = :deleted and i.goods.baseSale >= 1 and i.goods.expireAt > :expireAt " +
                "group by i.goods order by i.createdAt DESC");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("expireAt", new Date());
        q.setMaxResults(limit);
        return q.getResultList();
    }

    /**
     * 增加商品的推荐指数.
     *
     * @param goods
     * @param like
     */
    public static void addRecommend(Goods goods, boolean like) {
        if (goods == null) {
            return;
        }
        // 因为goods可能是从缓存中读取出来的，所以需要先从数据库中读取一次
        Goods updateGoods = Goods.findById(goods.id);
        int number = 1;
        if (like) {
            number = 100;
        }
        if (updateGoods.recommend == null) {
            updateGoods.recommend = 0;
        }
        updateGoods.recommend += number;

        // 这一操作不应当更新缓存
        updateGoods.skipUpdateCache = true;
        updateGoods.save();
    }

    /**
     * 获取推荐指数高的前n个商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopRecommend(int limit) {
        String sql = "select g from Goods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.deleted =:deleted and g.expireAt >:expireAt and g.baseSale>=1 and g.isLottery is false order by s.summaryCount desc";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<Goods> goodsList = query.getResultList();
        return goodsList;

    }

    /**
     * 获取同一商户前n个推荐商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findSupplierTopRecommend(int limit, Goods goods) {
        String sql = "select g from Goods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.supplierId=:supplierId and g.deleted =:deleted and " +
                " g.id <> :goodsId and g.expireAt >:expireAt and g.baseSale>=1 and g.isLottery is false order by s.summaryCount desc";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("supplierId", goods.supplierId);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);

        query.setParameter("goodsId", goods.id);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<Goods> goodsList = query.getResultList();
        List<Goods> newGoodsList = new ArrayList<>();
        List<Goods> doGoodsList = new ArrayList<>();
        int goodsCount = goodsList.size();
        if (goodsCount < 5) {
            newGoodsList = findTopRecommendByCategory(5 - goodsCount, goods);
            for (Goods goods1 : newGoodsList) {
                goodsList.add(goods1);
            }
        }
        for (Goods goods2 : goodsList) {
            doGoodsList.add(goods2);
        }
        return doGoodsList;

    }

    /**
     * 获取同一种子分类的其他商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopRecommendByCategory(final int limit, final Goods goods) {
        final Long goodsId = goods.id;
        Long categoryId = CacheHelper.getCache(CacheHelper.getCacheKey(Category.CACHEKEY + goodsId, "GOODS_CATE"), new CacheCallBack<Long>() {
            @Override
            public Long loadData() {
                Goods g = Goods.findById(goodsId);
                Long id = 0l;
                for (Category ca : g.categories) {
                    id = ca.id;
                }
                return id;
            }
        });

        String sql = "select g from Goods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.deleted =:deleted and " +
                " g.id in (select g.id from g.categories c where c.id = :categoryId or (c.parentCategory is not null and c.parentCategory.id=:categoryId))" +
                "and g.id <> :goodsId and g.supplierId <> :supplierId and g.expireAt >:expireAt and g.baseSale>=1 and g.isLottery is false order by s.summaryCount desc";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("categoryId", categoryId);

        query.setParameter("goodsId", goods.id);
        query.setParameter("supplierId", goods.supplierId);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<Goods> goodsList = query.getResultList();
        return goodsList;

    }

    /**
     * 获取最新商品
     *
     * @param limit
     * @return
     */
    public static List<Goods> findNewGoods(int limit) {
        return Goods.find("status = ? and deleted = ? and baseSale >= 1 and expireAt > ? order by createdAt DESC",
                GoodsStatus.ONSALE, DeletedStatus.UN_DELETED, new Date()).fetch(limit);
    }

    public void setPublishedPlatforms(List<GoodsPublishedPlatformType> publishedPlatforms) {
        if (unPublishedPlatforms == null) {
            unPublishedPlatforms = new HashSet<>();
        } else {
            unPublishedPlatforms.clear();
        }

        if (publishedPlatforms == null || publishedPlatforms.size() == 0) {
            for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
                final GoodsUnPublishedPlatform goodsUnPublishedPlatform = new GoodsUnPublishedPlatform(this, type);
                unPublishedPlatforms.add(goodsUnPublishedPlatform);
            }
            return;
        }

        for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
            if (!publishedPlatforms.contains(type)) {
                final GoodsUnPublishedPlatform goodsUnPublishedPlatform = new GoodsUnPublishedPlatform(this, type);
                unPublishedPlatforms.add(goodsUnPublishedPlatform);
            }
        }
    }

    public boolean canPublishTo(GoodsPublishedPlatformType type) {
        return !containsUnPublishedPlatform(type);
    }

    public boolean onSale() {
        return (GoodsStatus.ONSALE.equals(status) && expireAt.after(new Date()) &&
                baseSale > 0 && DeletedStatus.UN_DELETED.equals(deleted));
    }

    public Long summaryCount() {
        final Long goodsId = this.id;
        GoodsStatistics statistics = CacheHelper.getCache(CacheHelper.getCacheKey(GoodsStatistics.CACHEKEY_GOODSID + goodsId, "GOODSSTATS"), new CacheCallBack<GoodsStatistics>() {
            @Override
            public GoodsStatistics loadData() {
                return GoodsStatistics.find("goodsId", goodsId).first();
            }
        });
        if (statistics == null) {
            return 0l;
        }
        return statistics.summaryCount;
    }

}