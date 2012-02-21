package controllers;

import models.sales.Goods;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import controllers.modules.webtrace.WebTrace;

/**
 * 首页控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 9:57 AM
 */
@With(WebTrace.class)
public class Home extends Controller {

    public static void index() {
        List<Goods> goodsList = Goods.findTopByCategory(0,12);

        render(goodsList);
    }

}
