package controllers;

import controllers.modules.website.cas.SecureCAS;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: yan
 * Date: 13-3-25
 * Time: 下午5:14
 */
@With({SecureCAS.class, WebsiteInjector.class})
//@TargetOAuth(OAuthType.SINA)
public class WebSinaPaymentInfo extends Controller {
    public static void index(String orderNumber) {
        render(orderNumber);
    }
}
