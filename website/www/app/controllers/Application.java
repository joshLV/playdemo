package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.Topic;
import play.mvc.Controller;
import play.mvc.With;

/**
 * .
 * <p/>
 * User: sujie
 * Date: 5/22/12
 * Time: 6:56 PM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void about() {
        render();
    }

    public static void help() {
        render();
    }

    public static void rebate() {
        render();
    }

    public static void contact() {
        render();
    }

    public static void service() {
        render();
    }

    public static void topic(Long id) {
        Topic topic = Topic.findById(id);
        if (topic == null) {
            error(404, "没有找到该商品！");
        }
        render(topic);
    }
}
