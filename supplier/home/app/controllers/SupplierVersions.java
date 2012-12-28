package controllers;

import navigation.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 商户版本切换控制.
 * <p/>
 * User: sujie
 * Date: 12/27/12
 * Time: 4:30 PM
 */
@With(SupplierRbac.class)
@ActiveNavigation("coupons_multi_index")
public class SupplierVersions extends Controller {

    public static void change(int number) {
        switch (number) {
            case 1:

                break;
            case 2:
                break;
        }
    }
}