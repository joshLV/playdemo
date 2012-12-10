package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-10
 * Time: 上午11:03
 */
@With(OperateRbac.class)
@ActiveNavigation("goods_schedule")
public class GoodsSchedule extends Controller {
    public static void index() {
        render();
    }
    @ActiveNavigation("goods_schedule_add")
    public static void add() {
        render();
    }

    public static void create() {
        render();
    }

    public static void update() {
        render();
    }
}
