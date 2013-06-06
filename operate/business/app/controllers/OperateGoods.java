/*
* Copyright 2012 uhuila.com, Inc. All rights reserved.
* uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/
package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FileUploadUtil;
import models.ktv.KtvProduct;
import models.ktv.KtvProductGoods;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.operator.OperateUser;
import models.resale.ResalerLevel;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.GoodsHistory;
import models.sales.GoodsImages;
import models.sales.GoodsStatus;
import models.sales.GoodsUnPublishedPlatform;
import models.sales.MaterialType;
import models.sales.Shop;
import models.sales.Sku;
import models.supplier.Supplier;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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
    public static String BASE_URL = Play.configuration.getProperty("application.baseUrl", "");
    private static Goods goodsProperties;

    /**
     * 展示商品一览页面
     */
    @ActiveNavigation("goods_index")
    public static void index(models.sales.GoodsCondition condition) {
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        int pageNumber = getPage();
        if (condition == null) {
            condition = new GoodsCondition();
            condition.status = GoodsStatus.ONSALE;
        }

        condition.setDescFields();

        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);
        List<Supplier> supplierList = Supplier.findUnDeleted();

        Long id = OperateRbac.currentUser().id;
        List<Brand> brandList = Brand.findByOrder(null, id);

        renderArgs.put("brandList", brandList);
        String queryString = StringUtils.trimToEmpty(getQueryString());
        render(goodsPage, supplierList, condition, queryString, hasApproveGoodsPermission);
    }

    private static String getQueryString() {
        List<String> kvs = new ArrayList<>();
        for (String key : request.params.all().keySet()) {
            if (!"body".equals(key) && !"queryString".equals(key) && !"page".equals(key) && !"id".equals(key) && !"selectall".equals(key)) {
                String[] values = request.params.getAll(key);
                for (String value : values) {
                    kvs.add(key + "=" + value);
                }
            }
        }
        return StringUtils.join(kvs, "&");
    }

    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    /**
     * 展示添加商品页面
     */
    @ActiveNavigation("goods_add")
    public static void add() {
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        renderInit(null);
        Boolean ktvSupplier = false;
        render(hasApproveGoodsPermission, ktvSupplier);
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
            goods.materialType = MaterialType.ELECTRONIC;
            goods.unPublishedPlatforms = new HashSet<>();
            if (supplierList != null && supplierList.size() > 0) {
                goods.supplierId = supplierList.get(0).id;
                checkShops(goods.supplierId);
                renderShopList(goods.supplierId);
            }
            goods.beginOnSaleAt = new Date();
            renderArgs.put("goods.materialType", MaterialType.ELECTRONIC);
            renderArgs.put("selectAll", true);
            goods.supplierId = null;
        }
        renderShopList(goods.supplierId);

        String shopIds = ",";
        if (goods.shops != null && goods.shops.size() > 0) {
            for (Shop shop : goods.shops) {
                shopIds += shop.id + ",";
            }
            goods.isAllShop = false;
        } else {
            goods.shops = null;
            goods.isAllShop = true;
        }

        Long id = OperateRbac.currentUser().id;

        if (goods.supplierId != null) {
            List<Brand> brandList = Brand.findByOrder(new Supplier(goods.supplierId), id);
            renderArgs.put("brandList", brandList);
        }

        if (goods.brand != null) {
            List<Sku> skuList = Sku.findByBrand(goods.brand.id);
            long remainInventory = 0;
            if (skuList.size() > 0) {
                remainInventory = skuList.get(0).getRemainCount();
            }
            renderArgs.put("skuList", skuList);
            renderArgs.put("remainInventory", remainInventory);
        }
        List<Category> categoryList = Category.findByParent(0);//获取顶层分类
        List<Category> subCategoryList = new ArrayList<>();
        Long categoryId = 0L;
        if (categoryList.size() > 0) {
            if (goods.categories != null && goods.categories.size() > 0 && goods.categories.iterator() != null && goods.categories.iterator().hasNext()) {
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

        renderArgs.put("secondaryVerification", goods.getProperties(Goods.SECONDARY_VERIFICATION, "0"));

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


    private static void preview(Long goodsId, models.sales.Goods goods, File imagePath) {
        String cacheId = "0";
        try {
            cacheId = models.sales.Goods.preview(goodsId, goods, imagePath, OperateUploadFiles.ROOT_PATH);

        } catch (IOException e) {
            e.printStackTrace();
            error(500, "goods.image_upload_failed");
        }
        cacheId = play.cache.Cache.get(cacheId.toString()).toString();

        redirect("http://" + WWW_URL + "/p/" + cacheId + "?preview=true");
    }

    /**
     * 添加商品
     * 商户只能添加电子券.
     *
     * @param imagePath
     * @param goods
     */
    @ActiveNavigation("goods_add")
    public static void create(@Valid models.sales.Goods goods, @Required File imagePath, Boolean ktvProduct) {
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        checkImageFile(imagePath);
        checkExpireAt(goods);
        checkSalePrice(goods);
        checkShops(goods.supplierId);
        checkUseWeekDay(goods);

        if (Validation.hasErrors()) {
            boolean selectAll = false;
            List<KtvProduct> productList = KtvProduct.findProductBySupplier(goods.supplierId);
            renderInit(goods);
            boolean ktvSupplier = goods.getSupplier().isKtvSupplier();
            render("OperateGoods/add.html", productList, ktvSupplier, selectAll, hasApproveGoodsPermission, ktvProduct);
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
        //设置或更新商品属性
        setGoodsProperties(goods);

        String createdFrom = "Op";
        goods.createHistory(createdFrom);

        //KTV商品处理
        if (goods.isKtvProduct()) {
            Shop shop = Shop.findById(Long.valueOf(request.params.getAll("goods.shops.id")[0]));
            goods.shops = new HashSet<>();
            goods.isAllShop = false;
            goods.shops.add(shop);
            goods.save();
            KtvProductGoods productGoods = new KtvProductGoods();
            productGoods.shop = shop;
            productGoods.product = goods.product;
            productGoods.goods = goods;
            productGoods.save();
        }
        index(null);
    }

    private static void checkUseWeekDay(models.sales.Goods goods) {
        /*
        // 不再需要检查
        if (StringUtils.isBlank(goods.useWeekDay)) {
            Validation.addError("goods.useWeekDayAll", "validation.useWeekDay");
            renderArgs.put("noUseWeekDayAll", "true");
        } else if (StringUtils.isNotBlank(goods.useBeginTime) && StringUtils.isNotBlank(goods.useEndTime)
                && goods.useBeginTime.compareTo(goods.useEndTime) > 0 && goods.useEndTime.compareTo("06:00") > 0) {
            Validation.addError("goods.useEndTime", "validation.useEndTime");
        }
        */
    }

    private static void checkShops(Long supplierId) {
        if (!Shop.containsShop(supplierId)) {
            Validation.addError("goods.supplierId", "validation.noShop");
        }
    }

    private static void checkSalePrice(models.sales.Goods goods) {
        if (goods.salePrice == null) {
            Validation.addError("goods.salePrice", "validation.required");
        }
        if (goods.faceValue != null && goods.originalPrice != null && goods.originalPrice.compareTo(goods.faceValue) > 0) {
            Validation.addError("goods.originalPrice", "validation.moreThanFaceValue");
        }
        if (goods.salePrice != null && goods.originalPrice != null && goods.salePrice.compareTo(goods.originalPrice) < 0) {
            Validation.addError("goods.salePrice", "validation.lessThanOriginalPrice");
        }
        if (goods.promoterPrice != null && goods.invitedUserPrice != null &&
                goods.promoterPrice.compareTo(goods.invitedUserPrice) < 0) {
            Validation.addError("goods.invitedUserPrice", "validation.moreThanPromoterPrice");
        }
        if ("1".equals(StringUtils.trimToEmpty(request.params.get("secondaryVerification")))) {
            if (goods.advancedDeposit == null || goods.advancedDeposit.compareTo(BigDecimal.ZERO) <= 0) {
                Validation.addError("goods.advancedDeposit", "validation.required");
            }
        }
    }

    private static void checkExpireAt(models.sales.Goods goods) {
        if (goods.effectiveAt != null && goods.expireAt != null && goods.expireAt.before(goods.effectiveAt)) {
            Validation.addError("goods.expireAt", "validation.beforeThanEffectiveAt");
        }

        if ((StringUtils.isNotBlank(goods.useBeginTime) && StringUtils.isBlank(goods.useEndTime))
                || StringUtils.isBlank(goods.useBeginTime) && StringUtils.isNotBlank(goods.useEndTime)) {
            Validation.addError("goods.useEndTime", "validation.allRequiredUseTime");
        }
    }

    private static void checkSaleAt(models.sales.Goods goods) {
        if (goods.beginOnSaleAt != null && goods.endOnSaleAt != null && goods.endOnSaleAt.before(goods.beginOnSaleAt)) {
            Validation.addError("goods.endOnSaleAt", "validation.beforeThanBeginOnSaleAt");
        }

        if (goods.beginOnSaleAt != null && goods.expireAt != null && goods.beginOnSaleAt.after(goods.expireAt)) {
            Validation.addError("goods.beginOnSaleAt", "validation.afterThanExpireAt");
        }

        if (goods.endOnSaleAt != null && goods.expireAt != null && goods.endOnSaleAt.after(goods.expireAt)) {
            Validation.addError("goods.endOnSaleAt", "validation.afterThanExpireAt");
        }
    }

    private static void checkImageFile(File imagePath) {
        if (imagePath != null) {
            //检查目录
            File uploadDir = new File(OperateUploadFiles.ROOT_PATH);
            if (!uploadDir.isDirectory()) {
                Validation.addError("goods.imagePath", "validation.write");
            }

            //检查目录写权限
            if (!uploadDir.canWrite()) {
                Validation.addError("goods.imagePath", "validation.write");
            }

            if (imagePath.length() > OperateUploadFiles.MAX_SIZE) {
                Validation.addError("goods.imagePath", "validation.maxFileSize");
            }

            //检查扩展名
            //定义允许上传的文件扩展名
            String[] fileTypes = OperateUploadFiles.FILE_TYPES.trim().split(",");
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
        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, goodsId, OperateUploadFiles.ROOT_PATH);
        if (oldImageFile != null && !"".equals(oldImageFile)) {
            File oldImage = new File(OperateUploadFiles.ROOT_PATH + oldImageFile);
            oldImage.delete();
        }
        return absolutePath.substring(OperateUploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 取得指定商品信息
     */
    public static void edit2(Long id, int page) {
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        String queryString = StringUtils.trimToEmpty(getQueryString());
        models.sales.Goods goods = models.sales.Goods.findById(id);
        checkShops(goods.supplierId);

        renderArgs.put("imageLargePath", goods.getImageLargePath());
        renderArgs.put("page", page);
        renderArgs.put("queryString", queryString);

        boolean ktvSupplier = goods.isKtvSupplier();
        boolean ktvProduct = true;
        //普通产品
        if (KtvProductGoods.find("goods.id = ?", id).first() == null) {
            ktvSupplier = false;
            ktvProduct = false;
        }
        List<KtvProduct> productList = KtvProduct.findProductBySupplier(goods.supplierId);
        setGoodsProduct(goods);
        renderInit(goods);

        render(id, hasApproveGoodsPermission, ktvSupplier, productList, ktvProduct);

    }

    private static void setGoodsProduct(Goods goods) {
        if (goods.isKtvProduct() && !goods.isAllShop) {
            KtvProductGoods productGoods = KtvProductGoods.find("goods=? and shop=?", goods, goods.shops.iterator().next()).first();
            if (productGoods != null) {
                goods.product = productGoods.product;
            }
        }
    }


    /**
     * 取得指定商品信息
     */
    public static void copy(Long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        checkShops(goods.supplierId);
        boolean ktvSupplier = goods.isKtvSupplier();
        List<KtvProduct> productList = KtvProduct.findProductBySupplier(goods.supplierId);
        setGoodsProduct(goods);
        renderInit(goods);
        boolean ktvProduct = true;
        //普通产品
        if (KtvProductGoods.find("goods.id = ?", id).first() == null) {
            ktvSupplier = false;
            ktvProduct = false;
        }
        renderArgs.put("imageLargePath", goods.getImageLargePath());
        render(id, ktvSupplier, productList, ktvProduct);

    }

    private static void renderShopList(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        renderArgs.put("shopList", shopList);
    }

    private static void renderSupplierList(models.sales.Goods goods) {
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
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        models.sales.Goods goods = models.sales.Goods.findById(id);
        List<Shop> shopList = Shop.findShopBySupplier(goods.supplierId);
        renderTemplate("OperateGoods/show.html", goods, shopList, hasApproveGoodsPermission);
    }

    /**
     * GET /goods/{goods_id}/histories
     *
     * @param id
     */
    public static void showHistory(Long id) {
        String supplierName = "";
        String goodsNo = "";
        String goodsName = "";
        List<GoodsHistory> goodsHistoryList = GoodsHistory.find("goodsId=? order by createdAt desc", id).fetch();
        if (goodsHistoryList.size() > 0 && goodsHistoryList != null) {
            models.sales.Goods goods = models.sales.Goods.findById(id);
            goodsNo = goods.no;
            supplierName = goods.getSupplier().fullName;
            goodsName = goods.name;
        }
        render(goodsHistoryList, supplierName, goodsNo, goodsName);
    }


    /**
     * 更新指定商品信息
     */
    public static void update(Long id, @Valid final models.sales.Goods goods, File imagePath, String imageLargePath) {
        if (goods.isAllShop && goods.shops != null) {
            goods.shops = null;
        }
        checkImageFile(imagePath);
        checkExpireAt(goods);
        checkSaleAt(goods);
        checkSalePrice(goods);
        checkShops(goods.supplierId);
//        checkUseWeekDay(goods);
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
            goods.id = id;
            preview(id, goods, imagePath);
        }

        String supplierUser = OperateRbac.currentUser().loginName;

        try {
            models.sales.Goods oldGoods = models.sales.Goods.findById(id);
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
        models.sales.Goods.update(id, goods);
        Goods goodsItem = models.sales.Goods.findById(id);
        String createdFrom = "Op";
        goodsItem.createHistory(createdFrom);
        index(null);
    }

    /**
     * 更新指定商品信息
     */
    public static void update2(Long id, @Valid final models.sales.Goods goods, File imagePath, String imageLargePath, String queryString, int page) {
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        if (goods.isAllShop && goods.shops != null) {
            goods.shops = null;
        }

        checkImageFile(imagePath);
        checkExpireAt(goods);
        checkSaleAt(goods);
        checkSalePrice(goods);
        checkShops(goods.supplierId);
        checkUseWeekDay(goods);

        if (Validation.hasErrors()) {
            renderArgs.put("imageLargePath", imageLargePath);
            renderInit(goods);
            boolean ktvSupplier = goods.isKtvSupplier();
            List<KtvProduct> productList = KtvProduct.findProductBySupplier(goods.supplierId);
            render("OperateGoods/edit2.html", productList, ktvSupplier, goods, id, page, queryString, hasApproveGoodsPermission);
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
            goods.id = id;
            preview(id, goods, imagePath);
        }

        String supplierUser = OperateRbac.currentUser().loginName;

        try {
            models.sales.Goods oldGoods = models.sales.Goods.findById(id);
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
        models.sales.Goods.update(id, goods);
        Goods goodsItem = models.sales.Goods.findById(id);

        goodsItem.refresh();

        //设置或更新商品属性
        setGoodsProperties(goodsItem);
        String createdFrom = "Op";
        goodsItem.createHistory(createdFrom);
        if (goodsItem.isKtvProduct()) {
            KtvProductGoods productGoods = KtvProductGoods.find("goods=?", goodsItem).first();
            if (productGoods != null) {
                productGoods.shop = goodsItem.shops.iterator().next();
                productGoods.product = goods.product;
                productGoods.save();
            } else {
                productGoods = new KtvProductGoods();
                productGoods.goods = goodsItem;
                productGoods.shop = goodsItem.shops.iterator().next();
                productGoods.product = goods.product;
                productGoods.save();
            }
        }
        redirectUrl(page, queryString);
    }

    private static void redirectUrl(int page, String condition) {
        if (StringUtils.isNotEmpty(condition) && (condition.contains("?x-http-method-override=PUT") || condition.contains("x-http-method-override=PUT"))) {
            condition = condition.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        if (Play.mode.isDev()) {
            redirect("http://localhost:9303/?page=" + page + "&" + condition);
        } else {
            redirect(BASE_URL + "?page=" + page + "&" + condition);
        }
        redirect(BASE_URL + "?page=" + page + "&" + condition);
    }

    /**
     * 上架商品.
     * shopIds
     *
     * @param id 商品ID
     */
    public static void onSale(@As(",") Long... id) {
        Boolean hasApproveGoodsPermission = ContextedPermission.hasPermission("GOODS_APPROVE_ONSALE");
        for (Long goodsId : id) {
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            if (goods != null) {
                checkSalePrice(goods);
                checkShops(goods.supplierId);
            }

            renderArgs.put("imageLargePath", goods.getImageLargePath());

            if (Validation.hasErrors() && id.length > 0) {
                renderSupplierList(goods);
                renderInit(goods);
                renderArgs.put("id", goodsId);
                render("OperateGoods/edit2.html", goods, hasApproveGoodsPermission);
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
    public static void priority(Long id, models.sales.Goods goods) {
        models.sales.Goods updateGoods = models.sales.Goods.findById(id);
        updateGoods.keywords = goods.keywords;
        updateGoods.priority = goods.priority;
        String createdFrom = "Op";
        updateGoods.createHistory(createdFrom);
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
        if (status == GoodsStatus.OFFSALE) {
            for (Long id : ids) {
                models.sales.Goods goods = models.sales.Goods.findById(id);
                goods.refresh();
                Supplier supplier = Supplier.findById(goods.supplierId);
                if (supplier != null) {
                    String email = "";
                    if (supplier.salesId != null) {
                        OperateUser operateUser = OperateUser.findById(supplier.salesId);
                        email = operateUser.email;
                    } else if (StringUtils.isNotBlank(supplier.salesEmail)) {
                        email = supplier.salesEmail;
                    }
                    if (StringUtils.isBlank(email)) email = "bd@seewi.com.cn";
                    //发送提醒邮件
                    MailMessage mailMessage = new MailMessage();
                    mailMessage.addRecipient(email);
                    mailMessage.setSubject(Play.mode.isProd() ? "商品下架" : "商品下架【测试】");
                    mailMessage.putParam("date", DateUtil.getNowTime());
                    mailMessage.putParam("supplierName", supplier.fullName);
                    mailMessage.putParam("goodsName", goods.name);
                    mailMessage.putParam("faceValue", goods.faceValue);
                    mailMessage.putParam("operateUserName", OperateRbac.currentUser().userName);
                    MailUtil.sendGoodsOffSalesMail(mailMessage);
                }
            }
        }

        for (int i = 0; i < ids.length; i++) {
            Goods goodsItem = models.sales.Goods.findById(ids[i]);
            goodsItem.refresh();
            String createdFrom = "Op";
            goodsItem.createHistory(createdFrom);
        }
        int page = getPage();
        String queryString = StringUtils.trimToEmpty(getQueryString());
        redirectUrl(page, queryString);
    }


    /**
     * 删除指定商品
     *
     * @param id 商品ID
     */
    public static void delete(@As(",") Long... id) {
        for (Long goodsId : id) {        //已上架的商品不可以删除
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            if (GoodsStatus.ONSALE.equals(goods.status)) {
                index(null);
            }
        }
        models.sales.Goods.delete(OperateRbac.currentUser().loginName, id);

        index(null);
    }

    /**
     * 删除商品相册一个图片
     */
    public static void deleteImage(Long id) {
        GoodsImages images = GoodsImages.findById(id);
        images.delete();
        renderJSON("");
    }

    /**
     * 设为首页展示图片，一个商品只能有一个在首页展示
     */
    public static void setDisplay(Long id) {
        String goodsId1 = request.params.get("goodsId");
        Long goodsId = StringUtils.isEmpty(goodsId1) ? 0 : Long.valueOf(goodsId1);
        Goods goods = Goods.findById(goodsId);
        List<GoodsImages> imagesList = GoodsImages.find("goods=?", goods).fetch();
        for (GoodsImages image : imagesList) {
            //把指定的作为首页展示
            image.isDisplaySite = image.id.equals(id);
            image.save();
        }
        renderJSON("");
    }

    private static void setGoodsProperties(Goods goods) {
        if (StringUtils.isBlank(request.params.get("secondaryVerification"))) {
            goods.setProperties(Goods.SECONDARY_VERIFICATION, "0");
        } else {
            goods.setProperties(Goods.SECONDARY_VERIFICATION, request.params.get("secondaryVerification"));
        }
    }
}
