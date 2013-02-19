package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.admin.OperateUser;
import models.jingdong.groupbuy.JDGroupBuyHelper;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
import models.order.OuterOrderPartner;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.w3c.dom.Node;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

/**
 * @author likang
 *         Date: 13-1-11
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class JDGroupBuyProducts extends Controller{
    /**
     * 显示上传商品的界面
     */
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);

        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        Supplier supplier = Supplier.findById(goods.supplierId);

        render(goods, supplier, shops);
    }

    /**
     * 上传商品
     */
    @ActiveNavigation("resale_partner_product")
    public static void upload(Long venderTeamId) {
        //查找商品
        Goods goods = Goods.findById(venderTeamId);
        if (goods == null) {
            notFound();
        }

        //申请商品ID并准备参数
        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.DD, goods);
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String> entry : request.params.allSimple().entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        params.remove("body");
        params.put("venderTeamId", product.goodsLinkId);
        params.put("shops", goods.getShopList());

        //提交请求
        JingdongMessage response = JDGroupBuyUtil.sendRequest("uploadTeam", params);

        //保存历史
        if(response.isOk()) {
            OperateUser operateUser = OperateRbac.currentUser();
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id)
                    .partnerProduct(response.selectTextTrim("./JdTeamId").trim())
                    .latestJson(new Gson().toJson(params))
                    .save();
            //记录历史
            ResalerProductJournal.createJournal(product, operateUser.id, product.latestJsonData,
                    ResalerProductJournalType.CREATE, "上传商品");
        }
        render("resale/JDGroupBuyProducts/result.html", response);
    }

    /**
     * 显示修改商品界面
     */
    @ActiveNavigation("resale_partner_product")
    public static void showEdit(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        if (product == null) {
            notFound();
        }

        Goods goods = product.goods;
        render(product, goods);
    }

    /**
     * 修改商品
     */
    @ActiveNavigation("resale_partner_product")
    public static void edit(String action) {
        Map<String, Object> params = new HashMap<>();
        for(Map.Entry<String, String> entry : request.params.allSimple().entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }

        JingdongMessage response = JDGroupBuyUtil.sendRequest(action, params);

        render("resale/JDGroupBuyProducts/result.html", response);
    }

    /**
     * 查询城市、区域和商圈
     * @param id 不传则视为0
     * @param type 如果为空并且id为空，返回城市列表
     *             如果是city，返回区域,
     *             如果是district，则返回商圈
     */
    public static void city(Long id, String type) {
        if (id == null && type == null) {
            List<Node> cities = JDGroupBuyHelper.cacheCities();
            renderJSON(jsonStr(cities, true, "city"));
        }
        if (id == null || type == null) {
            error();
        }
        if ("city".equals(type)) {
            List<Node> districts = JDGroupBuyHelper.cacheCities();
            renderJSON(jsonStr(districts, true, "district"));
        }else if ("district".equals(type)) {
            List<Node> areas = JDGroupBuyHelper.cacheCities();
            renderJSON(jsonStr(areas, false, "area"));
        }

    }

    /**
     * 查询分类
     * @param id 如果为空，则视为0
     */
    public static void group(Long id) {
        if (id == null) {
            id = 0L;
        }
        List<Node> groups = JDGroupBuyHelper.cacheCategories(id);
        boolean isParent = id == 0L;
        renderJSON(jsonStr(groups, isParent, ""));
    }

    private static String jsonStr(List<Node> params, boolean isParent, String type) {
        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0 ; i < params.size(); i ++) {
            Node city = params.get(i);
            if (i != 0) {
                jsonString.append(",");
            }
            jsonString.append("{id:'").append(XPath.selectText("./Id", city).trim())
                    .append("',name:'").append(XPath.selectText("./Name", city).trim())
                    .append("',isParent:").append(isParent)
                    .append(",type:'").append(type)
                    .append("'}");
        }
        jsonString.append("]");
        return jsonString.toString();
    }
}
