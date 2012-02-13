package controllers;

import models.sales.Goods;
import play.mvc.Controller;

import java.util.List;

/**
 * 首页控制器.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 9:57 AM
 */
public class Home extends Controller {

    public static void index() {
        List<Goods> goodsList = Goods.findTopByCategory(0,12);

        render(goodsList);
    }

}