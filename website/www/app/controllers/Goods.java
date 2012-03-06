package controllers;

import controllers.modules.webcas.WebCAS;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.GoodsCondition;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.Paginator;
import play.modules.paginate.ValuePaginator;
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

    public static String SHANGHAI = "021";
    public static int LIMIT = 8;

    public static void index() {
        //默认取出5页产品
        List<models.sales.Goods> goodsList = models.sales.Goods.findTop(80);
        //默认取出前8个上海的区
        List<Area> districts = Area.findTopDistricts(SHANGHAI, LIMIT);
        List<Area> areas = Area.findTopAreas(LIMIT);
        List<Category> categories = Category.findTop(LIMIT);
        List<Brand> brands = Brand.findTop(LIMIT);

        renderArgs.put("categoryId", 0);
        renderArgs.put("cityId", SHANGHAI);
        renderArgs.put("districtId", " ");
        renderArgs.put("areaId", " ");
        renderArgs.put("brandId", 0);
        renderArgs.put("priceFrom", 0);
        renderArgs.put("priceTo", 0);
        renderArgs.put("orderBy", 0);
        renderArgs.put("orderByType", 1);
        ValuePaginator goodsPage = new ValuePaginator(goodsList);
        render(goodsPage, areas, districts, categories, brands);
    }

    public static void show(long id) {
        models.sales.Goods goods = models.sales.Goods.findUnDeletedById(id);
        if (goods == null) {
            notFound();
        }

        render(goods);
    }

    public static void list(String condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        int pageSize = 12;

        try {
            GoodsCondition goodsCond = new GoodsCondition(condition);
            JPAExtPaginator<models.sales.Goods> goodsPage = models.sales
                    .Goods.findByCondition(goodsCond, pageNumber, pageSize);

            //默认取出前8个上海的区
            List<Area> districts = Area.findTopDistricts(SHANGHAI, LIMIT, goodsCond.districtId);
            List<Area> areas = Area.findTopAreas(goodsCond.districtId, LIMIT, goodsCond.areaId);
            List<Category> categories = Category.findTop(LIMIT, goodsCond.categoryId);
            List<Brand> brands = Brand.findTop(LIMIT, goodsCond.brandId);

            renderArgs.put("categoryId", goodsCond.categoryId);
            renderArgs.put("cityId", SHANGHAI);
            renderArgs.put("districtId", goodsCond.districtId);
            renderArgs.put("areaId", goodsCond.areaId);
            renderArgs.put("brandId", goodsCond.brandId);
            renderArgs.put("priceFrom", goodsCond.priceFrom);
            renderArgs.put("priceTo", goodsCond.priceTo);
            renderArgs.put("orderBy", goodsCond.orderByNum);
            renderArgs.put("orderByType", goodsCond.orderByTypeNum);

            render("/Goods/index.html", goodsPage, areas, districts, categories, brands);
        } catch (Exception e) {
            e.printStackTrace();
            index();
        }
    }
}
