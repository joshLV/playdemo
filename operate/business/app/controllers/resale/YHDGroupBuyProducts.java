package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.resale.ResalerProductJournalType;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.yihaodian.YHDResponse;
import models.yihaodian.YHDUtil;
import models.yihaodian.api.YHDCategoryAPI;
import models.yihaodian.response.YHDIdName;
import models.yihaodian.response.YHDMerchantCategory;
import models.yihaodian.response.YHDProductCategory;
import models.yihaodian.shop.UpdateResult;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-8
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class YHDGroupBuyProducts extends Controller {
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        List<YHDIdName> brands = YHDCategoryAPI.brandsCache();

        render(goods, brands);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long outerId, File imgFile ) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(outerId);
        if (goods == null) {
            notFound();
        }

        GoodsDeployRelation relation = GoodsDeployRelation.generate(goods, OuterOrderPartner.YHD);

        Map<String, String> requestParams = request.params.allSimple();
        requestParams.remove("body");
        requestParams.put("outerId", String.valueOf(relation.linkId));

        String responseXml = YHDUtil.sendRequest(requestParams, "yhd.product.add");
        Logger.info("yhd.product.add response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<UpdateResult> res = new YHDResponse<>();
            res.parseXml(responseXml, "updateCount", false, UpdateResult.parser);
            renderArgs.put("res", res);
            if(res.getErrorCount() == 0){
                //保存记录
                saveProductAndJournal(goods, operateUser, requestParams);
                //上传主图
                try{
                    uploadMainImg(imgFile, relation.linkId);
                }catch (Exception e) {
                    //ignore
                    Logger.warn(e,"yihaodian upload main img failed");
                }
            }
        }
        render("resale/YHDGroupBuyProducts/result.html");
    }
    //保存记录
    private static void saveProductAndJournal(Goods goods, OperateUser operateUser, Map<String, String> requestParams){
        String jsonData = new Gson().toJson(requestParams);
        ResalerProduct product = ResalerProduct.createProduct(OuterOrderPartner.YHD, 0L, operateUser.id, goods,
                Long.parseLong(requestParams.get("outerId")));
        ResalerProductJournal.createJournal(product, operateUser.id, jsonData, ResalerProductJournalType.CREATE, "上传商品");
    }

    //上传主图
    private static void uploadMainImg(File imgFile, Long goodsLinkId) {
        if (imgFile == null) {
            return;
        }
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("outerId", String.valueOf(goodsLinkId));
        requestParams.put("mainImageName", String.valueOf(goodsLinkId));
        String[] filePaths = new String[]{imgFile.getAbsolutePath()};
        String responseXml = YHDUtil.sendRequest(requestParams, filePaths, "yhd.product.img.upload");
        Logger.info("yhd.product.add response %s", responseXml);
        if (responseXml != null) {
            YHDResponse<UpdateResult> res = new YHDResponse<>();
            res.parseXml(responseXml, "updateCount", false, UpdateResult.parser);
            if (res.getErrorCount() > 0) {
                renderArgs.put("extra", "上传商品成功，但是主图上传失败");
                renderArgs.put("res", res);
            }
        }else {
            renderArgs.put("extra", "上传商品成功，但是主图上传失败");
        }
    }

    /**
     * 一号店分类
     * @param id
     */
    public static void category(Long id) {
        if (id == null) {
            id = 0L;
        }
        List<YHDProductCategory> categories = YHDCategoryAPI.productCategoriesCache(id, false);
        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0 ; i < categories.size(); i ++) {
            YHDProductCategory category = categories.get(i);
            if (i != 0) {
                jsonString.append(",");
            }
            jsonString.append("{id:'").append(category.id)
                    .append("',name:'").append(category.name)
                    .append("',isParent:").append(!category.isLeaf);
            if (!category.isLeaf) {
                jsonString.append(",nocheck:true");
            }
            jsonString.append("}");

        }
        jsonString.append("]");
        renderJSON(jsonString.toString());
    }

    /**
     * 商家分类
     * @param id
     */
    public static void merchantCategory(Long id) {
        if (id == null) {
            id = 0L;
        }
        List<YHDMerchantCategory> categories = YHDCategoryAPI.merchantCategoriesCache(id, false);
        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0 ; i < categories.size(); i ++) {
            YHDMerchantCategory category = categories.get(i);
            if (i != 0) {
                jsonString.append(",");
            }
            jsonString.append("{id:'").append(category.id)
                    .append("',name:'").append(category.name)
                    .append("',isParent:").append(!category.isLeaf);
            if (!category.isLeaf) {
                jsonString.append(",nocheck:true");
            }
            jsonString.append("}");

        }
        jsonString.append("]");
        renderJSON(jsonString.toString());
    }
}
