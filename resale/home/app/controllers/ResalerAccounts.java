package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceFlag;
import models.accounts.util.AccountUtil;
import models.order.Order;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Map;

/**
 * 分销商账户明细控制器
 *
 * @author likang
 */
@With(SecureCAS.class)
public class ResalerAccounts extends Controller {
    private static final int PAGE_SIZE = 20;

    public static void index(AccountSequenceCondition condition) {

        Resaler resaler = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(resaler.getId());

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        String interval = request.params.get("interval");
        renderArgs.put("interval", interval);

        if (condition == null) {
            condition = new AccountSequenceCondition();
        } else {
            renderArgs.put("createdAtBegin", condition.createdAtBegin);
            renderArgs.put("createdAtEnd", condition.createdAtEnd);
        }
        condition.account = account;
        JPAExtPaginator<AccountSequence> seqs = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence seq : seqs.getCurrentPage()) {
            if (StringUtils.isEmpty(seq.remark)) {
                Order order = Order.findById(seq.orderId);
                if (order != null && order.orderItems != null) {
                    if (order.orderItems.size() > 1) {
                        seq.remark = order.orderItems.get(0).goodsName + "等...";
                    } else {
                        seq.remark = order.orderItems.get(0).goodsName;
                    }
                }
            }
        }

        Map<AccountSequenceFlag, Object[]> summaryReport = AccountSequence.summaryReport(account);
        renderArgs.put("summaryReport", summaryReport);

        render(account, seqs);
    }

}
