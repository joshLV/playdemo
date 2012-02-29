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
        //默认取出5页产品
        List<models.sales.Goods> goodsList = models.sales.Goods.findTop(80);
        //默认取出前8个上海的区
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

    public static void list(String condition) {
        String[] args = condition.split("-");
        String categoryId = args[0];
        String cityId = args[1];
        String districtId = args[2];
        String areaId = args[3];
        String brandId = args[4];
        String priceFrom = args[5];
        String priceTo = args[6];
        String order = args[7];

        List<models.sales.Goods> goodsList = models.sales.Goods.findByCondition
                (condition);

    }

}
