package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.*;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-2
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
 */


@Entity
@Table(name = "point_goods")
public class PointGoods extends Model {
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

    /**
     * 商品编号
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
     * 积分价
     */
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "point_price")
    public BigDecimal pointPrice;



    /**
     * 商品类型（单选 电子券/实物券）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    /**
     * 券有效期（电子券必填，实物券不需要）
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;



    /**
     * 库存
     */
    @Required
    @Min(0)
    @Max(999999)
    @Column(name = "base_sale")
    public Long baseSale;


    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public int saleCount;



    /**
     * 限量
     */
    @Column(name = "limit_number")
    public Integer limitNumber = 0;


    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;


    /**
     * 详情
     */
    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    private String details;


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
     * 进货量
     */
    @Column(name = "income_goods_count")
    public Long incomeGoodsCount;


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
     * 积分商品状态,
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


    //是否使用全部店
    @Column(name = "is_all_shop")
    public Boolean isAllShop = true;


}
