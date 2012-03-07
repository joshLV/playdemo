/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.*;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import com.uhuila.common.constants.DeletedStatus;
import play.modules.paginate.JPAExtPaginator;

@Entity
@Table(name = "goods")
public class Goods extends Model {

    private static final Pattern imagePat = Pattern.compile("^/([0-9]+)/([0-9]+)/([0-9]+)/([^_]+).(jpg|png|gif|jpeg)$");
    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "http://img0.uhlcdndev.net");
    private static final String IMAGE_ROOT_GENERATED = Play.configuration
            .getProperty("image.root", "/p");

    /**
     * 商品编号
     */
    public String no;
    /**
     * 商品名称
     */
    @Required
    public String name;
    /**
     * 所属商户ID
     */
    @Column(name = "company_id")
    public String companyId;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name
            = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
    public Set<Shop> shops;

    public void addValues(Shop shop) {
        if (this.shops ==null) {
            this.shops = new HashSet<>();
        }
        if (!this.shops.contains(shop)) {
            this.shops.add(shop);
        }
    }

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "goods_categories", inverseJoinColumns = @JoinColumn(name
            = "category_id"), joinColumns = @JoinColumn(name = "goods_id"))
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
        return getImageBySizeType("tiny");
    }

    /**
     * 小规格图片路径
     */
    @Transient
    public String getImageSmallPath() {
        return getImageBySizeType("small");
    }

    /**
     * 中等规格图片路径
     */
    @Transient
    public String getImageMiddlePath() {
        return getImageBySizeType("middle");
    }

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return getImageBySizeType("large");
    }

    /**
     * 进货量
     */
    @Column(name = "income_goods_count")
    public String incomeGoodsCount;
    /**
     * 券有效开始日
     */
    @Column(name = "effective_at")
    @Temporal(TemporalType.DATE)
    public Date effectiveAt;
    /**
     * 券有效结束日
     */
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
    @Column(name = "original_price")
    public BigDecimal originalPrice;
    /**
     * 商品现价
     */
    @Column(name = "sale_price")
    public BigDecimal salePrice;

    private Integer discount;

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    @Column(name = "discount")
    public Integer getDiscount() {
        if (originalPrice != null && originalPrice.compareTo(new BigDecimal(0)) > 0) {
            this.discount = salePrice.divide(originalPrice).multiply
                    (new BigDecimal(100)).toBigInteger().intValue();
        }
        this.discount = 0;
        return discount;
    }

    @Transient
    public String getDiscountExpress() {
        if (originalPrice != null && originalPrice.compareTo(new BigDecimal(0)) > 0) {
            int discount = getDiscount();
            if (discount == 100) {
                return "";
            }
            if (discount % 10 == 0) {
                return String.valueOf(discount / 10);
            }
            return String.valueOf(discount);
        }
        return "";
    }

    /**
     * 温馨提示
     */
    public String prompt;
    /**
     * 商品详情
     */
    @Required
    public String details;
    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public int saleCount;
    /**
     * 售出基数
     */
    @Column(name = "base_sale")
    @Required
    public long baseSale;
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

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = true)
    public Brand brand;

    @Transient
    public String salePriceBegin;
    @Transient
    public String salePriceEnd;
    @Transient
    public int saleCountBegin;
    @Transient
    public int saleCountEnd;
    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    private String getImageBySizeType(String sizeType) {
        String defaultImage = IMAGE_SERVER + IMAGE_ROOT_GENERATED +
                "/1/1/1/default_" + sizeType + ".png";
        if (imagePath == null || imagePath.equals("")) {
            return defaultImage;
        }
        Matcher matcher = imagePat.matcher(imagePath);
        if (!matcher.matches()) {
            return defaultImage;
        }
        String imageHeadStr = IMAGE_ROOT_GENERATED + imagePath;
        return IMAGE_SERVER + imageHeadStr.replace("/" + matcher.group(4), "/" + matcher.group(4) + "_" + sizeType);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTop(int limit) {
        return find("status=? and deleted=? order by updatedAt,createdAt DESC",
                GoodsStatus.ONSALE,
                DeletedStatus.UN_DELETED).fetch(limit);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param limit
     * @return
     */
    public static List<Goods> findTopByCategory(long categoryId,int limit) {
        EntityManager entityManager = JPA.em();
        Query q = entityManager.createQuery("select g from Goods g where g.status=:status and g.deleted=:deleted " +
                "and g.id in (select g.id from g.categories c where c.id = :categoryId) order by g.updatedAt, g.createdAt DESC");
        q.setParameter("status",GoodsStatus.ONSALE);
        q.setParameter("deleted",DeletedStatus.UN_DELETED);
        q.setParameter("categoryId",categoryId);
        q.setMaxResults(limit);
        return q.getResultList();
//        return find("status=? and deleted=? and categories.id=? order by updatedAt,createdAt DESC",
//                GoodsStatus.ONSALE,
//                DeletedStatus.UN_DELETED,
//                Category.findById(categoryId)).fetch(limit);
    }

    public static Goods findUnDeletedById(long id) {
        return find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();

    }

    /**
     * 查询商品一览
     *
     * @param goods
     * @return
     */
    public List query(models.sales.Goods goods, Pager pager) {
        StringBuilder condition = new StringBuilder();
        condition.append(" deleted = ? ");
        List<Object> params = new ArrayList();
        params.add(DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(goods.name)) {
            condition.append(" and name like ? ");
            params.add("%" + goods.name + "%");
        }
        if (StringUtils.isNotBlank(goods.no)) {
            condition.append(" and no like ? ");
            params.add("%" + goods.no + "%");
        }
        if (goods.status !=null) {
            condition.append(" and status = ? ");
            params.add(goods.status);
        }
        if (StringUtils.isNotBlank(goods.salePriceBegin)) {
            condition.append(" and salePrice >= ?");
            params.add(new BigDecimal(goods.salePriceBegin));
        }
        if (StringUtils.isNotBlank(goods.salePriceEnd)) {
            condition.append(" and salePrice <= ?");
            params.add(new BigDecimal(goods.salePriceEnd));
        }
        if (goods.saleCountBegin > 0) {
            condition.append(" and saleCount >= ?");
            params.add(goods.saleCountBegin);
        }
        if (goods.saleCountEnd > 0) {
            condition.append(" and saleCount <= ?");
            params.add(goods.saleCountEnd);
        }
        pager.totalCount = count(condition.toString(), params.toArray());
        pager.totalPager();
        condition.append(" order by created_at desc");
        return find(condition.toString(), params.toArray()).fetch(pager.currPage, pager.pageSize);
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
}