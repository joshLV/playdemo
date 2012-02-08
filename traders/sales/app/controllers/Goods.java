/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import models.GoodsModel;

import org.apache.commons.codec.digest.DigestUtils;

import play.mvc.Controller;

/**
 * 通用说明：
 *
 * @author yanjy
 *
 * @version 1.0 02/8/12
 */
public class Goods extends Controller{
	
	/**
	 * 展示添加商品页面
	 */
	public static void goodsIndex() {
		render("goods/addgoods.html");
	}
	
	/**
	 * 添加商品
	 */
	public static void addgoods() {
		System.out.println("aaaaaaaaaaaa");
		GoodsModel goods= new GoodsModel();
		goods.no=	params.get("goods_no");
		goods.name=	params.get("goods_name");
		goods.company_id="1";
		goods.expired_bg_on=	params.get("expired_bg_on");
		goods.expired_ed_on=	params.get("expired_ed_on");
		goods.original_price=	params.get("original_price");
		goods.sale_price=	params.get("sale_price");
		
		goods.original_price=	params.get("original_price");
		goods.sale_price=	params.get("sale_price");
		
		goods.base_sale=	params.get("base_sale");
		goods.image_path=	params.get("image_path");
		
		goods.prompt=	params.get("prompt");
		goods.details=	params.get("details");
		goods.save();
		
		goodsIndex();
		//render("goods/goodslist.html");
	}
}
