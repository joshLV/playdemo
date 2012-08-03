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
        if (pointGoods == null) {
            pointGoods = new models.sales.PointGoods();
            pointGoods.materialType = MaterialType.ELECTRONIC;
            renderArgs.put("pointGoods.materialType", MaterialType.ELECTRONIC);
        }

        //调试用
        for (String key : validation.errorsMap().keySet()) {
            warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
        }
        renderArgs.put("pointGoods", pointGoods);
    }



    /**
     * 添加商品
     * 商户只能添加电子券.
     *
     * @param imagePath
     * @param goods
     */
    @ActiveNavigation("point_goods_add")
    public static void create(@Valid models.sales.Goods pointGoods, @Required File imagePath) {

        checkImageFile(imagePath);



        //checkExpireAt(goods);
        checkSalePrice(pointGoods);

        if (Validation.hasErrors()) {
            renderInit(pointGoods);
         //   boolean selectAll = false;
          //  render("OperateGoods/add.html", selectAll);
            render("OperateGoods/add.html");
        }

        //预览
        if (GoodsStatus.UNCREATED.equals(pointGoods.status)) {
            preview(null, goods, imagePath);
        }


       // goods.createdBy = OperateRbac.currentUser().loginName;

        pointGoods.create();
        try {
            pointGoods.imagePath = uploadImagePath(imagePath, goods.id, null);
        } catch (IOException e) {
            error(500, "goods.image_upload_failed");
        }
        pointGoods.save();

        index(null);
    }




}

