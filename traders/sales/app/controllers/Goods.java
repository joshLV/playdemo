/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import models.sales.GoodsStatus;
import models.sales.Pager;
import models.sales.Shop;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Controller;
import util.FileUploadUtil;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.PathUtil;



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
	public static void index(models.sales.Goods goods,String page) {
		Pager pager = new Pager();
		if(page != null){
			pager.currPage = Integer.parseInt(page);
		}
		List list = goods.query(goods,pager);
		renderTemplate("sales/Goods/index.html", list,pager);
	}

	/**
	 * 展示添加商品页面
	 */
	public static void add() {
		String companyId="1";
		List<Shop> list = Shop.findShopByCompany(Long.parseLong(companyId));
		renderTemplate("sales/Goods/add.html", list);
	}
	/**
	 * 展示添加商品页面
	 */
	public static void preview(Long id) {
		redirect("http://www.uhuiladev.com/goods/"+id+"?preview=true");
	}

	/**
	 * 添加商品
	 *
	 * @param imagePath
	 * @param goods
	 */
	public static void create(@Required File imagePath, @Valid models.sales.Goods goods,String radios, int status,
			Long checkoption[]) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			renderTemplate("sales/Goods/add.html");
		}
		String companyId="1";
		//添加商品处理
		goods.status = status == 1 ? GoodsStatus.ONSALE:GoodsStatus.OFFSALE;
		goods.companyId = companyId;
		goods.deleted = DeletedStatus.UN_DELETED;
		goods.saleCount = 1;
		goods.details=	htmlspecialchars(goods.details);
		goods.prompt=	htmlspecialchars(goods.prompt);
		goods.createdAt = new Date();
		goods.createdBy = "yanjy";
		goods.create();
		uploadImagePath(imagePath, goods);
		goods.save();

		//全部门店的场合
		if ("1".equals(radios)) {
			List<Shop> list = Shop.findShopByCompany(Long.parseLong(companyId));
			for (Shop shop : list) {
				goods.addValues(shop);
			}
		} else {
			Shop shop = new Shop();
			//部分门店
			for (Long id : checkoption) {
				shop.id=id;
				goods.addValues(shop);
			}
		}

		//预览的情况
		if ("2".equals(status)) {
			redirect("http://www.uhuiladev.com/goods/"+goods.id+"?preview=true");
		}
		index(null,"1");
	}

	
	private static String htmlspecialchars(String str) {
		str = str.replaceAll("&", "&amp;");
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("\"", "&quot;");
		return str;
	}
	/**
	 * 上传图片
	 * 
	 * @param uploadImageFile
	 * @param goods
	 */
	private static void uploadImagePath(File uploadImageFile, models.sales.Goods goods) {
		if (uploadImageFile != null && uploadImageFile.getName() != null) {
			//取得文件存储路径
			String storepath = play.Play.configuration.get("upload.imagepath").toString();
			//上传文件
			String path = PathUtil.getPathById(goods.id);
			String uploadImageFileName = uploadImageFile.getName();
			String extName = play.Play.configuration.get("imageExtName").toString();;
			if (uploadImageFileName.indexOf(".") > 0) {
				extName = uploadImageFileName.substring(uploadImageFileName.lastIndexOf(".") + 1, uploadImageFileName.length());
			}
			String baseFileName = "origin." + extName;
			new FileUploadUtil().storeImage(uploadImageFile, storepath + path, baseFileName);
			goods.imagePath = path + baseFileName;
		}
	}

	/**
	 * 取得指定商品信息
	 */
	public static void edit(Long id) {
		models.sales.Goods goods = models.sales.Goods.findById(id);
		System.out.println(goods.details);
		System.out.println(">>>>"+htmlspecialchars(goods.details));
		goods.details=	htmlspecialchars(goods.details);
		goods.prompt=	htmlspecialchars(goods.prompt);
		List<Shop> list = Shop.findShopByCompany(Long.parseLong(goods.companyId));
		renderTemplate("sales/Goods/edit.html", goods, list);
	}

	/**
	 * 取得指定商品信息
	 */
	public static void detail(Long id) {
		models.sales.Goods goods = models.sales.Goods.findById(id);
		renderTemplate("sales/Goods/detail.html", goods);
	}

	/**
	 * 更新指定商品信息
	 *
	 * @param id
	 */
	public static void update(Long id, File uploadImageFile, models.sales.Goods goods) {
		models.sales.Goods updateGoods = models.sales.Goods.findById(id);

		uploadImagePath(uploadImageFile, updateGoods);

		updateGoods.name = goods.name;
		updateGoods.no = goods.no;
		updateGoods.effectiveAt = goods.effectiveAt;
		updateGoods.expireAt = goods.expireAt;
		updateGoods.originalPrice = goods.originalPrice;
		updateGoods.salePrice = goods.salePrice;
		updateGoods.baseSale = goods.baseSale;
		updateGoods.prompt = goods.prompt;
		updateGoods.details = goods.details;
		updateGoods.updatedAt = new Date();
		updateGoods.updatedBy = "ytrr";
		updateGoods.save();

		index(null,"1");
	}

	/**
	 * 上下架指定商品
	 */
	public static void updateStatus(Long checkoption[], GoodsStatus status) {
		//更新处理
		for (Long id : checkoption) {
			models.sales.Goods goods = models.sales.Goods.findById(id);
			goods.status = status;
			goods.save();
		}

		index(null,"1");
	}

	/**
	 * 删除指定商品
	 */
	public static void delete(Long checkoption[]) {
		System.out.println("<<<<<<<<<<<<"+checkoption);
		for (Long id : checkoption) {
			System.out.println(">>>>>>>>>>>>>>."+id);
			models.sales.Goods goods =	models.sales.Goods.findById(id);
			goods.deleted=DeletedStatus.DELETED;
			goods.save();
		}
		index(null,"1");
	}

}
