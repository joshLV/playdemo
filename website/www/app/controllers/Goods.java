package controllers;

import controllers.modules.webcas.WebCAS;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 商品控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 5:32 PM
 */
@With(WebCAS.class)
public class Goods extends Controller {

    public static void show(long id) {
        models.sales.Goods goods = models.sales.Goods.findById(id);
        render(goods);
    }

}
