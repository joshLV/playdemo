package controllers;

import models.resale.ResalerLevel;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static play.Logger.warn;

/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-2
 * Time: 下午5:17
 * To change this template use File | Settings | File Templates.
 */

@With(OperateRbac.class)
@ActiveNavigation("point_goods_index")
public class OperatePointGoods  extends Controller {

    /**
     * 展示添加商品页面
     */
    @ActiveNavigation("point_goods_add")
    public static void add() {
        renderInit(null);
        render();
    }

    /**
     * 初始化form界面.
     * 添加和修改页面共用
     *
     * @param pointGoods
     */
    private static void renderInit(models.sales.PointGoods pointGoods) {
        List<Supplier> supplierList = Supplier.findUnDeleted();

        if (pointGoods == null) {
            pointGoods = new models.sales.PointGoods();
            BigDecimal[] levelPrices = new BigDecimal[ResalerLevel.values().length];
            Arrays.fill(levelPrices, null);

            pointGoods.materialType = MaterialType.ELECTRONIC;


            renderArgs.put("goods.materialType", MaterialType.ELECTRONIC);
            renderArgs.put("selectAll", true);
        }



        //调试用
        for (String key : validation.errorsMap().keySet()) {
            warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
        }




        renderArgs.put("pointGoods", pointGoods);
    }



//    /**
//     * 添加商品
//     * 商户只能添加电子券.
//     *
//     * @param imagePath
//     * @param goods
//     */
//    @ActiveNavigation("goods_add")
//    public static void create(@Valid models.sales.Goods goods, @Required File imagePath, BigDecimal[] levelPrices) {
//
//        checkImageFile(imagePath);
//
//        goods.setLevelPrices(levelPrices);
//
//        checkExpireAt(goods);
//        checkSalePrice(goods);
//        checkLevelPrice(levelPrices);
//        checkShops(goods.supplierId);
//
//        if (Validation.hasErrors()) {
//            renderInit(goods);
//            boolean selectAll = false;
//            render("OperateGoods/add.html", selectAll);
//        }
//        //预览
//        if (GoodsStatus.UNCREATED.equals(goods.status)) {
//            preview(null, goods, imagePath);
//        }
//
//        //添加商品处理
//        if (goods.unPublishedPlatforms != null) {
//            for (GoodsUnPublishedPlatform unPublishedPlatform : goods.unPublishedPlatforms) {
//                if (unPublishedPlatform == null) {
//                    goods.unPublishedPlatforms.remove(unPublishedPlatform);
//                }
//            }
//        }
//        goods.createdBy = OperateRbac.currentUser().loginName;
//
//        goods.create();
//        try {
//            goods.imagePath = uploadImagePath(imagePath, goods.id, null);
//        } catch (IOException e) {
//            error(500, "goods.image_upload_failed");
//        }
//        goods.save();
//
//        index(null);
//    }




}

