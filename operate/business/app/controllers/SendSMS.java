package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-6
 * Time: 上午11:16
 * To change this template use File | Settings | File Templates.
 */

@With(OperateRbac.class)
@ActiveNavigation("send_sms")
public class SendSMS extends Controller {

    public static void index() {

        render();
    }

}
