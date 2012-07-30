/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.resale.ResalerLevel;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
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
import java.util.*;

import static play.Logger.warn;

/**
 * 通用说明：
 *
 * @author yanjy
 * @version 1.0 02/8/12
 */
@With(OperateRbac.class)
@ActiveNavigation("goods_index")
public class OperateGoods extends Controller {

    public static int PAGE_SIZE = 15;
    public static String WWW_URL = Play.configuration.getProperty("www.url", "");

    /**
     * 展示商品一览页面
     */
    @ActiveNavigation("goods_index")
    public static void index(models.sales.GoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new GoodsCondition();
            condition.status = GoodsStatus.ONSALE;
        }
        if (condition.priority == 1) {
            condition.orderBy = "g.priority";
        } else {
            condition.orderBy = "g.createdAt";
        }
        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);
        List<Supplier> supplierList = Supplier.findAll();

        List<Brand> brandList = Brand.findByOrder(null);
        renderArgs.put("brandList", brandList);

        render(goodsPage, supplierList, condition);
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
        List<Supplier> supplierList = Supplier.findUnDeleted();

        if (goods == null) {
            goods = new models.sales.Goods();
            BigDecimal[] levelPrices = new BigDecimal[ResalerLevel.values().length];
            Arrays.fill(levelPrices, null);
            goods.setLevelPrices(levelPrices);
            goods.materialType = MaterialType.ELECTRONIC;
            goods.unPublishedPlatforms = new HashSet<>();
            if (supplierList != null && supplierList.size() > 0) {
                goods.supplierId = supplierList.get(0).id;
                checkShops(goods.supplierId);
                renderShopList(goods.supplierId);
            }
            renderArgs.put("goods.materialType", MaterialType.ELECTRONIC);
            renderArgs.put("selectAll", true);
        }
        renderShopList(goods.supplierId);

        String shopIds = ",";
        if (goods.shops != null && goods.shops.size() > 0) {
            for (Shop shop : goods.shops) {
                shopIds += shop.id + ",";
            }

            goods.isAllShop = false;
        } else {
            goods.isAllShop = true;
        }

        if (goods.supplierId != null) {
            List<Brand> brandList = Brand.findByOrder(new Supplier(goods.supplierId));
            renderArgs.put("brandList", brandList);
        }

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
        //调试用
        for (String key : validation.errorsMap().keySet()) {
            warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
        }

        renderArgs.put("supplierList", supplierList);
        renderArgs.put("categoryList", categoryList);
        renderArgs.put("subCategoryList", subCategoryList);
        renderArgs.put("categoryId", categoryId);

        renderArgs.put("shopIds", shopIds);
        renderArgs.put("isAllShop", goods.isAllShop);
        renderArgs.put("goods", goods);
    }

    /**
     * 展示添加商品页面
     */

    private static void preview(Long goodsId, Goods goods, File imagePath) {
        String cacheId = "0";
        try {
            cacheId = Goods.preview(goodsId, goods, imagePath, UploadFiles.ROOT_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            error(500, "goods.image_upload_failed");
        }
        cacheId = play.cache.Cache.get(cacheId.toString()).toString();
        redirect("http://" + WWW_URL + "/g/" + cacheId + "?preview=true");
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

        checkExpireAt(goods);
        checkSalePrice(goods);
        checkLevelPrice(levelPrices);
        checkShops(goods.supplierId);

        if (Validation.hasErrors()) {
            renderInit(goods);
            boolean selectAll = false;
            render("OperateGoods/add.html", selectAll);
        }
        //预览
        if (GoodsStatus.UNCREATED.equals(goods.status)) {
            preview(null, goods, imagePath);
        }

        //添加商品处理
        if (goods.unPublishedPlatforms != null) {
            for (GoodsUnPublishedPlatform unPublishedPlatform : goods.unPublishedPlatforms) {
                if (unPublishedPlatform == null) {
                    goods.unPublishedPlatforms.remove(unPublishedPlatform);
                }
            }
        }
        goods.createdBy = OperateRbac.currentUser().loginName;

        goods.create();
        try {
            goods.imagePath = uploadImagePath(imagePath, goods.id, null);
        } catch (IOException e) {
            error(500, "goods.image_upload_failed");
        }
        goods.save();

        index(null);
    }

    private static void checkShops(Long supplierId) {
        if (!Shop.containsShop(supplierId)) {
            Validation.addError("goods.supplierId", "validation.noShop");
        }
    }

    private static void checkSalePrice(Goods goods) {
        if (goods.salePrice == null) {
            Validation.addError("goods.salePrice", "validation.required");
        }
        if (goods.faceValue != null && goods.originalPrice != null && goods.originalPrice.compareTo(goods.faceValue) > 0) {
            Validation.addError("goods.originalPrice", "validation.moreThanFaceValue");
        }
        if (goods.salePrice != null && goods.originalPrice != null && goods.salePrice.compareTo(goods.originalPrice) < 0) {
            Validation.addError("goods.salePrice", "validation.lessThanOriginalPrice");
        }
    }

    private static void checkExpireAt(Goods goods) {
        if (goods.effectiveAt != null && goods.expireAt != null && goods.expireAt.before(goods.effectiveAt)) {
            Validation.addError("goods.expireAt", "validation.beforeThanEffectiveAt");
        }

        if ((StringUtils.isNotBlank(goods.useBeginTime) && StringUtils.isBlank(goods.useEndTime))
                || StringUtils.isBlank(goods.useBeginTime) && StringUtils.isNotBlank(goods.useEndTime)) {
            Validation.addError("goods.useEndTime", "validation.allRequiredUseTime");
        } else if (StringUtils.isNotBlank(goods.useBeginTime) && StringUtils.isNotBlank(goods.useEndTime) && goods.useBeginTime.compareTo(goods.useEndTime) >= 0) {
            Validation.addError("goods.useEndTime", "validation.beforeThanUseTime");
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
    private static String uploadImagePath(File uploadImageFile, Long goodsId, String oldImageFile) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径
        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, goodsId, UploadFiles.ROOT_PATH);
        if (oldImageFile != null && !"".equals(oldImageFile)) {
            File oldImage = new File(UploadFiles.ROOT_PATH + oldImageFile);
            oldImage.delete();
        }
        return absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 取得指定商品信息
     */
    public static void edit(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        checkShops(goods.supplierId);
        renderInit(goods);
        renderArgs.put("imageLargePath", goods.getImageLargePath());
        render(id);

    }

    /**
     * 取得指定商品信息
     */
    public static void copy(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        checkShops(goods.supplierId);
        renderInit(goods);
        renderArgs.put("imageLargePath", goods.getImageLargePath());
        render(id);

    }

    private static void renderShopList(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        renderArgs.put("shopList", shopList);
    }

    private static void renderSupplierList(Goods goods) {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        Supplier supplier = goods.getSupplier();
        if (supplier != null) {
            if (DeletedStatus.DELETED.equals(supplier.deleted)) {
                supplierList.add(supplier);
            }
        }
        renderArgs.put("supplierList", supplierList);
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
    public static void update(Long id, @Valid models.sales.Goods goods, File imagePath, BigDecimal[] levelPrices,
                              String imageLargePath) {
        if (goods.isAllShop && goods.shops != null) {
            goods.shops = null;
        }


        checkImageFile(imagePath);

        checkExpireAt(goods);
        goods.setLevelPrices(levelPrices, id);
        checkSalePrice(goods);
        checkLevelPrice(levelPrices);
        checkShops(goods.supplierId);

        if (Validation.hasErrors()) {

            renderArgs.put("imageLargePath", imageLargePath);
            renderInit(goods);

            render("OperateGoods/edit.html", goods, id);
        }

        //添加商品处理
        if (goods.unPublishedPlatforms != null) {
            for (GoodsUnPublishedPlatform unPublishedPlatform : goods.unPublishedPlatforms) {
                if (unPublishedPlatform == null) {
                    goods.unPublishedPlatforms.remove(unPublishedPlatform);
                }
            }
        }
        //预览的情况
        if (GoodsStatus.UNCREATED.equals(goods.status)) {
            preview(id, goods, imagePath);
        }

        String supplierUser = OperateRbac.currentUser().loginName;

        try {
            Goods oldGoods = Goods.findById(id);
            String oldImagePath = oldGoods == null ? null : oldGoods.imagePath;
            String image = uploadImagePath(imagePath, id, oldImagePath);
            if (StringUtils.isNotEmpty(image)) {
                goods.imagePath = image;
            }
        } catch (IOException e) {
            e.printStackTrace();
            error(e);
        }

        goods.updatedBy = supplierUser;
        Goods.update(id, goods, false);

        index(null);
    }

    private static void checkLevelPrice(BigDecimal[] prices) {
        if (prices == null) {
            for (ResalerLevel level : ResalerLevel.values()) {
                Validation.addError("goods.levelPrice." + level, "validation.required");
            }
            return;
        }
        //检查各个级别的价格，要求必须级别越高加价越低。
        for (int i = 0; i < prices.length; i++) {
            BigDecimal price = prices[i];
            if (price == null) {
                Validation.addError("goods.levelPrice." + ResalerLevel.values()[i], "validation.required");
            } else if (price.compareTo(BigDecimal.ZERO) < 0) {
                Validation.addError("goods.levelPrice." + ResalerLevel.values()[i], "validation.min", "0");
            } else if (price.compareTo(prices[(i > 0 ? i - 1 : 0)]) > 0) {
                Validation.addError("goods.levelPrice." + ResalerLevel.values()[i], "validation.moreThanLastLevel");
            }
        }
    }

    /**
     * 上架商品.
     * shopIds
     *
     * @param id 商品ID
     */
    public static void onSale(@As(",") Long... id) {

        for (Long goodsId : id) {
            models.sales.Goods goods = Goods.findById(goodsId);
            if (goods != null) {
                checkSalePrice(goods);
                checkLevelPrice(goods.getLevelPriceArray());
                checkShops(goods.supplierId);
            }

            renderArgs.put("imageLargePath", goods.getImageLargePath());

            if (Validation.hasErrors() && id.length > 0) {
                renderSupplierList(goods);

                renderInit(goods);
                renderArgs.put("id", goodsId);
                render("OperateGoods/edit.html", goods);
            }
        }
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
     * 设置精选级别.
     *
     * @param id 商品ID
     */
    public static void priority(Long id, Goods goods) {
        models.sales.Goods updateGoods = models.sales.Goods.findById(id);
        updateGoods.keywords = goods.keywords;
        updateGoods.priority = goods.priority;
        updateGoods.save();

        index(null);
    }

    /**
     * 上下架指定商品
     *
     * @param status 商品状态
     * @param ids    商品ID
     */
    private static void updateStatus(GoodsStatus status, Long... ids) {
        models.sales.Goods.updateStatus(status, ids);
        for (Long id : ids) {
            models.sales.Goods goods = Goods.findById(id);
            Supplier supplier = Supplier.findById(goods.supplierId);
            if (supplier != null && StringUtils.isNotEmpty(supplier.email)) {
                //发送提醒邮件
                MailMessage mailMessage = new MailMessage();
                mailMessage.addRecipient(supplier.email);
                mailMessage.setSubject(Play.mode.isProd() ? "商品下架" : "商品下架【测试】");
                mailMessage.putParam("date", new Date());
                mailMessage.putParam("supplier", supplier.fullName);
                mailMessage.putParam("goodsName", goods.name);
                mailMessage.putParam("faceValue", goods.faceValue);
                mailMessage.putParam("operateUser",OperateRbac.currentUser().userName);
                MailUtil.sendOperatorNotificationMail(mailMessage);
            }
        }
        index(null);
    }


    /**
     * 删除指定商品
     *
     * @param id 商品ID
     */
    public static void delete(@As(",") Long... id) {
        for (Long goodsId : id) {        //已上架的商品不可以删除
            Goods goods = Goods.findById(goodsId);
            warn("goods.status:" + goods.status);
            if (GoodsStatus.ONSALE.equals(goods.status)) {
                index(null);
            }
        }
        models.sales.Goods.delete(id);

        index(null);
    }

}
