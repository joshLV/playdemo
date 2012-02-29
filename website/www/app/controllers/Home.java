package controllers;

import controllers.modules.webcas.WebCAS;
import models.sales.Area;
import models.sales.Category;
import models.sales.Goods;
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
@With(WebCAS.class)
public class Home extends Controller {

    public static void index() {
        List<Goods> goodsList = Goods.findTop(12);

        render(goodsList);
    }
}
