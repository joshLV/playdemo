package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.modules.resale.cas.SecureCAS;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.GoodsThirdSupport;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-16
 * Time: 上午10:57
 */
@With(SecureCAS.class)
public class DDPushGoods extends Controller {
    public static final String DATE_FORMAT = "yyy-MM-dd HH:mm:ss";

    /**
     * 批量发布商品
     *
     * @param goodsIds
     */
    public static void batchAdd(@As(",") List<Long> goodsIds) {
        Logger.info("DDAPIPushGoods API begin!");
        Resaler user = SecureCAS.getResaler();
        if (!Resaler.DD_LOGIN_NAME.equals(user.loginName)) {
            error("user is not dangdang resaler");
        }
        String failGoods = "";
        boolean pushFlag = true;
        for (Long goodsId : goodsIds) {
            Goods goods = Goods.findOnSale(goodsId);
            ResalerFav resalerFav = ResalerFav.find("byGoodsAndResaler", goods, user).first();
            if (resalerFav == null) {
                error("no fav found");
            }

            Map<String, Object> goodsArgs = new HashMap<>();
            List<Category> categoryList = Category.findByParent(0);//获取顶层分类
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
            }
            goodsArgs.put("categoryId", categoryId);
            goodsArgs.put("goods", goods);

            GoodsDeployRelation goodsMapping = GoodsDeployRelation.generate(goods, OuterOrderPartner.DD);

            goodsArgs.put("goodsMappingId", goodsMapping.linkId);
            Template template = TemplateLoader.load("DDPushGoods/pushGoods.xml");

            String requestParams = template.render(goodsArgs);
            try {
                pushFlag = DDAPIUtil.pushGoods(goodsMapping.linkId, requestParams);
            } catch (DDAPIInvokeException e) {
                Logger.info("[DDAPIPushGoods API] invoke push goods fail! goodsId=" + goodsId);
            }
            if (pushFlag) failGoods += "商品ID=" + goodsId + ",";
            if (!pushFlag) {
                resalerFav.partner = OuterOrderPartner.DD;
                resalerFav.save();
                Logger.info("[DDAPIPushGoods API] invoke push goods success!");
            }
        }

        renderJSON("{\"error\":\"" + pushFlag + "\",\"info\":\"" + failGoods + "\"}");
    }

    public static void prepare(Long goodsId) {
        Logger.info("DDAPIPushGoods API begin!");
        Resaler user = SecureCAS.getResaler();
        if (!Resaler.DD_LOGIN_NAME.equals(user.loginName)) {
            error("user is not dangdang resaler");
        }
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
        render(goods, shops);
    }

    public static void push() {
        Map<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
        Long goodsId = Long.valueOf(StringUtils.trimToEmpty(params.get("goodsId")));
        Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
        Resaler user = SecureCAS.getResaler();
        if (!Resaler.DD_LOGIN_NAME.equals(user.loginName)) {
            error("user is not dangdang resaler");
        }

        Goods goods = Goods.findOnSale(goodsId);
        ResalerFav resalerFav = ResalerFav.find("byGoodsAndResaler", goods, user).first();
        if (resalerFav == null) {
            error("no fav found");
        }
        String goodsData = gson.toJson(params);

        GoodsThirdSupport support = GoodsThirdSupport.getSupportGoods(goods, OuterOrderPartner.DD);
        if (support == null) {
            new GoodsThirdSupport().generate(goods, goodsData, OuterOrderPartner.DD);
        } else {
            support.goodsData = goodsData;
            support.save();
        }

        render();
    }
}
