package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-25
 * Time: 上午10:14
 * To change this template use File | Settings | File Templates.
 */
@With(OperateRbac.class)
@ActiveNavigation("category_admin_index")
public class CategoryAdmin extends Controller {
    public static void index() {

        render();
    }


}
