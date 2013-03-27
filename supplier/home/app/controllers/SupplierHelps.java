package controllers;

import play.mvc.Controller;
import controllers.supplier.SupplierInjector;

import play.mvc.With;

/**
 * 商户帮助页面
 * <p/>
 * User: wangjia
 * Date: 13-3-27
 * Time: 上午10:12
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierHelps extends Controller {
    public static void verify() {
        render();
    }
}
