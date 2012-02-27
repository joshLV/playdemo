/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.PathUtil;
import models.sales.GoodsShop;
import models.sales.GoodsStatus;
import models.sales.Shop;
import play.mvc.Controller;
import util.FileUploadUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

        List list = goods.query(goods);
        renderTemplate("sales/Goods/index.html", list);
    }

    /**
     * 展示添加商品页面
     */
    public static void add() {
        List<Shop> list = Shop.findShopByCompany(Long.parseLong("1"));
        renderTemplate("sales/Goods/add.html", list);
    }

    /**
     * 添加商品
     *
     * @param imagePath
     * @param goods
     */
    public static void create(File imagePath, models.sales.Goods goods,
                              String radios, GoodsStatus status,
                              Long checkoption[]) {
        if (validation.hasErrors()) {
            error("Validation errors");
        }

        //添加商品处理
        goods.status = status;
        goods.companyId = "1";
        goods.lockVersion = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datestr = sdf.format(new Date());
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.createdAt = datestr;
        goods.createdBy = "yanjy";
        goods.create();
        uploadImagePath(imagePath, goods);
        goods.save();
        //全部门店的场合
        GoodsShop goods_shop = new GoodsShop();
        if ("1".equals(radios)) {
            List<Shop> list = Shop.findShopByCompany(Long.parseLong("1"));
            for (Shop shop : list) {
                goods_shop = new GoodsShop();
                goods_shop.shopId = shop.id;
                goods_shop.goodsId = goods.id;
                goods_shop.save();
            }
        } else {
            //部分门店
            for (Long id : checkoption) {
                goods_shop = new GoodsShop();
                goods_shop.shopId = id;
                goods_shop.goodsId = goods.id;
                goods_shop.create();
            }
        }
        index(null);
    }

    private static void uploadImagePath(File uploadImageFile, models.sales.Goods goods) {
        if (uploadImageFile != null && uploadImageFile.getName() != null) {
            //取得文件存储路径
            String storepath = play.Play.configuration.get("upload.imagepath").toString();
            //上传文件
            String path = PathUtil.getPathById(goods.id);
            System.out.println("path=" + path);
            System.out.println("uploadImageFile=" + uploadImageFile);
            String uploadImageFileName = uploadImageFile.getName();
            String extName = ".jpg";
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
    public static void edit(String id) {
        models.sales.Goods goods = models.sales.Goods.findById(Long.parseLong(id));
        List<Shop> list = Shop.findShopByCompany(Long.parseLong("1"));
        renderTemplate("sales/Goods/edit.html", goods, list);

    }

    /**
     * 取得指定商品信息
     */
    public static void detail(String id) {
        models.sales.Goods goods = models.sales.Goods.findById(Long.parseLong(id));
        renderTemplate("sales/Goods/detail.html", goods);
    }

    /**
     * 更新指定商品信息
     *
     * @param id
     */
    public static void update(String id, File uploadImageFile, models.sales.Goods goods) {
        models.sales.Goods updateGoods = models.sales.Goods.findById(Long.parseLong(id));

        uploadImagePath(uploadImageFile, updateGoods);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datestr = sdf.format(new Date());
        updateGoods.name = goods.name;
        updateGoods.no = goods.no;
        updateGoods.expiredBeginOn = goods.expiredBeginOn;
        updateGoods.expiredEndOn = goods.expiredEndOn;
        updateGoods.originalPrice = goods.originalPrice;
        updateGoods.salePrice = goods.salePrice;
        updateGoods.baseSale = goods.baseSale;
        updateGoods.prompt = goods.prompt;
        updateGoods.details = goods.details;
        updateGoods.updateAt = datestr;
        updateGoods.updateBy = "ytrr";
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
            models.sales.Goods.delete("id=?", id);
        }
        index(null);
    }

}
