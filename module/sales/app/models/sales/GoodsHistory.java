package models.sales;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.util.PathUtil;
import models.resale.ResalerFav;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.binding.As;
import play.data.validation.InFuture;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: wangjia
 * Date: 12-10-11
 * Time: 下午3:55
 */

@Entity
@Table(name = "goods_history")
public class GoodsHistory extends Model {
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

    public static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");


    /**
     * 所属商品ID
     */
    @Column(name = "goods_id")
    public Long goodsId;

    /**
     * 供应商的GoodsId
     */
    @Column(name = "supplier_goods_id")
    public Long supplierGoodsId;

    /**
     * 商品短名称
     */
    @Required
    @MaxSize(24)
    @Column(name = "short_name")
    public String shortName;

    /**
     * (网站标题)原来叫商品名称
     */
    @Required
    @MaxSize(1000)
    public String name;
    /**
     * 商品编号
     */
    @MaxSize(30)
    public String no;

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

    /**
     * 是否预约商品
     */
    @Column(name = "is_order")
    public Boolean isOrder = Boolean.FALSE;

    /**
     * 券有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;

    /**
     * 最早上架时间
     */
    @Column(name = "first_onsale_at")
    public Date firstOnSaleAt;

    /**
     * 券有效结束日
     */
    @Required
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    /**
     * 是否隐藏上架
     */
    @Column(name = "is_hide_onsale")
    public Boolean isHideOnsale = Boolean.FALSE;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_history_id")
    public List<ResalerFav> resalerFavs;

    /**
     * 手工排序
     */
    @Column(name = "display_order")
    public String displayOrder;

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

    public BigDecimal discount;

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

    //  ======  价格列表结束 ==========

    /**
     * 开始上架时间
     */
    @Column(name = "begin_onsale_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date beginOnSaleAt;

    /**
     * 下架时间
     */
    @Column(name = "end_onsale_at")
    @As(lang = {"*"}, value = {"yyyy-MM-dd HH:mm:ss"})
    public Date endOnSaleAt;

    /**
     * 累积进货量
     * <p/>
     * 通过管理界面增加进货量后得到累计进货量，只会越来越大，因为是累计的
     */
    @Column(name = "cumulative_stocks")
    @Required
    public Long cumulativeStocks;

    /**
     * 虚拟销量基数
     * <p/>
     * 通过管理界面设置
     * 用户前端网站显示的销量是virtualBaseSaleCount+realSaleCount
     */
    @Column(name = "virtual_base_sale_count")
    public Long virtualBaseSaleCount;

    /**
     * 界面上显示的销量，实际销量+虚拟销量基数
     */
    @Transient
    public Long virtualSaleCount;

    /**
     * 商家介绍
     */
    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    @Column(name = "supplier_des")
    public String supplierDes;

    /**
     * 商品详情
     */
    @MaxSize(65000)
    @Lob
    @Required
    public String exhibition;

    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;


    @Transient
    public Long topCategoryId;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_history_categories", inverseJoinColumns = @JoinColumn(name
            = "category_id"), joinColumns = @JoinColumn(name = "goods_history_id"))
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
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 修改人
     */
    @Column(name = "created_by")
    public String createdBy;


    /**
     * 来自于 运营后台 or 商户后台
     */
    @Column(name = "created_from")
    public String createdFrom;

    @Required
    @ManyToOne
    @JoinColumn(name = "brand_id")
    public Brand brand;


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
     * 商品流水码（至少2位 可动态扩展）
     */
    @Column(name = "sequence_code")
    public String sequenceCode;

    /**
     * 商品编码 【商户类别编码（2位）+商户流水码（4位）+商品流水码（至少2位 可动态扩展）】
     */
    @Column(name = "code")
    public String code;


    /**
     * 进货量
     */
    @Column(name = "income_goods_count")
    public Long incomeGoodsCount;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public com.uhuila.common.constants.DeletedStatus deleted;

    /**
     * 温馨提示
     */
    @Required
    @MaxSize(65000)
    @Lob
    public String prompt;

    /**
     * 不可退款
     */
    @Column(name = "no_refund")
    public Boolean noRefund = Boolean.FALSE;

    @Required
    @MinSize(7)
    @MaxSize(65000)
    @Lob
    public String details;

    @Column(name = "is_all_shop")
    public Boolean isAllShop = true;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "goods_history_shops", inverseJoinColumns = @JoinColumn(name
            = "shop_id"), joinColumns = @JoinColumn(name = "goods_history_id"))
    public Set<Shop> shops;

    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    /**
     * 不允许发布的电子商务网站.
     * 设置后将不允许自动发布到这些电子商务网站上
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "goodshistory")
    public Set<GoodsHistoryUnPublishedPlatform> unPublishedPlatforms;

    @OneToMany(mappedBy = "goodsHistory")
    public List<GoodsImages> goodsImagesList;

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_LARGE);
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

    public void setDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) >= 0 && discount.compareTo(BigDecimal.TEN) <= 0) {
            this.discount = discount;
        } else if (discount != null && discount.compareTo(BigDecimal.ZERO) < 0) {
            this.discount = BigDecimal.ZERO;
        } else if (discount != null && discount.compareTo(BigDecimal.TEN) > 0) {
            this.discount = BigDecimal.TEN;
        }
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

    public void setDetails(String details) {
        this.details = Jsoup.clean(details, HTML_WHITE_TAGS);
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

    public Long summaryCount() {
        return theGoods().summaryCount();
    }

    public boolean onSale() {
        return theGoods().onSale();
    }

    @Transient
    private Goods goods;

    public Goods theGoods() {
        if (goods == null) {
            goods = Goods.findById(goodsId);
        }
        return goods;
    }

    public GoodsStatus getStatus() {
        return theGoods().getStatus();
    }

    /**
     * 得到实际的库存数量.
     */
    @Transient
    public Long getRealStocks() {
        return theGoods().getRealSaleCount();
    }

    /**
     * 得到当前实际的销售数量.
     */
    @Transient
    public Long getRealSaleCount() {
        return theGoods().getRealSaleCount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Goods goods = (Goods) o;

        if (id != goods.id) return false;
        if (name != null ? !name.equals(goods.name) : goods.name != null) return false;
        if (title != null ? !title.equals(goods.title) : goods.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
