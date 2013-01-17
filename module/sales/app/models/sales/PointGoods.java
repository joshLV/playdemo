package models.sales;


import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.PathUtil;
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User: clara
 * Date: 12-8-2
 * Time: 下午4:37
 */


@Entity
@Table(name = "point_goods")
public class PointGoods extends Model {
    public static String EMAIL_RECEIVER = Play.configuration.getProperty("goods_not_enough.receiver", "dev@uhuila.com");

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
     * 积分商品原价
     */
    @Required
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "face_value")
    public BigDecimal faceValue;

    /**
     * 积分价
     */
    @Required
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "point_price")
    public Long pointPrice;

    //  ======  价格列表结束 ==========

    /**
     * 积分商品编号
     */
    @MaxSize(30)
    public String no;

    /**
     * 积分商品名称
     */
    @Required
    @MaxSize(60)
    public String name;


    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;


    /**
     * 商品类型（单选 电子券/实物券）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    /**
     * 券有效开始日
     */
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;

    /**
     * 券有效结束日
     */
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    @Column(name = "use_begin_time")
    public String useBeginTime;

    @Column(name = "use_end_time")
    public String useEndTime;


    /**
     * 积分商品的详情
     */
    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    private String details;

    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public int saleCount;

    /**
     * 库存
     */
    @Required
    @Min(0)
    @Max(999999)
    @Column(name = "base_sale")
    public Long baseSale;

    /**
     * 积分商品状态,
     */
    @Enumerated(EnumType.STRING)
    public GoodsStatus status;

    /**
     * 限量
     */
    @Column(name = "limit_number")
    public Integer limitNumber = 0;

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
     * SEO关键字.
     */
    @Column(name = "keywords")
    public String keywords;

    /**
     * 不允许发布的电子商务网站.
     * 设置后将不允许自动发布到这些电子商务网站上
     */
    /*
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "goods")
    public Set<GoodsUnPublishedPlatform> unPublishedPlatforms;
    */

    // 图片服务器
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


    @Transient
    public boolean skipUpdateCache = false;


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

    /**
     * 小规格图片路径
     */
    @Transient
    public String getImageSmallPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_SMALL);
    }

    /**
     * 最小规格图片路径
     */
    @Transient
    public String getImageTinyPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_TINY);
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

    public GoodsStatus getStatus() {
//        if (status != null && GoodsStatus.ONSALE.equals(status) &&
//                (expireAt != null && expireAt.before(new Date())) || (baseSale != null && baseSale <= 0)) {
//            status = GoodsStatus.OFFSALE;
//        }
        return status;
    }


