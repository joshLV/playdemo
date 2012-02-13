package controllers;

import models.sales.Goods;
import play.mvc.Controller;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/13/12
 * Time: 3:51 PM
 */
public class GoodsController extends Controller {

    public static void index() {
        render(Goods.findAll());
    }

}