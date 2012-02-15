/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "goods")
public class Goods extends Model {

	private static final Pattern imagePat = Pattern.compile("^/o/([0-9]+)/([0-9]+)/([0-9]+)/([^_]+).(jpg|png|gif|jpeg)$");
	private static final String IMAGE_SERVER = "http://localhost:9007";

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
	public String company_id;
	/**
	 * 原始图片路径
	 */
	public String image_path;
	/**
	 * 最小规格图片路径
	 */
	public String getImage_tiny_path() {
		return getImageBySizeType("tiny");
	}
	/**
	 * 小规格图片路径
	 */
	public String getImage_small_path() {
		return getImageBySizeType("small");
	}
	/**
	 * 中等规格图片路径
	 */
	public String getImage_middle_path() {
		return getImageBySizeType("middle");
	}
	/**
	 * 大规格图片路径
	 */
	public String getImage_large_path() {
		return getImageBySizeType("large");
	}

	private String getImageBySizeType(String sizeType) {
		String defaultImage = IMAGE_SERVER + "/p/1/1/1/default_" + sizeType + ".png";
		if (image_path == null || image_path.equals("")){
			return defaultImage;
		}
		Matcher matcher = imagePat.matcher(image_path);
		if (!matcher.matches()) {

			return defaultImage;
		}
		String imageHeadStr = image_path.replace("/o/", "/p/");
		return IMAGE_SERVER + imageHeadStr.replace("/" + matcher.group(4), "/" + matcher.group(4) + "_" + sizeType);
	}


	/**
	 * 进货量
	 */
	public String income_goods_count;
	/**
	 * 券有效开始日
	 */
	public String expired_bg_on;
	/**
	 * 券有效结束日
	 */
	public String expired_ed_on;
	/**
	 * 商品标题
	 */
	//    public String title;
	/**
	 * 商品原价
	 */
	public float original_price;
	/**
	 * 商品现价
	 */
	public float sale_price;
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
	public String sale_count;
	/**
	 * 售出基数
	 */
	public String base_sale;
	/**
	 * 商品状态,
	 */
	public String status;
	/**
	 * 创建来源
	 */
	public String created_from;
	/**
	 * 创建时间
	 */
	public String created_at;
	/**
	 * 创建人
	 */
	public String created_by;
	/**
	 * 修改时间
	 */
	public String update_at;
	/**
	 * 修改人
	 */
	public String update_by;
	/**
	 * 逻辑删除
	 */
	public String deleted;
	/**
	 * 乐观锁
	 */
	public String lock_version;
	/**
	 * 手工排序
	 */
	public String display_order;
	public String sale_price_s;
	public String sale_price_e;
	public String sale_count_s;
	public String sale_count_e;
	/**
	 * 商品类型，e:电子券，r:实物
	 */
	@Column(name="material_type")
	public String materialType;
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
	 * 更新商品处理
	 * 
	 * @param id
	 * @param file
	 * @param goods
	 */
	public static void updateGoods(String id, File file,
			models.sales.Goods goods) {
		models.sales.Goods update_goods= models.sales.Goods.findById(Long.parseLong(id));

		if (file !=null && file.getName() !=null ) {
			update_goods.image_path="/1/1/1/"+file.getName();
		}

		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		String datestr = sdf.format( new  Date());  
		update_goods.name=goods.name;
		update_goods.no=goods.no;
		update_goods.expired_bg_on=goods.expired_bg_on;
		update_goods.expired_ed_on=goods.expired_ed_on;
		update_goods.original_price=goods.original_price;
		update_goods.sale_price=goods.sale_price;
		update_goods.base_sale=goods.base_sale;
		update_goods.prompt=goods.prompt;
		update_goods.details=goods.details;
		update_goods.update_at=datestr;
		update_goods.update_by="ytrr";

		update_goods.save();		
	}

	/**
	 * 添加商品处理
	 * 
	 * @param file
	 * @param goods
	 */
	public static void addGoods(File file, models.sales.Goods goods,String status) {

		//默认商品下架状态
		goods.status=status;
		goods.company_id="1";
		if (file !=null && file.getName() !=null ) {
			goods.image_path="/1/1/1/"+file.getName();
		}
		goods.deleted="0";
		SimpleDateFormat sdf  =   new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		String datestr = sdf.format( new  Date());  
		goods.created_at=datestr;
		goods.created_by="yanjy";
		goods.create();		
	}

	/**
	 * 查询商品一览
	 * 
	 * @param goods
	 * @return
	 */
	public List query(models.sales.Goods goods){
		StringBuffer condtion=new StringBuffer();
		condtion.append(" deleted= ? ");
		List params=new ArrayList();
		params.add("0");
		if (goods.name !=null && !"".equals(goods.name)) {
			condtion.append(" and name like ? ");
			params.add("%"+goods.name+"%");
		}
		if (goods.no !=null && !"".equals(goods.no)) {
			condtion.append(" and no like ? ");
			params.add("%"+goods.no+"%");
		}
		if (goods.status !=null && !"".equals(goods.status)) {
			condtion.append(" and status = ? ");
			params.add(goods.status);
		}
		if (goods.sale_price_s !=null && !"".equals(goods.sale_price_s)) {
			condtion.append(" and sale_price >= ?");
			params.add(goods.sale_price_s);
		}
		if (goods.sale_price_e !=null && !"".equals(goods.sale_price_e)) {
			condtion.append(" and sale_price <= ?");
			params.add(goods.sale_price_e);
		}
		if (sale_count_s !=null && !"".equals(sale_count_s)) {
			condtion.append(" and sale_count >= ?");
			params.add(goods.sale_count_s);
		}
		if (sale_count_e !=null && !"".equals(sale_count_e)) {
			condtion.append(" and sale_count <= ?");
			params.add(goods.sale_count_e);
		}
		JPAQuery query=null;
		List list= null;
		query = goods.find(condtion.toString(),params.toArray());
		list = query.fetch();
		return list;
	}

}
