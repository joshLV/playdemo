/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import java.io.File;
import java.util.Date;
import java.util.List;

import models.sales.Brand;
import models.sales.Category;
import models.sales.GoodsStatus;
import models.sales.Shop;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.data.validation.Valid;
import play.modules.paginate.JPAExtPaginator;
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

	public static int PAGE_SIZE = 15;

	/**
	 * 展示商品一览页面
	 */
	public static void index(models.sales.Goods goods) {
		String page = request.params.get("page");
		int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
		JPAExtPaginator<models.sales.Goods> list = models.sales.Goods.query1(goods, pageNumber, PAGE_SIZE);
		renderTemplate("sales/Goods/index.html", list);
	}

	/**
	 * 展示添加商品页面
	 */
	public static void add() {
		Long companyId=1l;
		List<Shop> shopList = Shop.findShopByCompany(companyId);
		List<Brand> brandList = Brand.findByCompanyId(companyId);
		renderTemplate("sales/Goods/add.html", shopList,brandList);
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
	public static void create(@Required File imagePath, @Valid models.sales.Goods goods,String radios,String status,
			Long checkoption[]) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			renderTemplate("sales/Goods/add.html");
		}
		Long companyId=1l;
		//添加商品处理
		goods.status = "1".equals(status) ? GoodsStatus.ONSALE:GoodsStatus.OFFSALE;
		goods.companyId = companyId;
		goods.deleted = DeletedStatus.UN_DELETED;
		goods.saleCount = 0;
		goods.details=	goods.details;
		goods.prompt=	goods.prompt;
		goods.createdAt = new Date();
		goods.createdBy = "yanjy";
		goods.create();
		uploadImagePath(imagePath, goods);

		//全部门店的场合
		if ("1".equals(radios)) {
			List<Shop> list = Shop.findShopByCompany(companyId);
			for (Shop shop : list) {
				goods.addValues(shop);
			}
		} else {
			//部分门店
			for (Long id : checkoption) {
				Shop shop = new Shop();
				shop.id=id;
				goods.addValues(shop);
			}
		}

		goods.save();

		//预览的情况
		if ("2".equals(status)) {
			redirect("http://www.uhuiladev.com/goods/"+goods.id+"?preview=true");
		}
		index(null);
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
		List<Shop> shopList = Shop.findShopByCompany(goods.companyId);
		List<Brand> brandList = Brand.findByCompanyId(goods.companyId);
		renderTemplate("sales/Goods/edit.html", goods, shopList,brandList);
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
		updateGoods.brand = goods.brand;
		updateGoods.save();

		index(null);
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

		index(null);
	}

	/**
	 * 删除指定商品
	 */
	public static void delete(Long checkoption[]) {
		for (Long id : checkoption) {
			models.sales.Goods goods =	models.sales.Goods.findById(id);
			goods.deleted=DeletedStatus.DELETED;
			goods.save();
		}
		index(null);
	}

}
