package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceSummary;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.order.Order;
import models.resale.Resaler;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 财务报表.
 * <p/>
 * User: sujie
 * Date: 5/3/12
 * Time: 4:30 PM
 */
@With(OperateRbac.class)
public class OperateReports extends Controller {
    private static final int PAGE_SIZE = 10;

    /**
     * 查询消费者资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("consumers_account_reports")
    public static void showConsumerReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        User user = User.findByLoginName(condition.accountName);
        if (user != null) {
            condition.account = AccountUtil.getConsumerAccount(user.id);
        } else {
            condition.account = new Account();
            condition.account.accountType = AccountType.CONSUMER;
        }
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            User consumer = User.findById(accountSequence.account.uid);
            if (consumer != null) {
                accountSequence.accountName = consumer.loginName;
            }
            setOrderInfo(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    private static Order setOrderInfo(AccountSequence accountSequence) {
        if (accountSequence.orderId != null) {
            Order order = Order.findById(accountSequence.orderId);
            if (order != null) {
                accountSequence.payMethod = PaymentSource.findNameByCode(order.payMethod);
                accountSequence.orderNumber = order.orderNumber;
            }
            return order;
        }
        return null;
    }

    /**
     * 查询分销商资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("resales_account_reports")
    public static void showResaleReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        Resaler user = Resaler.findByLoginName(condition.accountName);
        if (user != null) {
            condition.account = AccountUtil.getResalerAccount(user.id);
        } else {
            condition.account = new Account();
            condition.account.accountType = AccountType.RESALER;
        }
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            Resaler resaler = Resaler.findById(accountSequence.account.uid);
            if (resaler != null) {
                accountSequence.accountName = resaler.loginName;
            }
            setOrderInfo(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    /**
     * 查询商户资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("suppliers_account_reports")
    public static void showSupplierReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        Supplier user = Supplier.findByFullName(condition.accountName);
        if (user != null) {
            condition.account = AccountUtil.getSupplierAccount(user.id);
        } else {
            condition.account = new Account();
            condition.account.accountType = AccountType.SUPPLIER;
        }
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            Supplier supplier = Supplier.findById(accountSequence.account.uid);
            if (supplier != null) {
                accountSequence.supplierName = supplier.fullName;
                accountSequence.accountName = supplier.loginName;
            }
            setOrderInfo(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    /**
     * 查询一百券佣金账户资金明细.
     */
    @ActiveNavigation("websites_account_reports")
    public static void showWebsiteReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        condition.account = AccountUtil.getUhuilaAccount();
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            setOrderInfo(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    private static void setPlatform(AccountSequence accountSequence) {
        TradeBill bill = TradeBill.findById(accountSequence.tradeId);
        if (bill == null) {
            return;
        }

        if (bill.fromAccount.getId().equals(accountSequence.account.getId())) {
            accountSequence.platform = bill.toAccount.accountType.name();
        } else {
            accountSequence.platform = bill.fromAccount.accountType.name();
        }
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    /**
     * 查询平台佣金账户资金明细.
     */
    @ActiveNavigation("platforms_account_reports")
    public static void showPlatformReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        condition.account = AccountUtil.getPlatformCommissionAccount();
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            setOrderInfo(accountSequence);
            setPlatform(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    /**
     * 查询收款账户资金明细.
     */
    @ActiveNavigation("incomings_account_reports")
    public static void showIncomingReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        condition.account = AccountUtil.getPlatformIncomingAccount();
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            setOrderInfo(accountSequence);
            setPlatform(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    @ActiveNavigation("withdraw_account_reports")
    public static void showWithdrawReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        condition.account = AccountUtil.getPlatformWithdrawAccount();
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            setOrderInfo(accountSequence);
            setPlatform(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);
        render(accountSequencePage, summary, condition);
    }

    @ActiveNavigation("financing_incoming_reports")
    public static void showFinancingIncomingReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        condition.account = AccountUtil.getFinancingIncomingAccount();
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage) {
            setOrderInfo(accountSequence);
            setPlatform(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);
        render(accountSequencePage, summary, condition);
    }
}

