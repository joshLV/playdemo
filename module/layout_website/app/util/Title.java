package util;

import play.Play;

public class Title {

    /**
     * 返回首页的标题.
     * @return
     */
    public static String getHomeTitle() {
        if (Play.runingInTestMode()) {
            return "首页";
        }
        return "优惠券网,代金券，优惠券,一百券网-网上消费券首选门户";
    }
}
