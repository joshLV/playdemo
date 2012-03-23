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
import controllers.supplier.cas.SecureCAS;

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

        if (condition == null) {
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
        renderInit(null);
        render();
    }

    /**
     * 初始化form界面.
     * 添加和修改页面共用
     *
     * @param goods
     */
    private static void renderInit(models.sales.Goods goods) {
        if (goods == null) {
            goods = new models.sales.Goods();
        }
        if (goods.isAllShop != null) {
            if (goods.isAllShop && goods.shops != null) {
                goods.shops = null;
            }
        } else {
            goods.isAllShop = false;
        }

        Long supplierId = getSupplierId();
        String shopIds = "";
        if (goods.shops != null) {
            for (Shop shop : goods.shops) {
                shopIds += shop.id + ",";
                goods.isAllShop = false;
            }
        } else {
            goods.isAllShop = true;
        }

        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        List<Brand> brandList = Brand.findByOrder();

        List<Category> categoryList = Category.findByParent(0);//获取顶层分类
        List<Category> subCategoryList = new ArrayList<>();
        Long categoryId = 0L;
        if (categoryList.size() > 0) {
            if (goods.categories != null && goods.categories.size() > 0 && goods.categories.iterator() != null && goods
                    .categories.iterator().hasNext()) {
                Category category = goods.categories.iterator().next();
                categoryId = category.id;
                if ((goods.topCategoryId == null || goods.topCategoryId == 0) && category.parentCategory!=null) {
                    goods.topCategoryId = category.parentCategory.id;
                }
            }
            if (goods.topCategoryId == null) {
                goods.topCategoryId = categoryList.get(0).id;
            }
            subCategoryList = Category.findByParent(goods.topCategoryId);
        }
        for (String key : validation.errorsMap().keySet()) {
            System.out.println("validation.errorsMap().get(key):" + validation.errorsMap().get(key));
        }
        renderArgs.put("shopList", shopList);
        renderArgs.put("brandList", brandList);
        renderArgs.put("categoryList", categoryList);
        renderArgs.put("subCategoryList", subCategoryList);
        renderArgs.put("categoryId", categoryId);
        renderArgs.put("shopIds", shopIds);
    }

    private static Long getSupplierId() {
        //todo
        return 1l;
    }

    /**
     * 展示添加商品页面
     */
    public static void preview(Long id) {
        redirect("http://www.uhuila.cn/goods/" + id + "?preview=true");
    }

    /**
     * 添加商品
     * 商户只能添加电子券.
     *
     * @param imagePath
     * @param goods
     */
    @ActiveNavigation("goods_add")
    public static void create(@Valid models.sales.Goods goods, @Required File imagePath) {
        Long supplierId = getSupplierId();

        checkImageFile(imagePath);

        if (Validation.hasErrors()) {
            renderInit(goods);
            render("Goods/add.html");
        }

        //添加商品处理
        goods.supplierId = supplierId;
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
            redirect("http://www.uhuila.cn/goods/" + goods.id + "?preview=true");
        }
        index(null);
    }

    private static void checkImageFile(File imagePath) {
        if (imagePath != null) {
            //检查目录
            File uploadDir = new File(UploadFiles.ROOT_PATH);
            if (!uploadDir.isDirectory()) {
                Validation.addError("goods.imagePath", "validation.write");
            }

            //检查目录写权限
            if (!uploadDir.canWrite()) {
                Validation.addError("goods.imagePath", "validation.write");
            }

            if (imagePath.length() > UploadFiles.MAX_SIZE) {
                Validation.addError("goods.imagePath", "validation.maxFileSize");
            }

            //检查扩展名
            //定义允许上传的文件扩展名
            String[] fileTypes = UploadFiles.FILE_TYPES.trim().split(",");
            String fileExt = imagePath.getName().substring(imagePath.getName().lastIndexOf(".") + 1).toLowerCase();
            if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
                Validation.addError("goods.imagePath", "validation.invalidType", StringUtils.join(fileTypes, ','));
            }
        }
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
        renderInit(goods);
        render(goods, id);
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
     */
    public static void update(Long id, @Valid models.sales.Goods goods, File imagePath) {
        System.out.println("goods.details:" + goods.getDetails());
        if (goods.isAllShop && goods.shops != null) {
            goods.shops = null;
        }
        goods.id = id;
        checkImageFile(imagePath);

        if (Validation.hasErrors()) {
            renderInit(goods);
            render("Goods/edit.html", goods);
        }

        String companyUser = getCompanyUser();
        models.sales.Goods updateGoods = models.sales.Goods.findById(id);

        try {
            uploadImagePath(imagePath, updateGoods);
        } catch (IOException e) {
            error("goods.image_upload_failed");
        }

        updateGoods.name = goods.name;
        updateGoods.no = goods.no;
        updateGoods.categories = goods.categories;
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
        updateGoods.isAllShop = goods.isAllShop;
        updateGoods.save();

        //预览的情况
        if (GoodsStatus.UNCREATED.equals(goods.status)) {
            redirect("http://www.uhuila.cn/goods/" + goods.id + "?preview=true");
        }
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
