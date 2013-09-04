package controllers;

import controllers.supplier.SupplierInjector;
import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.order.Order;
import models.order.OrderItems;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
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
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new AccountSequenceCondition();
            condition.createdAtBegin = DateUtils.addDays(new Date(), -15);
        }
        condition.account = account;

        JPAExtPaginator<AccountSequence> accountSequences = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        //account_sequence记录的备注是订单的首个商品名，为避免出现显示别家商户的商品名，在此重新查一遍
        for (AccountSequence sequence : accountSequences.getCurrentPage()) {
            if (sequence.tradeType != TradeType.PURCHASE_COSTING && sequence.tradeType != TradeType.REFUND && sequence.tradeType != TradeType.SUPPLIER_CHEATED) {
                continue;
            }
            List<OrderItems> orderItems = OrderItems.findBySupplierOrder(account.id, sequence.orderId);
            if (orderItems.size() > 0) {
                sequence.orderNumber = orderItems.get(0).order.orderNumber;
                String postfix = orderItems.size() > 1 ? "等" + orderItems.size() + "个商品" : "";
                sequence.remark = orderItems.get(0).goods.shortName + postfix;
            }
            AccountSequence.setAccountSequenceInfo(sequence);
            setOrderInfo(sequence);
        }
        renderArgs.put("condition", condition);

        render(account, accountSequences);
    }

    private static Order setOrderInfo(AccountSequence accountSequence) {
        if (accountSequence.orderId != null) {
            Order order = Order.findById(accountSequence.orderId);
            if (order != null) {
                accountSequence.orderNumber = order.orderNumber;
                if (order.rebateValue.compareTo(BigDecimal.ZERO) > 0) {
                    accountSequence.sendCoupon = true;
                }
            }
            return order;
        }
        return null;
    }
}