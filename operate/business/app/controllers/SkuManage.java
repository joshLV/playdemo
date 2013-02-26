package controllers;

import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-26
 * Time: 下午6:01
 */
@With(OperateRbac.class)
public class SkuManage extends Controller {
    public static void index() {
        render();
    }

    public static void add() {
        render();
    }

    public static void create() {
        render();
    }

    public static void edit() {
        render();
    }

    public static void update() {
        render();
    }

    public static void delete() {
        render();
    }
}
