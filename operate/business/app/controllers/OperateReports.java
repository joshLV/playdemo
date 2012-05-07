package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceSummary;
import models.accounts.AccountType;
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
     * 查询消费者报表.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("consumers_reports")
    public static void showConsumerReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        User user = User.findByLoginName(condition.accountName);
        if (user != null) {
            condition.account = AccountUtil.getAccount(user.id, AccountType.CONSUMER);
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
                accountSequence.payMethod = order.payMethod;
                accountSequence.orderNumber = order.orderNumber;
            }
            return order;
        }
        return null;
    }

    /**
     * 查询分销商报表.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("resales_reports")
    public static void showResaleReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        Resaler user = Resaler.findByLoginName(condition.accountName);
        if (user != null) {
            condition.account = AccountUtil.getAccount(user.id, AccountType.RESALER);
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
     * 查询商户报表.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("suppliers_reports")
    public static void showSupplierReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new AccountSequenceCondition();
        }

        Supplier user = Supplier.findByFullName(condition.accountName);
        if (user != null) {
            condition.account = AccountUtil.getAccount(user.id, AccountType.SUPPLIER);
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
            }
            Order order = setOrderInfo(accountSequence);
            if (order != null) {
                User consumer = order.getUser();
                if (consumer != null) {
                    accountSequence.accountName = consumer.loginName;
                }
            }
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    /**
     * 查询优惠啦佣金账户报表.
     */
    @ActiveNavigation("websites_reports")
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
        TradeBill bill = TradeBill.findById(accountSequence.referenceSerialId);
        if (bill!=null){
            if(AccountType.RESALER.equals(bill.fromAccount.accountType)){
                accountSequence.platform = AccountType.RESALER.name();
            }else if(AccountType.CONSUMER.equals(bill.fromAccount.accountType)){
                accountSequence.platform = AccountType.CONSUMER.name();
            }
        }
    }

    private static int getPageNumber() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    /**
     * 查询平台佣金账户报表.
     */
    @ActiveNavigation("platforms_reports")
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
     * 查询收款账户报表.
     */
    @ActiveNavigation("incomings_reports")
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
}