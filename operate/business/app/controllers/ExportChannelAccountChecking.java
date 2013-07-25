package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: wangjia
 * Date: 13-7-25
 * Time: 上午10:20
 */
@With(OperateRbac.class)
@ActiveNavigation("export_58_account_checking")
public class ExportChannelAccountChecking extends Controller {
    public static void index() {

        render();
    }

}
