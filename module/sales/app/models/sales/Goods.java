/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import com.uhuila.common.constants.DeletedStatus;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "goods")
public class Goods extends Model {

	private static final Pattern imagePat = Pattern.compile("^/([0-9]+)/([0-9]+)/([0-9]+)/([^_]+).(jpg|png|gif|jpeg)$");
	private static final String IMAGE_SERVER;

	static {
		Object oImageServer = play.Play.configuration.get("image.server");
		if (oImageServer != null) {
			IMAGE_SERVER = oImageServer.toString();
		} else {
			IMAGE_SERVER = "http://img0.uhlcdndev.net";
		}
	}


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

	@ManyToMany(cascade = CascadeType.REFRESH)
	@JoinTable(name = "goods_shops", inverseJoinColumns = @JoinColumn(name = "shop_id"), joinColumns = @JoinColumn(name = "goods_id"))
	public Set<Shop> shops;


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
    public String effectiveAt;
    /**
     * 券有效结束日
     */
    @Column(name = "expire_at")
    public String expireAt;
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

	@Transient
	public String getDiscount() {
		if (originalPrice != null && originalPrice.compareTo(new BigDecimal(0)) >	0) {
			BigInteger discount = salePrice.divide(originalPrice).multiply(new BigDecimal(100)).toBigInteger();
			if (discount.intValue() == 100) {
				return "";
			}
			if (discount.mod(new BigInteger("10")).intValue() == 0) {
				return discount.divide(new BigInteger("10")).toString();
			}
			return discount.toString();
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
	public String saleCount;
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
	public String createdAt;
	/**
	 * 创建人
	 */
	@Column(name = "created_by")
	public String createdBy;
	/**
	 * 修改时间
	 */
	@Column(name = "updated_at")
	public String updatedAt;
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
		System.out.println("defaultImage=" + defaultImage);
		if (imagePath == null || imagePath.equals("")) {
			return defaultImage;
		}
		System.out.println("filePath=" + imagePath);
		Matcher matcher = imagePat.matcher(imagePath);
		if (!matcher.matches()) {
			System.out.println("filePat not match");
			return defaultImage;
		}
		String imageHeadStr = "/p" + imagePath;
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
	 * 查询商品一览
	 *
	 * @param goods
	 * @return
	 */
	public List query(models.sales.Goods goods,Pager pager) {
		StringBuilder condition = new StringBuilder();
		condition.append(" deleted= ? ");
		List<Object> params = new ArrayList();
		params.add(Integer.parseInt("0"));
		if (goods.name != null && !"".equals(goods.name)) {
			condition.append(" and name like ? ");
			params.add("%" + goods.name + "%");
		}
		if (goods.no != null && !"".equals(goods.no)) {
			condition.append(" and no like ? ");
			params.add("%" + goods.no + "%");
		}
		if (goods.status != null && !"".equals(goods.status)) {
			condition.append(" and status = ? ");
			params.add(goods.status);
		}
		if (goods.salePriceBegin != null && !"".equals(goods.salePriceBegin)) {
			condition.append(" and salePrice >= ?");
			params.add(new BigDecimal(goods.salePriceBegin));
		}
		if (goods.salePriceEnd != null && !"".equals(goods.salePriceEnd)) {
			condition.append(" and salePrice <= ?");
			params.add(new BigDecimal(goods.salePriceEnd));
		}
		if (goods.saleCountBegin != null && !"".equals(goods.saleCountBegin)) {
			condition.append(" and saleCount >= ?");
			params.add(goods.saleCountBegin);
		}
		if (goods.saleCountEnd != null && !"".equals(goods.saleCountEnd)) {
			condition.append(" and saleCount <= ?");
			params.add(goods.saleCountEnd);
		}
		condition.append(" order by created_at desc");
		pager.totalCount = goods.count(condition.toString(),params.toArray());
		pager.totalPager();
		return goods.find(condition.toString(), params.toArray()).fetch(pager.currPage, pager.pageSize);
	}
}
