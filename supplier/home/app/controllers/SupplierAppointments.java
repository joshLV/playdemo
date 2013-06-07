package controllers;

import controllers.supplier.SupplierInjector;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: yan
 * Date: 13-6-6
 * Time: 下午5:35
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierAppointments extends Controller {
    public void index() {
        render();
    }

}
