package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.webcas.WebCAS;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 商品控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 5:32 PM
 */
@With(WebCAS.class)
public class Goods extends Controller {

    public static void index() {
        List<models.sales.Goods> goodsList = models.sales.Goods.findTop(40);
        List<Area> districts = Area.findTopDistricts("021", 8);
        List<Area> areas = Area.findTopAreas(8);
        List<Category> categories = Category.findTop(8);
        List<Brand> brands = Brand.findTop(8);

        render(goodsList, areas, districts, categories, brands);
    }

    public static void show(long id) {
        models.sales.Goods goods = models.sales.Goods.find(
                "id=? and deleted=?",
                id, DeletedStatus.UN_DELETED).first();
        if (goods == null) {
            notFound();
        }

        render(goods);
    }

}
