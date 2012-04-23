package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.Address;
import models.consumer.User;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
public class UserAddress extends Controller {

    /**
     * 地址列表
     */
    public static void index() {
        User user = SecureCAS.getUser();
        Address addList = Address.findDefault(user);
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的地址", "/address");
        render(addList, breadcrumbs, user);
    }

}
