package controllers;

import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.RandomNumberUtil;
import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.Voucher;
import models.accounts.VoucherType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.consumer.UserCondition;
import models.consumer.UserGoldenCoin;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;

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
        String isExchange = request.params.get("isExchange");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new UserCondition();
        }
        JPAExtPaginator<UserGoldenCoin> coinList = UserGoldenCoin.find(user, condition, pageNumber, PAGE_SIZE);
        BreadcrumbList breadcrumbs = new BreadcrumbList("金币明细", "/user-coins");

        Long coinsNumber = UserGoldenCoin.getTotalCoins(user);

        //兑换比例
        Long number = UserGoldenCoin.getPresentOfCoins(coinsNumber);
        render(coinList, breadcrumbs, user, coinsNumber, condition, number, isExchange);
    }

    /**
     * 兑换
     *
     * @param exNumber
     */
    public static void exchange(Long exNumber) {
        User user = SecureCAS.getUser();
        //总金币数
        Long coinsNumber = UserGoldenCoin.getTotalCoins(user);
        //兑换比例
        Long number = UserGoldenCoin.getPresentOfCoins(coinsNumber);
        if (exNumber > number) {
            error("对不起，你的金币暂时不够兑换！");
        }

        new UserGoldenCoin(user, -(exNumber * 500), null, "兑换" + exNumber + "张5元抵用券", true).save();
        Account account = AccountUtil.getConsumerAccount(user.id);
        String name = "一百券抵用券";
        String prefix = "YBQ" + RandomNumberUtil.generateRandomNumber(7);

        Date expiredAt = DateUtil.nextYear(new Date());
        Voucher.generate(exNumber.intValue(), new BigDecimal(5), name, prefix, account, user.id, VoucherType.EXCHANGE, expiredAt);
        redirect("/user-coins?isExchange=true");
    }


}
