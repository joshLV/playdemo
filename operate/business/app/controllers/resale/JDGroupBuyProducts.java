package controllers.resale;

import controllers.OperateRbac;
import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JDRest;
import models.jingdong.groupbuy.response.IdNameResponse;
import models.jingdong.groupbuy.response.UploadTeamResponse;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.resale.ResalerProductJournalType;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.*;

/**
 * @author likang
 *         Date: 13-1-11
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class JDGroupBuyProducts extends Controller{
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        refreshGoods(goods);

        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        Supplier supplier = Supplier.findById(goods.supplierId);

        render(goods, supplier, shops);
    }

    /**
     * 更新goods的信息为上次使用的信息
     */
    private static void refreshGoods(Goods goods) {
        //查找最后一次上传的信息
        ResalerProduct product = ResalerProduct .find("goods = ? and partner = ? order by createdAt desc",
                goods, OuterOrderPartner.JD).first();
        if (product == null) {
            return;
        }
        ResalerProductJournal journal = ResalerProductJournal.find("product = ? and type = ? order by createdAt desc",
                product, ResalerProductJournalType.CREATE).first();
        if (journal == null) {
            Logger.info("journal not found");
            error();
        }
        //todo refresh

    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long venderTeamId) {
        String url = JDGroupBuyUtil.GATEWAY_URL + "/platform/normal/uploadTeam.action";
        Template template = TemplateLoader.load("jingdong/groupbuy/request/uploadTeam.xml");
        Map<String, Object> params = new HashMap<>();
        String data = template.render(params);
        Logger.info("request, %s", data);

        String restRequest = JDGroupBuyUtil.makeRequestRest(data);
        WS.HttpResponse response = WS.url(url).body(restRequest).post();

        JDRest<UploadTeamResponse> uploadTeamRest = new JDRest<>();
        if (uploadTeamRest.parse(response.getString(), new UploadTeamResponse())) {

        }

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
            List<IdNameResponse> cities = JDGroupBuyUtil.cacheCities();
            renderJSON(jsonStr(cities, true, "city"));
        }
        if (id == null || type == null) {
            error();
        }
        if ("city".equals(type)) {
            List<IdNameResponse> districts = JDGroupBuyUtil.cacheDistricts(id);
            renderJSON(jsonStr(districts, true, "district"));
        }else if ("district".equals(type)) {
            List<IdNameResponse> areas = JDGroupBuyUtil.queryArea(id);
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
        List<IdNameResponse> groups = JDGroupBuyUtil.cacheCategories(id);
        boolean isParent = id == 0L;
        renderJSON(jsonStr(groups, isParent, ""));

    }
    private static String jsonStr(List<IdNameResponse> params, boolean isParent, String type) {

        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0 ; i < params.size(); i ++) {
            IdNameResponse city = params.get(i);
            if (i != 0) {
                jsonString.append(",");
            }
            jsonString.append("{id:'").append(city.id)
                    .append("',name:'").append(city.name)
                    .append("',isParent:").append(isParent)
                    .append(",type:'").append(type)
                    .append("'}");
        }
        jsonString.append("]");
        return jsonString.toString();
    }
}
