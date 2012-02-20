/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.sales.GoodsShop;
import models.sales.Shop;
import play.mvc.Controller;
import util.FileUploadUtil;

/**
 * 通用说明：
 *
 * @author yanjy
 * @version 1.0 02/8/12
 */
public class Goods extends Controller {

	/**
	 * 展示商品一览页面
	 */
	public static void index(models.sales.Goods goods) {

		List list= goods.query(goods);
		renderTemplate("sales/Goods/index.html",list);
	}

	/**
	 * 展示添加商品页面
	 */
	public static void add() {
		List<Shop> list = Shop.findShopByCompany(Long.parseLong("1"));
		renderTemplate("sales/Goods/add.html",list);
	}

	/**
	 *  添加商品
	 * @param image_path
	 * @param goods
	 */
	public static void create(File image_path,models.sales.Goods goods,String radios,String status,Long checkoption[]) {
		if (validation.hasErrors()) {
			error("Validation errors");
		}

		//添加商品处理
		goods.status = status;
		goods.companyId = "1";
		goods.lockVersion=0;
		if (image_path !=null && image_path.getName() !=null ) {
			//取得文件存储路径
			String storepath = play.Play.configuration.get("upload.imagepath").toString();
			//上传文件
			new FileUploadUtil().storeImage(image_path,storepath+"/1/1/1/");
			goods.imagePath = "/1/1/1/" + image_path.getName();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String datestr = sdf.format(new Date());
		goods.deleted="0";
		goods.createdAt = datestr;
		goods.createdBy = "yanjy";
		goods.create();

		//全部门店的场合
		GoodsShop goods_shop = new GoodsShop();
		if ("1".equals(radios)) {
			List<Shop> list = Shop.findShopByCompany(Long.parseLong("1"));
			for (Shop shop:list) {
				goods_shop = new GoodsShop();
				goods_shop.shopId =shop.id;
				goods_shop.goodsId =goods.id;
				goods_shop.save();
			}
		}else{
			//部分门店
			for (Long id:checkoption) {
				goods_shop = new GoodsShop();
				goods_shop.shopId =id;
				goods_shop.goodsId =goods.id;
				goods_shop.create();
			}
		}
		index(null);
	}

	/**
	 * 取得指定商品信息
	 */
	public static void edit(String id) {
		models.sales.Goods goods= models.sales.Goods.findById(Long.parseLong(id));
		List<Shop> list = Shop.findShopByCompany(Long.parseLong("1"));
		renderTemplate("sales/Goods/edit.html",goods,list);

	}
	/**
	 * 取得指定商品信息
	 */
	public static void detail(String id) {
		models.sales.Goods goods= models.sales.Goods.findById(Long.parseLong(id));
		renderTemplate("sales/Goods/detail.html",goods);
	}

	/**
	 *  更新指定商品信息
	 * 
	 * @param id
	 */
	public static void update(String id,File image_path,models.sales.Goods goods) {
		models.sales.Goods update_goods = models.sales.Goods.findById(Long.parseLong(id));

		if (image_path !=null && image_path.getName() !=null ) {
			//取得文件存储路径
			String storepath = play.Play.configuration.get("upload.imagepath").toString();
			//上传文件
			new FileUploadUtil().storeImage(image_path,storepath+"/1/1/1/");
			update_goods.imagePath = "/1/1/1/" + image_path.getName();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String datestr = sdf.format(new Date());
		update_goods.name = goods.name;
		update_goods.no = goods.no;
		update_goods.expiredBeginOn = goods.expiredBeginOn;
		update_goods.expiredEndOn = goods.expiredEndOn;
		update_goods.originalPrice = goods.originalPrice;
		update_goods.salePrice = goods.salePrice;
		update_goods.baseSale = goods.baseSale;
		update_goods.prompt = goods.prompt;
		update_goods.details = goods.details;
		update_goods.updateAt = datestr;
		update_goods.updateBy = "ytrr";
		update_goods.save();

		index(null);
	}

	/**
	 *  上下架指定商品
	 * 
	 * @param id
	 */
	public static void updateStatus(Long checkoption[],String status) {
		//更新处理
		for (Long id:checkoption) {
			models.sales.Goods goods= models.sales.Goods.findById(id);
			goods.status=status;
			goods.save();
		}

		index(null);
	}

	/**
	 * 删除指定商品
	 * @param id
	 */
	public static void delete(Long checkoption[]) {
		for (Long id:checkoption) {
			models.sales.Goods.delete("id=?",id);
		}
		index(null);
	}

}
