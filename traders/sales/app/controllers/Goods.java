/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import java.io.File;
import java.util.List;

import CommonUtil.Common;

import play.cache.Cache;
import play.mvc.Controller;

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
	public static void index() {
		List list= models.sales.Goods.findAll();
		renderTemplate("sales/Goods/index.html",list);
	}

	/**
	 * 展示添加商品页面
	 */
	public static void add() {
		renderTemplate("sales/Goods/add.html");
	}

	/**
	 *  添加商品
	 * @param image_path
	 * @param goods
	 */
	public static void create(File image_path,models.sales.Goods goods,String flag) {
		if (validation.hasErrors()) {
			error("Validation errors");
		}
		//默认商品下架状态，点击保存并上架状态为1
		String status ="0";
		if ("1".equals(flag)) {
			status ="1";
		}

		if (image_path !=null && image_path.getName() !=null ) {
			//取得文件存储路径
			String storepath = play.Play.configuration.get("upload.imagepath").toString();
			//上传文件
			new Common().storeImage(image_path,storepath+"/1/1/1/");
		}

		//添加商品处理
		models.sales.Goods.addGoods(image_path,goods,status);
		index();
	}

	/**
	 * 取得指定商品信息
	 */
	public static void edit(String id) {
		models.sales.Goods goods= models.sales.Goods.findById(Long.parseLong(id));
		renderTemplate("sales/Goods/edit.html",goods);

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
		if (image_path !=null && image_path.getName() !=null ) {
			//取得文件存储路径
			String storepath = play.Play.configuration.get("upload.imagepath").toString();
			//上传文件
			new Common().storeImage(image_path,storepath+"/1/1/1/");
		}
		//更新处理
		models.sales.Goods.updateGoods(id,image_path,goods);
		index();
	}

	/**
	 *  上下架指定商品
	 * 
	 * @param id
	 */
	public static void updateStatus(Long checkoption[],String status) {
		//更新处理
		for (Long id:checkoption) {
			System.out.println("id="+id);
			models.sales.Goods goods= models.sales.Goods.findById(id);
			goods.status=status;
			goods.save();
		}

		index();
	}

	/**
	 * 删除指定商品
	 * @param id
	 */
	public static void delete(Long checkoption[]) {
		for (Long id:checkoption) {
			models.sales.Goods.delete("id=?",id);
		}
		index();
	}
}
