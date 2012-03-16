/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import models.sales.*;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.Scope;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import play.mvc.With;
import navigation.annotations.ActiveNavigation;
import controllers.modules.cas.SecureCAS;

/**
 * 通用说明：
 *
 * @author yanjy
 * @version 1.0 02/8/12
 */
@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("goods_index")
public class Goods extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 展示商品一览页面
     */
    public static void index(models.sales.GoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null){
            condition = new GoodsCondition();
        }

        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        renderArgs.put("condition", condition);
        render(goodsPage);
    }

    /**
     * 展示添加商品页面
     */
    @ActiveNavigation("goods_add")
    public static void add() {
        Long companyId = getCompanyId();
        List<Shop> shopList = Shop.findShopByCompany(companyId);
        List<Brand> brandList = Brand.findByCompanyId(companyId);
        List<Category> categoryList = Category.findByParent(0);
        List<Category> subCategoryList = new ArrayList<>();
        if (categoryList.size() > 0) {
            subCategoryList = Category.findByParent(categoryList.get(0).id);
        }

        Goods goods = new Goods();
        Scope.Flash.current().put("goods", goods);
        renderArgs.put("isAllShop", true);
        render(shopList, brandList, categoryList, subCategoryList);
    }

    private static Long getCompanyId() {
        //todo
        return 1l;
    }

    /**
     * 展示添加商品页面
     */
    public static void preview(Long id) {
        redirect("http://www.uhuiladev.com/goods/" + id + "?preview=true");
    }

    /**
     * 添加商品
     * 商户只能添加电子券.
     *
     * @param imagePath
     * @param goods
     * @param topCategoryId 顶层分类Id
     * @param isAllShop     是否全部门店
     */
    public static void create(@Required File imagePath, @Valid models.sales.Goods goods, Long topCategoryId,
                              boolean isAllShop) {
        Long companyId = getCompanyId();
        if (isAllShop && goods.shops != null) {
            goods.shops = null;
        }

        //检查目录
        File uploadDir = new File(UploadFiles.ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            Validation.addError("goods.imagePath", "validation.write");
            System.out.println(uploadDir.getAbsolutePath());
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            Validation.addError("goods.imagePath", "validation.write");
        }

        //检查文件大小
        if (imagePath != null) {
            if (imagePath.length() > UploadFiles.MAX_SIZE) {
                Validation.addError("goods.imagePath", "validation.maxFileSize");
            }

            //检查扩展名
            //定义允许上传的文件扩展名
            String[] fileTypes = UploadFiles.FILE_TYPES.trim().split(",");
            String fileExt = imagePath.getName().substring(imagePath.getName().lastIndexOf(".") + 1).toLowerCase();
            if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
                Validation.addError("goods.imagePath", "validation.invalidType", StringUtils.join(fileTypes));
            }
        }


        if (Validation.hasErrors()) {
            System.out.println("has Error");
            for (String key : validation.errorsMap().keySet()) {
                System.out.println(key+": "+validation.errorsMap().get(key));
            }
            String shopIds = "";
            if (goods.shops != null) {
                for (Shop shop : goods.shops) {
                    shopIds += shop.id + ",";
                    renderArgs.put("isAllShop", false);
                }
            } else {
                renderArgs.put("isAllShop", true);
            }

            List<Shop> shopList = Shop.findShopByCompany(companyId);
            List<Brand> brandList = Brand.findByCompanyId(companyId);
            List<Category> categoryList = Category.findByParent(0);
            List<Category> subCategoryList = new ArrayList<>();
            if (categoryList.size() > 0) {
                subCategoryList = Category.findByParent(categoryList.get(0).id);
            }
            Long categoryId = 0L;
            if (goods.categories != null && goods.categories.size() > 0 && goods.categories.iterator() != null && goods
                    .categories.iterator().hasNext()) {
                categoryId = goods.categories.iterator().next().id;
            }
            render("Goods/add.html", shopList, brandList, categoryList, subCategoryList, goods, topCategoryId,
                    categoryId, shopIds);
        }

        System.out.println("Begin to add goods");
        //添加商品处理
        goods.companyId = companyId;
        goods.createdBy = getCompanyUser();
        goods.deleted = DeletedStatus.UN_DELETED;
        goods.saleCount = 0;
        goods.incomeGoodsCount = 0L;
        goods.createdAt = new Date();
        goods.materialType = MaterialType.ELECTRONIC;


        if (goods.baseSale == 0) {
            goods.status = GoodsStatus.OFFSALE;
        }
        goods.create();
        try {
            uploadImagePath(imagePath, goods);
        } catch (IOException e) {
            e.printStackTrace();
            error("goods.image_upload_failed");
        }
        goods.filterShops();
        goods.save();


        //预览的情况
        if (GoodsStatus.UNCREATED.equals(goods.status)) {
            redirect("http://www.uhuiladev.com/goods/" + goods.id + "?preview=true");
        }
        index(null);
    }

    /**
     * 上传图片
     *
     * @param uploadImageFile
     * @param goods
     */
    private static void uploadImagePath(File uploadImageFile, models.sales.Goods goods) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return;
        }
        //取得文件存储路径

        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, goods.id, UploadFiles.ROOT_PATH);
        goods.imagePath = absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 取得指定商品信息
     */
    public static void edit(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        List<Shop> shopList = Shop.findShopByCompany(goods.companyId);
        List<Brand> brandList = Brand.findByCompanyId(goods.companyId);
        render(goods, shopList, brandList);
    }

    /**
     * 取得指定商品信息
     */
    public static void detail(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        renderTemplate("Goods/detail.html", goods);
    }

    /**
     * 更新指定商品信息
     *
     * @param id
     */
    public static void update(Long id, File uploadImageFile, models.sales.Goods goods) {
        String companyUser = getCompanyUser();
        models.sales.Goods updateGoods = models.sales.Goods.findById(id);

        try {
            uploadImagePath(uploadImageFile, updateGoods);
        } catch (IOException e) {
            error("goods.image_upload_failed");
        }

        updateGoods.name = goods.name;
        updateGoods.no = goods.no;
        updateGoods.effectiveAt = goods.effectiveAt;
        updateGoods.expireAt = goods.expireAt;
        updateGoods.originalPrice = goods.originalPrice;
        updateGoods.salePrice = goods.salePrice;
        updateGoods.baseSale = goods.baseSale;
        updateGoods.setPrompt(goods.getPrompt());
        updateGoods.setDetails(goods.getDetails());
        updateGoods.updatedAt = new Date();
        updateGoods.updatedBy = companyUser;
        updateGoods.brand = goods.brand;
        updateGoods.save();

        index(null);
    }

    private static String getCompanyUser() {
        //todo
        return "燕井允";
    }

    /**
     * 上下架指定商品
     */
    public static void updateStatus(GoodsStatus status, Long... ids) {
        //更新处理
        models.sales.Goods.updateStatus(status, ids);

        index(null);
    }

    /**
     * 删除指定商品
     */
    public static void delete(Long... ids) {
        models.sales.Goods.delete(ids);

        index(null);
    }

}
