package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.User;
import models.consumer.UserCoin;
import models.consumer.UserCondition;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-12
 * Time: 下午4:32
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class UserCoins extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 积分页面
     */
    public static void index(UserCondition condition) {
        User user = SecureCAS.getUser();
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new UserCondition();
        }
        JPAExtPaginator<UserCoin> coinList = UserCoin.find(user, condition, pageNumber, PAGE_SIZE);
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的金币", "#", "金币明细", "/user-coins");

        Long coins = UserCoin.coinNumber(user, coinList);
        render(coinList, breadcrumbs, user, coins, condition);
    }
}
