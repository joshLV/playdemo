package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.Order;
import org.apache.commons.lang.StringUtils;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserSequences extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 资金页面
     */
    public static void index(AccountSequenceCondition condition) {

        User user = SecureCAS.getUser();
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        Account account = AccountUtil.getConsumerAccount(user.getId());
        condition.account = account;

        JPAExtPaginator<AccountSequence> amountList = AccountSequence.findByCondition(condition, pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : amountList.getCurrentPage()) {
            setOrderInfo(accountSequence);
        }

        BreadcrumbList breadcrumbs = new BreadcrumbList("资金明细", "/user-sequences");
        render(amountList, breadcrumbs, user, account, condition);
    }

    /**
     * 设置订单信息
     *
     * @param accountSequence
     * @return
     */
    private static Order setOrderInfo(AccountSequence accountSequence) {
        if (accountSequence.orderId != null) {
            Order order = Order.findById(accountSequence.orderId);
            if (order != null) {
                PaymentSource source = PaymentSource.find("byCode", order.payMethod).first();
                if (source != null && "99bill".equals(source.paymentCode)) {
                    accountSequence.payMethod = "快钱-" + source.name;
                } else if (source != null) {
                    accountSequence.payMethod = source.name;
                }
                accountSequence.orderNumber = order.orderNumber;
            }
            return order;
        }
        return null;
    }

}
