package controllers;

import controllers.modules.website.cas.OAuthType;
import controllers.modules.website.cas.Security;
import controllers.modules.website.cas.annotations.SkipCAS;
import controllers.modules.website.cas.annotations.TargetOAuth;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: yan
 * Date: 13-3-22
 * Time: 下午4:36
 */
@With(Security.class)
@SkipCAS
//@TargetOAuth(OAuthType.SINA)
public class WebSinaVouchers extends Controller {

    /**
     * 从新浪微博入口，展示页面
     *
     * @param productId
     */
    public static void index(String productId) {
        Goods goods = ResalerProduct.getGoodsByPartnerProductId(productId, OuterOrderPartner.SINA);
        //检测查看来源，pc，wap
        render(goods,productId);
    }

}
