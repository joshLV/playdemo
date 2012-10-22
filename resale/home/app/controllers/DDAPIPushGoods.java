package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Category;
import models.sales.Goods;
import play.Logger;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

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
public class DDAPIPushGoods extends Controller {
    /**
     * 批量发布商品
     *
     * @param goodsIds
     */
    public static void batchAdd(@As(",") List<Long> goodsIds) {

        Resaler user = SecureCAS.getResaler();
        if (!"dangdang".equals(user.loginName)) {
            error("user is not dangdang resaler");
        }
        String failGoods = "";
        boolean pushFlag = false;
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
            Template template = TemplateLoader.load("DDPushGoods/pushGoods.xml");
            String requestParams = template.render(goodsArgs);
            try {
                pushFlag = DDAPIUtil.pushGoods(goodsId, requestParams);
            } catch (DDAPIInvokeException e) {
                pushFlag = false;
                Logger.info("[DangDang API] invoke push goods fail! goodsId=" + goodsId);
            }
            if (!pushFlag) failGoods += "商品ID=" + goodsId + ",";
            if (pushFlag) {
                resalerFav.partner = OuterOrderPartner.DD;
                resalerFav.save();
            }

        }
        Logger.info("[DangDang API] invoke push goods success!");
        renderJSON("{\"error\":\"" + pushFlag + "\",\"info\":\"" + failGoods + "\"}");
    }
}
