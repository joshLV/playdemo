package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillCondition;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * @author likang
 *         Date: 12-5-10
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class UserWithdraw extends Controller {
    private static final int PAGE_SIZE = 20;
    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("withdraw_notification.email.receiver", "tangliqun@uhuila.com").split(",");
    private static String[] NOTIFICATION_MOBILES = Play.configuration.getProperty("withdraw_notification.mobile", "").trim().split(",");

    public static void index(WithdrawBillCondition condition) {
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new WithdrawBillCondition();
        }
        condition.account = account;

        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        BreadcrumbList breadcrumbs = new BreadcrumbList("提现申请", "/withdraw");

        render(user, account, billPage, condition, breadcrumbs);
    }

    public static void apply() {
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        BreadcrumbList breadcrumbs = new BreadcrumbList("提现申请", "/withdraw");
        render(account, breadcrumbs);
    }

    public static void create(@Valid WithdrawBill withdraw) {
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        if (withdraw.amount != null && withdraw.amount.compareTo(account.amount) > 0) {
            Validation.addError("withdraw.amount", "提现金额不能大于余额！！");
            render("UserWithdraw/apply.html", withdraw, account);
        }
        if (Validation.hasErrors()) {
            render("UserWithdraw/apply.html", withdraw, account);
        }

        if (withdraw.apply(user.loginName, account, user.loginName)) {
            // 不再需要通知 sendNotification(withdraw);
            index(null);
        } else {
            error("申请失败");
        }
    }

    public static void detail(Long id) {
        User user = SecureCAS.getUser();
        WithdrawBill bill = WithdrawBill.findByIdAndUser(id, user.getId(), AccountType.CONSUMER);
        if (bill == null) {
            error("withdraw bill not found");
        }
        BreadcrumbList breadcrumbs = new BreadcrumbList("提现申请", "/withdraw");
        render(bill, breadcrumbs);
    }

}
