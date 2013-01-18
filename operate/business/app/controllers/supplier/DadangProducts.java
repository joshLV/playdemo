package controllers.supplier;

import models.supplier.Supplier;
import play.mvc.Controller;

/**
 * 大东票务 产品同步。
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午4:46
 */
public class DadangProducts extends Controller {

    public static void sync() {
        Supplier dadong = Supplier.findByDomainName("dadang");

    }
}
