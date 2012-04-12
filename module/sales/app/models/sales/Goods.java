/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.ImageSize;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.PathUtil;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.resale.ResalerLevel;
import models.supplier.Supplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.validation.*;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "goods")
public class Goods extends Model {
    public static final String PREVIEW_IMG_ROOT = "/9999/9999/9999/";

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
     * 运营人员填写的优惠啦网站价格
     */
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("level")
    @JoinColumn(name = "goods_id")
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
    @MaxSize(80)
    public String name;
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
    /**
     * 商品标题
     */
    //    public String title;

    private Integer discount;

    @Required
    @MinSize(7)
    @MaxSize(4000)
    @Lob
    private String details;

    /**
     * 温馨提示
     */
    @MaxSize(4000)
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


    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");
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
     * 获取商品所属的商户信息.
     *
     * @return
     */
    @Transient
    public Supplier getSupplier() {
        return Supplier.findById(supplierId);
    }

    static {
        //增加可信标签到白名单
        HTML_WHITE_TAGS.addTags("embed", "object", "param", "span", "div");
        //增加可信属性
        HTML_WHITE_TAGS.addAttributes(":all", "style", "class", "id", "name");
        HTML_WHITE_TAGS.addAttributes("object", "width", "height", "classid", "codebase");
        HTML_WHITE_TAGS.addAttributes("param", "name", "value");
        HTML_WHITE_TAGS.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen", "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
    }

    @Column(name = "is_all_shop")
    public Boolean isAllShop = true;

    @Transient
    public Long topCategoryId;

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

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    @Column(name = "discount")
    public Integer getDiscount() {
        if (discount != null && discount > 0) {
            return discount;
        }
        if (faceValue != null && salePrice != null && faceValue.compareTo(new BigDecimal(0)) > 0) {
            this.discount = salePrice.divide(faceValue, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
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


    public void setLevelPrices(List<GoodsLevelPrice> levelPrices) {
        this.levelPrices = levelPrices;
    }

    public void setLevelPrices(BigDecimal[] prices) {
        if (prices == null || prices.length == 0) {
            return;
        }
        for (int i = 0; i < prices.length; i++) {
            getLevelPrices().get(i).price = prices[i];
        }
    }

    /**
     * 不同分销商等级所对应的价格, 此方法可确保返回的价格数量与分销等级的数量相同
     */
    public List<GoodsLevelPrice> getLevelPrices() {
        if (levelPrices == null) {
            levelPrices = new ArrayList<>();
        }
        if (levelPrices.size() == 0) {
            for (ResalerLevel level : ResalerLevel.values()) {
                GoodsLevelPrice levelPrice = new GoodsLevelPrice(this, level, BigDecimal.ZERO);
                levelPrices.add(levelPrice);
            }
        } else {
            for (GoodsLevelPrice levelPrice : levelPrices) {
                if (levelPrice.price == null) {
                    levelPrice.price = BigDecimal.ZERO;
                }
            }
        }

        if (levelPrices.size() < ResalerLevel.values().length) {
            int zeroLevelCount = ResalerLevel.values().length - levelPrices.size();
            int originalSize = levelPrices.size();
            for (int i = 0; i < zeroLevelCount; i++) {
                levelPrices.add(new GoodsLevelPrice(this, ResalerLevel.values()[i + originalSize], BigDecimal.ZERO));
            }
        }
        return levelPrices;
    }

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

    public String getPrompt() {
        if (StringUtils.isBlank(prompt)) {
            return "";
        }
        return Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    public void setPrompt(String prompt) {
        this.prompt = Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

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


        if (!noLevelPrices) {
            updateGoods.levelPrices = goods.levelPrices;
        }
        updateGoods.setPrompt(goods.getPrompt());
        updateGoods.setDetails(goods.getDetails());
        updateGoods.updatedAt = new Date();
        updateGoods.updatedBy = goods.updatedBy;
        updateGoods.brand = goods.brand;
        updateGoods.isAllShop = goods.isAllShop;
        updateGoods.status = goods.status;
        if (!StringUtils.isEmpty(goods.imagePath)) {
            updateGoods.imagePath = goods.imagePath;
        }
        if (goods.supplierId != null) {
            updateGoods.supplierId = goods.supplierId;
        }
        updateGoods.shops = goods.shops;
        updateGoods.save();
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

    public static List<Goods> findInIdList(List<Long> goodsIds) {
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
        if (goods.isAllShop){
            goods.shops = new HashSet<>();

            System.out.println("goods.supplierId:" + goods.supplierId);
            System.out.println("Shop.findShopBySupplier(goods.supplierId):" + Shop.findShopBySupplier(goods
                    .supplierId).size());
            goods.shops.addAll(Shop.findShopBySupplier(goods.supplierId));
        }

        if (imageFile == null && id != null) {
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
        play.cache.Cache.set(cacheId.toString(), goods, expiration);
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
                .orderBy("createdAt desc");
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

        for (GoodsLevelPrice goodsLevelPrice : getLevelPrices()) {
            if (goodsLevelPrice.level == level) {
                resalePrice = originalPrice.add(goodsLevelPrice.price);
                break;
            }
        }
        return resalePrice;
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
        for (int i = 0; i < prices.length; i++) {
            this.id = id;
            getLevelPrices().get(i).price = prices[i];
        }

    }

    public BigDecimal[] getLevelPriceArray() {
        BigDecimal[] prices = new BigDecimal[ResalerLevel.values().length];
        if (levelPrices == null || levelPrices.size() == 0) {
            Arrays.fill(prices, BigDecimal.ZERO);
            return prices;
        }

        List<GoodsLevelPrice> levelPriceList = getLevelPrices();
        for (int i = 0; i < levelPriceList.size(); i++) {
            GoodsLevelPrice levelPrice = levelPriceList.get(i);
            prices[i] = (levelPrice == null || levelPrice.price == null) ? BigDecimal.ZERO : levelPrice.price;
        }
        return prices;
    }
    
    public Iterator<Shop> getShopList(){
        if (isAllShop){
            return Shop.findShopBySupplier(supplierId).iterator();
        }
        return shops.iterator();
    }
}