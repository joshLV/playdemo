package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.operator.OperateUser;
import models.order.OuterOrderPartner;
import models.sales.*;
import models.yihaodian.YHDResponse;
import models.yihaodian.YHDUtil;
import models.yihaodian.YHDCategoryAPI;
import operate.rbac.annotations.ActiveNavigation;
import org.w3c.dom.Node;
import play.Logger;
import play.libs.XPath;
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
public class YHDProducts extends Controller {
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        List<Node> brands = YHDCategoryAPI.brandsCache();

        render(goods, brands);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long outerId, File imgFile ) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(outerId);
        if (goods == null) {
            notFound();
        }

        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.YHD, goods);

        Map<String, String> requestParams = request.params.allSimple();
        requestParams.remove("body");
        requestParams.put("outerId", String.valueOf(product.goodsLinkId));

        YHDResponse productResponse = YHDUtil.sendRequest(requestParams, "yhd.product.add", "updateCount");
        renderArgs.put("productResponse", productResponse);
        if (productResponse.isOk()) {
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id).save();
            //保存记录
            ResalerProductJournal.createJournal(product, operateUser.id, new Gson().toJson(requestParams),
                    ResalerProductJournalType.CREATE, "上传商品");
            //上传主图
            try{
                uploadMainImg(imgFile, product.goodsLinkId);
            }catch (Exception e) {
                //ignore
                Logger.warn(e,"yihaodian upload main img failed");
            }
        }
        render("resale/YHDProducts/result.html");
    }

    //上传主图
    private static void uploadMainImg(File imgFile, Long productId) {
        if (imgFile == null) {
            return;
        }
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("outerId", String.valueOf(productId));
        requestParams.put("mainImageName", String.valueOf(productId));
        YHDResponse response = YHDUtil.sendRequestWithFiles(requestParams, "yhd.product.img.upload", "updateCount", new File[]{imgFile});
        renderArgs.put("imgResponse", response);
    }

    /**
     * 一号店分类
     * @param id
     */
    public static void category(Long id) {
        if (id == null) {
            id = 0L;
        }
        List<Node> categories = YHDCategoryAPI.productCategoriesCache(id);
        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0 ; i < categories.size(); i ++) {
            Node category = categories.get(i);
            if (i != 0) {
                jsonString.append(",");
            }
            boolean isParent = "0".equals(XPath.selectText("./categoryIsLeaf", category).trim());
            jsonString.append("{id:'").append(XPath.selectText("./categoryId", category).trim())
                    .append("',name:'").append(XPath.selectText("./categoryName", category).trim())
                    .append("',isParent:").append(isParent);
            if (isParent) {
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
        List<Node> categories = YHDCategoryAPI.merchantCategoriesCache(id);
        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0 ; i < categories.size(); i ++) {
            Node category = categories.get(i);
            if (i != 0) {
                jsonString.append(",");
            }
            boolean isParent = "0".equals(XPath.selectText("./categoryIsLeaf", category));
            jsonString.append("{id:'").append(XPath.selectText("./merchantCategoryId", category))
                    .append("',name:'").append(XPath.selectText("./categoryName", category))
                    .append("',isParent:").append(isParent);
            if (isParent) {
                jsonString.append(",nocheck:true");
            }
            jsonString.append("}");
        }
        jsonString.append("]");
        renderJSON(jsonString.toString());
    }
}
