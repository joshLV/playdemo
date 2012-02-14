package controllers;

import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.webtrace.WebTrace;

/**
 * 商品控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 5:32 PM
 */
@With(WebTrace.class)
public class Goods extends Controller {

    public static void show(long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        render(goods);
    }

}
