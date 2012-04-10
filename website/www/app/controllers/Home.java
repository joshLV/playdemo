package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.sales.Area;
import models.sales.Category;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 首页控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 9:57 AM
 */
@With(SecureCAS.class)
@SkipCAS
public class Home extends Controller {

    public static void index(long categoryId) {
        List<models.sales.Goods> goodsList;
        if (categoryId==0){
            goodsList = models.sales.Goods.findTop(12);
        }else{
            goodsList = models.sales.Goods.findTopByCategory(categoryId, 12);
        }
        List<Area> areas = Area.findTopAreas(8);
        List<Category> categories = Category.findTop(8);

        renderArgs.put("categoryId", new Long(categoryId));
        render(goodsList, categories, areas);
    }
}
