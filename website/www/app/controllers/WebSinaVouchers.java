package controllers;

import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.Security;
import controllers.modules.website.cas.annotations.SkipCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.Collection;

/**
 * User: yan
 * Date: 13-3-22
 * Time: 下午4:36
 */
@With({Security.class, WebsiteInjector.class})
@SkipCAS
@TargetOAuth(OAuthType.SINA)
public class WebSinaVouchers extends Controller {

    @Before
    private static void mobileCheck() {
        String url = request.url;
        if (isMobile()) {
            if (!url.startsWith(WEIBO_WAP)) {
                redirect(WEIBO_WAP + url.substring(6));
            }
        }
    }

    /**
     * 从新浪微博入口，展示页面
     *
     * @param productId
     */
    public static void index(String productId, String source) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        Collection<Shop> shops = goods.getShopList();

        String templatePath = "WebSinaVouchers/index" + StringUtils.capitalize(StringUtils.trimToEmpty(source)) + ".html";

        render(templatePath, goods, shops, productId);
    }

    /**
     * 检查是否从手机或除PC以外的设备访问页面
     *
     * @return
     */
    private static boolean isMobile() {
//        Http.Header headAgent = request.headers.get("user-agent");
//        String userAgent = headAgent.value();
//        for (String mobile : MOBILE_SPECIFIC_SUBSTRING) {
//            if (userAgent.contains(mobile)
//                    || userAgent.contains(mobile.toUpperCase())
//                    || userAgent.contains(mobile.toLowerCase())) {
//                return true;
//            }
//        }
//
//        return false;
        return true;
    }

    static final String WEIBO_WAP = "/weibo/wap";

    static final String[] MOBILE_SPECIFIC_SUBSTRING = {
            "iPad", "iPhone", "Android", "MIDP", "Opera Mobi",
            "Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile",
            "MSIEMobile", "Windows Phone", "HTC", "LG",
            "MOT", "Nokia", "Symbian", "Fennec",
            "Maemo", "Tear", "Midori", "armv",
            "Windows CE", "WindowsCE", "Smartphone", "240x320",
            "176x220", "320x320", "160x160", "webOS",
            "Palm", "Sagem", "Samsung", "SGH",
            "SIE", "SonyEricsson", "MMP", "UCWEB"};
}
