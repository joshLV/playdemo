/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import controllers.operate.cas.SecureCAS;
import models.resale.ResalerLevel;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.math.BigDecimal.ZERO;

/**
 * 通用说明：
 *
 * @author yanjy
 * @version 1.0 02/8/12
 */
@With({SecureCAS.class, OperateRbac.class})
@ActiveNavigation("goods_index")
public class OperateGoods extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 展示商品一览页面
     */
    @ActiveNavigation("goods_index")
    public static void index(models.sales.GoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new GoodsCondition();
        }

        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);
        List<Supplier> supplierList = Supplier.findAll();

        renderArgs.put("condition", condition);
        render(goodsPage, supplierList);
    }

    /**
     * 展示添加商品页面
     */
    @ActiveNavigation("goods_add")
    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        renderArgs.put("supplierList", supplierList);

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
            BigDecimal[] levelPrices = new BigDecimal[ResalerLevel.values().length];
            for (BigDecimal levelPrice : levelPrices) {
                levelPrice = ZERO;
            }
            renderArgs.put("levelPrices", levelPrices);
        } else {
            BigDecimal[] levelPrices = new BigDecimal[ResalerLevel.values().length];
            for (int i = 0; i < levelPrices.length; i++) {
                GoodsLevelPrice levelPrice = goods.getLevelPrices().get(i);
                if (levelPrice == null) {
                    levelPrices[i] = ZERO;
                } else {
                    levelPrices[i] = levelPrice.price;
                }
            }
            renderArgs.put("levelPrices", levelPrices);
        }
        if (goods.isAllShop != null) {
            if (goods.isAllShop && goods.shops != null) {
                goods.shops = null;
            }
        } else {
            goods.isAllShop = false;
        }

        Long supplierId = OperateRbac.currentUser().id;
        String shopIds = "";
        if (goods.shops != null) {
            for (Shop shop : goods.shops) {
                shopIds += shop.id + ",";
                goods.isAllShop = false;
            }
        } else {
            goods.isAllShop = true;
        }
        Logger.info("shopIds=" + shopIds);

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
                if ((goods.topCategoryId == null || goods.topCategoryId == 0) && category.parentCategory != null) {
                    goods.topCategoryId = category.parentCategory.id;
                }
            }
            if (goods.topCategoryId == null) {
                goods.topCategoryId = categoryList.get(0).id;
            }
            subCategoryList = Category.findByParent(goods.topCategoryId);
        }
        for (String key : validation.errorsMap().keySet()) {
            Logger.warn("validation.errorsMap().get(key):" + validation.errorsMap().get(key));
        }
        renderArgs.put("shopList", shopList);
        renderArgs.put("brandList", brandList);
        renderArgs.put("categoryList", categoryList);
        renderArgs.put("subCategoryList", subCategoryList);
        renderArgs.put("categoryId", categoryId);
        renderArgs.put("shopIds", shopIds);
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
    public static void create(@Valid models.sales.Goods goods, @Required File imagePath, BigDecimal[] levelPrices) {

        checkImageFile(imagePath);

        goods.setLevelPrices(levelPrices);

        checkSalePrice(goods);
        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            renderArgs.put("supplierList", supplierList);
            renderInit(goods);
            render("OperateGoods/add.html");
        }

        //添加商品处理
        goods.createdBy = OperateRbac.currentUser().loginName;
        goods.materialType = MaterialType.ELECTRONIC;

        goods.create();
        try {
            goods.imagePath = uploadImagePath(imagePath, goods.id);
        } catch (IOException e) {
            System.out.println("e:" + e);
            e.printStackTrace();
            error(500, "goods.image_upload_failed");
        }
        goods.save();

        //预览的情况
        if (GoodsStatus.UNCREATED.equals(goods.status)) {
            redirect("http://www.uhuila.cn/goods/" + goods.id + "?preview=true");
        }
        index(null);
    }

    private static void checkSalePrice(Goods goods) {
        if (goods.salePrice == null) {
            Validation.addError("goods.salePrice", "validation.required");
        } else if (goods.salePrice.compareTo(new BigDecimal("0.01")) < 0) {
            Validation.addError("goods.salePrice", "validation.min", "0.01");
        }
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
     * @param goodsId
     */
    private static String uploadImagePath(File uploadImageFile, Long goodsId) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径

        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, goodsId, UploadFiles.ROOT_PATH);
        return absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 取得指定商品信息
     */
    public static void edit(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        Supplier supplier = goods.getSupplier();
        if (supplier != null) {
            if (DeletedStatus.DELETED.equals(supplier.deleted)) {
                supplierList.add(supplier);
            }
        }
        renderArgs.put("supplierList", supplierList);
        renderInit(goods);
        render(goods, id);
    }

    /**
     * 取得指定商品信息
     */
    public static void show(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        renderTemplate("OperateGoods/show.html", goods);
    }

    /**
     * 更新指定商品信息
     */
    public static void update(Long id, @Valid models.sales.Goods goods, File imagePath, BigDecimal[] levelPrices) {
        if (goods.isAllShop && goods.shops != null) {
            goods.shops = null;
        }
        checkImageFile(imagePath);

        goods.setLevelPrices(levelPrices);
        checkSalePrice(goods);
        if (Validation.hasErrors()) {

            List<Supplier> supplierList = Supplier.findUnDeleted();
            renderArgs.put("supplierList", supplierList);
            renderInit(goods);
            render("OperateGoods/edit.html", goods, id);
        }

        String supplierUser = OperateRbac.currentUser().loginName;

        try {
            String image = uploadImagePath(imagePath, id);
            if (!StringUtils.isEmpty(image)) {
                goods.imagePath = image;
            }

        } catch (IOException e) {
            e.printStackTrace();
            error("goods.image_upload_failed");
        }

        goods.updatedBy = supplierUser;
        Goods.update(id, goods);

        //预览的情况
        if (GoodsStatus.UNCREATED.equals(goods.status)) {
            redirect("http://www.uhuila.cn/goods/" + id + "?preview=true");
        }
        index(null);
    }

    /**
     * 上架商品.
     *
     * @param id 商品ID
     */
    public static void onSale(@As(",") Long... id) {
        updateStatus(GoodsStatus.ONSALE, id);
    }

    /**
     * 下架商品.
     *
     * @param id 商品ID
     */
    public static void offSale(@As(",") Long... id) {
        updateStatus(GoodsStatus.OFFSALE, id);
    }

    /**
     * 拒绝上架申请.
     *
     * @param id 商品ID
     */
    public static void reject(@As(",") Long... id) {
        updateStatus(GoodsStatus.REJECT, id);
    }

    /**
     * 上下架指定商品
     *
     * @param status 商品状态
     * @param ids    商品ID
     */
    private static void updateStatus(GoodsStatus status, Long... ids) {
        models.sales.Goods.updateStatus(status, ids);

        index(null);
    }


    /**
     * 删除指定商品
     *
     * @param id 商品ID
     */
    public static void delete(@As(",") Long... id) {
        models.sales.Goods.delete(id);

        index(null);
    }

}
