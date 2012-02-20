/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "goods")
public class Goods extends Model {

    private static final Pattern imagePat = Pattern.compile("^/o/([0-9]+)/([0-9]+)/([0-9]+)/([^_]+).(jpg|png|gif|jpeg)$");
    private static final String IMAGE_SERVER = "http://localhost:9007";

    @ManyToMany(cascade = CascadeType.REFRESH)
    private Set<Shop> shops = new HashSet<Shop>();//用集合类来存放Shop

    @JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
    //JoinTable就是定义中间表的名字以及关联字段名
    public Set<Shop> getShops() {
        return shops;
    }

    public void setShops(Set<Shop> shop) {
        this.shops = shop;
    }

    public void removeShop(Shop shop) {
        // 要能判断出是否包含在Set中，必须重写shop中的hashCode方法和equals方法
        if (shops.contains(shop)) {
            shops.remove(shop);
        }
    }

    public void addShop(Shop shop) {
        shops.add(shop);
    }

    /**
     * 商品编号
     */
    public String no;
    /**
     * 商品名称
     */
    public String name;
    /**
     * 所属商户ID
     */
    @Column(name = "company_id")
    public String companyId;
    //    @OneToMany(mappedBy = "companyId")
    //    public Set<Shop> shops = new HashSet<Shop>(0);
    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

    //    @Transient
    //    private String image_tiny_path;
    //    private String image_small_path;
    //    private String image_middle_path;
    //    private String image_large_path;

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
    @Column(name = "expired_bg_on")
    public String expiredBeginOn;
    /**
     * 券有效结束日
     */
    @Column(name = "expired_ed_on")
    public String expiredEndOn;
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
    /**
     * 温馨提示
     */
    public String prompt;
    /**
     * 商品详情
     */
    public String details;
    /**
     * 售出数量
     */
    @Column(name = "sale_count")
    public String saleCount;
    /**
     * 售出基数
     */
    @Column(name = "base_sale")
    public String baseSale;
    /**
     * 商品状态,
     */
    public String status;
    /**
     * 创建来源
     */
    @Column(name = "created_from")
    public String createdFrom;
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public String createdAt;
    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;
    /**
     * 修改时间
     */
    @Column(name = "update_at")
    public String updateAt;
    /**
     * 修改人
     */
    @Column(name = "update_by")
    public String updateBy;
    /**
     * 逻辑删除
     */
    public String deleted;
    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    public int lockVersion;
    /**
     * 手工排序
     */
    @Column(name = "display_order")
    public String displayOrder;
    @Transient
    public String salePriceBegin;
    @Transient
    public String salePriceEnd;
    @Transient
    public String saleCountBegin;
    @Transient
    public String saleCountEnd;
    /**
     * 商品类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    public MaterialType materialType;

    private String getImageBySizeType(String sizeType) {
        String defaultImage = IMAGE_SERVER + "/p/1/1/1/default_" + sizeType + ".png";
        if (imagePath == null || imagePath.equals("")) {
            return defaultImage;
        }
        Matcher matcher = imagePat.matcher(imagePath);
        if (!matcher.matches()) {


            return defaultImage;
        }
        String imageHeadStr = imagePath.replace("/o/", "/p/");
        return IMAGE_SERVER + imageHeadStr.replace("/" + matcher.group(4), "/" + matcher.group(4) + "_" + sizeType);
    }

    /**
     * 根据商品分类和数量取出指定数量的商品.
     *
     * @param categoryId
     * @param limit
     * @return
     */
    public static List<Goods> findTopByCategory(int categoryId, int limit) {
        //todo 商品状态判断
        return find("").fetch(limit);
    }

    /**
     * 查询商品一览
     *
     * @param goods
     * @return
     */
    public List query(models.sales.Goods goods) {
        StringBuffer condtion = new StringBuffer();
        condtion.append(" deleted= ? ");
        List params = new ArrayList();
        params.add("0");
        if (goods.name != null && !"".equals(goods.name)) {
            condtion.append(" and name like ? ");
            params.add("%" + goods.name + "%");
        }
        if (goods.no != null && !"".equals(goods.no)) {
            condtion.append(" and no like ? ");
            params.add("%" + goods.no + "%");
        }
        if (goods.status != null && !"".equals(goods.status)) {
            condtion.append(" and status = ? ");
            params.add(goods.status);
        }
        if (goods.salePriceBegin != null && !"".equals(goods.salePriceBegin)) {
            condtion.append(" and salePrice >= ?");
            params.add(goods.salePriceBegin);
        }
        if (goods.salePriceEnd != null && !"".equals(goods.salePriceEnd)) {
            condtion.append(" and salePrice <= ?");
            params.add(goods.salePriceEnd);
        }
        if (goods.saleCountBegin != null && !"".equals(goods.saleCountBegin)) {
            condtion.append(" and saleCount >= ?");
            params.add(goods.saleCountBegin);
        }
        if (goods.saleCountEnd != null && !"".equals(goods.saleCountEnd)) {
            condtion.append(" and saleCount <= ?");
            params.add(goods.saleCountEnd);
        }
        JPAQuery query = null;
        List list = null;
        query = goods.find(condtion.toString(), params.toArray());
        list = query.fetch();
        return list;
    }
}
