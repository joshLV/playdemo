package models.sales;

import play.data.validation.*;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-11
 * Time: 下午3:55
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "goods_history")
public class GoodsHistory extends Model {

    /**
     * 所属商品ID
     */
    @Column(name = "goods_id")
    public Long goodsId;

    /**
     * 商品名称
     */
    @Required
    @MaxSize(60)
    public String name;

    /**
     * 商品编号
     */
    @MaxSize(30)
    public String no;

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

    private BigDecimal discount;

    /**
     * 运营人员填写的一百券网站价格
     */
    @Min(0)
    @Max(999999)
    @Money
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    /**
     * 售出基数
     */
    @Required
    @Min(0)
    @Max(999999)
    @Column(name = "base_sale")
    public Long baseSale;

    /**
     * 给推荐者的返利金额
     */
    @Column(name = "promoter_price")
    @Min(0)
    @Max(5)
    @Money
    public BigDecimal promoterPrice;

    /**
     * 给受邀者的返利金额
     */
    @Column(name = "invited_user_price")
    @Min(0)
    @Max(5)
    @Money
    public BigDecimal invitedUserPrice;


    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;


    @Transient
    public Long topCategoryId;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "category_id"), joinColumns = @JoinColumn(name = "goods_id"))
    @Required
    public Set<Category> categories;

    /**
     * 分销渠道加价
     */
    @Min(0)
    @Max(999999)
    @Money
    @Required
    @Column(name = "resale_price")
    public BigDecimal resaleAddPrice;

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

    @Required
    @ManyToOne
    @JoinColumn(name = "brand_id")
    public Brand brand;

    @Column(name = "is_all_shop")
    public Boolean isAllShop = true;

    /**
     * 商品状态,
     */
    @Enumerated(EnumType.STRING)
    public GoodsStatus status;

    /**
     * SEO关键字.
     */
    @Column(name = "keywords")
    public String keywords;

    /**
     * 限购数量
     */
    @Column(name = "limit_number")
    public Integer limitNumber = 0;

    @Column(name = "coupon_type")
    @Enumerated(EnumType.STRING)
    public GoodsCouponType couponType;

    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

    /**
     * 所属商户ID
     */
    @Column(name = "supplier_id")
    public Long supplierId;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name
            = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
    public Set<Shop> shops;

    /**
     * 商品标题（短信发送用）
     */
    @Required
    @MaxSize(60)
    public String title;

    @Column(name = "use_begin_time")
    public String useBeginTime;

    @Column(name = "use_end_time")
    public String useEndTime;

    @Column(name = "use_week_day")
    public String useWeekDay;

    /**
     * 是否抽奖商品
     */
    @Column(name = "is_lottery")
    public Boolean isLottery = Boolean.FALSE;

    /**
     * 商品分组代码.
     * 用于多个商品组合成一个商品组，同一系列的发送收货短信时，使用相同的replyCode.
     */
    @Column(name = "group_code", length = 32)
    public String groupCode;

    /**
     * 温馨提示
     */
    @MaxSize(65000)
    @Lob
    private String prompt;

    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    private String details;

    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    /**
     * 不允许发布的电子商务网站.
     * 设置后将不允许自动发布到这些电子商务网站上
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "goods")
    public Set<GoodsUnPublishedPlatform> unPublishedPlatforms;

    public void setDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) >= 0 && discount.compareTo(BigDecimal.TEN) <= 0) {
            this.discount = discount;
        } else if (discount.compareTo(BigDecimal.ZERO) < 0) {
            this.discount = BigDecimal.ZERO;
        } else if (discount.compareTo(BigDecimal.TEN) > 0) {
            this.discount = BigDecimal.TEN;
        }
    }

    public void setPrompt(String prompt) {
        this.prompt = Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    public void setDetails(String details) {
        this.details = Jsoup.clean(details, HTML_WHITE_TAGS);
    }

    public void setPublishedPlatforms(List<GoodsPublishedPlatformType> publishedPlatforms, Goods goods) {
        if (unPublishedPlatforms == null) {
            unPublishedPlatforms = new HashSet<>();
        } else {
            unPublishedPlatforms.clear();
        }

        if (publishedPlatforms == null || publishedPlatforms.size() == 0) {
            for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
                final GoodsUnPublishedPlatform goodsUnPublishedPlatform = new GoodsUnPublishedPlatform(goods, type);
                unPublishedPlatforms.add(goodsUnPublishedPlatform);
            }
            return;
        }

        for (GoodsPublishedPlatformType type : GoodsPublishedPlatformType.values()) {
            if (!publishedPlatforms.contains(type)) {
                final GoodsUnPublishedPlatform goodsUnPublishedPlatform = new GoodsUnPublishedPlatform(goods, type);
                unPublishedPlatforms.add(goodsUnPublishedPlatform);
            }
        }
    }
}