//    public GoodsStatus getCurrentStatus() {
//
//        return status;
//    }


    /**
     * @return
     */
    @Transient
    public boolean isExpired() {
        boolean isExpired=false;
        if(expireAt != null && expireAt.before(new Date()))
        {    isExpired=true;


//            //发送提醒邮件
//            MailMessage mailMessage = new MailMessage();
//            mailMessage.addRecipient(EMAIL_RECEIVER);
//            mailMessage.setSubject(Play.mode.isProd() ? "积分商品 " : "商品下架【测试】");
//
//
//            PointGoods pointGoods = PointGoods.findById(orderItem.goods.supplierId);
//
//            mailMessage.putParam("goodsName", pointGoods.name);
//            mailMessage.putParam("pointPrice", pointGoods.pointPrice);
//            mailMessage.putParam("expireAt", pointGoods.expireAt);
//
//
//            MailUtil.sendGoodsOffSalesMail(mailMessage);
        }

        return  isExpired;
    }

    //=================================================== 数据库操作 ====================================================

    @Override
    public boolean create() {
        deleted = DeletedStatus.UN_DELETED;
        saleCount = 0;
        createdAt = new Date();
        expireAt = DateUtil.getEndOfDay(expireAt);
        return super.create();
    }

    public static void update(Long id, PointGoods pointGoods) {
        models.sales.PointGoods updateGoods = models.sales.PointGoods.findById(id);
        if (updateGoods == null) {
            return;
        }
        //System.out.println(">>>>>"+pointGoods.name);
        updateGoods.name = pointGoods.name;
        updateGoods.no = pointGoods.no;
        updateGoods.effectiveAt = pointGoods.effectiveAt;
        updateGoods.expireAt = DateUtil.getEndOfDay(pointGoods.expireAt);
        updateGoods.faceValue = pointGoods.faceValue;
        updateGoods.pointPrice = pointGoods.pointPrice;
        updateGoods.baseSale = pointGoods.baseSale;
        updateGoods.materialType = pointGoods.materialType;
        updateGoods.setDetails(pointGoods.getDetails());
        updateGoods.updatedAt = new Date();
        updateGoods.updatedBy = pointGoods.updatedBy;
        updateGoods.status = pointGoods.status;
        updateGoods.keywords = pointGoods.keywords;
        updateGoods.limitNumber = pointGoods.limitNumber;
        if (!StringUtils.isEmpty(pointGoods.imagePath)) {
            updateGoods.imagePath = pointGoods.imagePath;
        }

        updateGoods.useBeginTime = pointGoods.useBeginTime;
        updateGoods.useEndTime = pointGoods.useEndTime;

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

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<PointGoods> findTop(int limit) {
        return find("status=? and deleted=? and baseSale >=1 and expireAt > ? order by priority DESC,createdAt DESC",
                GoodsStatus.ONSALE,
                DeletedStatus.UN_DELETED,
                new Date()).fetch(limit);
    }

    public static List<PointGoods> findInIdList(List<Long> goodsIds) {
        if(goodsIds == null || goodsIds.size() == 0) {
            return new ArrayList<>();
        }
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select g from PointGoods g where g.status=:status and g.deleted=:deleted " +
                "and g.id in :ids");
        q.setParameter("status", GoodsStatus.ONSALE);
        q.setParameter("deleted", DeletedStatus.UN_DELETED);
        q.setParameter("ids", goodsIds);
        return q.getResultList();
    }

    public static PointGoods findUnDeletedById(long id) {
        return find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
    }

    public static PointGoods findOnSale(long id) {
        return find("id=? and deleted=? and status=? and baseSale >= 1 and expireAt > ?", id,
                DeletedStatus.UN_DELETED, GoodsStatus.ONSALE, new Date()).first();
    }

    public static JPAExtPaginator<PointGoods> findByCondition(PointGoodsCondition condition,
                                                         int pageNumber, int pageSize) {

        JPAExtPaginator<PointGoods> pointGoodsPage = new JPAExtPaginator<>
                ("PointGoods g", "g", PointGoods.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy(condition.getOrderByCreatedAtDesc());
        pointGoodsPage.setPageNumber(pageNumber);
        pointGoodsPage.setPageSize(pageSize);
        pointGoodsPage.setBoundaryControlsEnabled(false);
        return pointGoodsPage;
    }




    // 删除商品（修改状态为deleted）
    public static void delete(Long... ids) {
        for (Long id : ids) {
            models.sales.PointGoods goods = models.sales.PointGoods.findById(id);
            if (goods != null) {
                goods.deleted = DeletedStatus.DELETED;
                goods.save();
            }
        }
    }

    // 修改上下架状态
    public static void updateStatus(GoodsStatus status, Long... ids) {
        for (Long id : ids) {
            models.sales.PointGoods goods = models.sales.PointGoods.findById(id);
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
     * @param pointGoods
     * @return uuid
     */
    public static String preview(Long id, PointGoods pointGoods, File imageFile, String rootDir) throws IOException {
        pointGoods.status = GoodsStatus.UNCREATED;

        if (id == null && imageFile == null) {
            pointGoods.imagePath = null;
        } else if (imageFile == null || imageFile.getName() == null) {
            PointGoods originalGoods = PointGoods.findById(id);
            pointGoods.imagePath = originalGoods.imagePath;
        } else {
            String ext = imageFile.getName().substring(imageFile.getName().lastIndexOf("."));
            String imagePath = PREVIEW_IMG_ROOT + FileUploadUtil.generateUniqueId() + ext;
            File targetDir = new File(rootDir + PREVIEW_IMG_ROOT);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            FileUtils.moveFile(imageFile, new File(rootDir + imagePath));
            pointGoods.imagePath = imagePath;
        }
        UUID cacheId = UUID.randomUUID();
        play.cache.Cache.set(cacheId.toString(), id, expiration);
        return cacheId.toString();
    }

    /**
     * 获取商品预览对象.
     *
     * @param uuid 缓存键值
     * @return 预览商品
     */
    public static PointGoods getPreviewGoods(String uuid) {
        return (PointGoods) play.cache.Cache.get(uuid);
    }


    /**
     * 增加商品的推荐指数.
     *
     * @param goods
     * @param like
     */
    public static void addRecommend(PointGoods goods, boolean like) {
        if (goods == null) {
            return;
        }
        // 因为goods可能是从缓存中读取出来的，所以需要先从数据库中读取一次
        PointGoods updateGoods = PointGoods.findById(goods.id);
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
    public static List<PointGoods> findTopRecommend(int limit) {
        String sql = "select g from PointGoods g,GoodsStatistics s  where g.id =s.goodsId " +
                " and g.status =:status and g.deleted =:deleted and g.expireAt >:expireAt and g.baseSale>=1 order by s.summaryCount desc";
        Query query = PointGoods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<PointGoods> goodsList = query.getResultList();
        return goodsList;

    }

    public static List<PointGoods> findTopSaleGoods(int limit) {
        String sql = "select g from PointGoods g where " +
                "g.status =:status and g.deleted =:deleted and g.expireAt >:expireAt and g.baseSale>=1 order by g.saleCount desc";
        Query query = PointGoods.em().createQuery(sql);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("expireAt", new Date());
        query.setMaxResults(limit);
        List<PointGoods> goodsList = query.getResultList();
        return goodsList;
    }

    public boolean onSale() {
        boolean isEffective = true;
        if (expireAt != null && expireAt.before(new Date())){
            isEffective = false;
        }
        return (GoodsStatus.ONSALE.equals(status) && isEffective &&
                baseSale > 0 && DeletedStatus.UN_DELETED.equals(deleted));
    }

    public Long summaryCount() {
        final Long pointGoodsId = this.id;
        GoodsStatistics statistics = CacheHelper.getCache(CacheHelper.getCacheKey(GoodsStatistics.CACHEKEY_GOODSID + pointGoodsId, "GOODSSTATS"), new CacheCallBack<GoodsStatistics>() {
            @Override
            public GoodsStatistics loadData() {
                return GoodsStatistics.find("goodsId", pointGoodsId).first();
            }
        });
        if (statistics == null) {
            return 0l;
        }
        return statistics.summaryCount;
    }




}

