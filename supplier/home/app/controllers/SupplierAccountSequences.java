package controllers;

import controllers.supplier.SupplierInjector;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.TradeType;
import models.accounts.util.AccountUtil;
import models.order.OrderItems;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 商户的资金明细.
 * <p/>
 * User: sujie
 * Date: 1/25/13
 * Time: 3:40 PM
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierAccountSequences extends Controller {

    private static final int PAGE_SIZE = 20;

    @ActiveNavigation("account_sequence")
    public static void index(AccountSequenceCondition condition) {
        Long accountId = SupplierRbac.currentUser().supplier.id;
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        Account account = AccountUtil.getSupplierAccount(accountId);
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }
        condition.account = account;

        JPAExtPaginator<AccountSequence> accountSequences = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        //account_sequence记录的备注是订单的首个商品名，为避免出现显示别家商户的商品名，在此重新查一遍
        for (AccountSequence sequence : accountSequences) {
            if (sequence.tradeType != TradeType.PURCHASE_COSTING) {
                continue;
            }
            List<OrderItems> orderItems = OrderItems.findBySupplierOrder(accountId, sequence.orderId);
            if (orderItems.size() > 0) {
                sequence.orderNumber = orderItems.get(0).order.orderNumber;
                String postfix = orderItems.size() > 1 ? "等" + orderItems.size() + "个商品" : "";
                sequence.remark = orderItems.get(0).goods.shortName + postfix;
            }
        }
        renderArgs.put("condition", condition);

        render(account, accountSequences);
    }

}