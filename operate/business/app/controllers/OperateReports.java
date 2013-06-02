package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountSequenceStatistic;
import models.accounts.AccountSequenceSummary;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.accounts.TradeBill;
import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.operator.Operator;
import models.order.Order;
import models.resale.Resaler;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.With;
import util.DateHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            condition = getDefaultAccountSequenceCondition();
        }

        if (condition.accountName != null && !condition.accountName.trim().equals("")) {
            User user = User.findByLoginName(condition.accountName);
            if (user != null) {
                condition.account = AccountUtil.getConsumerAccount(user.id);
            } else {
                condition.account = new Account();
                condition.account.id = -1L;
            }
        }
        condition.accountType = AccountType.CONSUMER;

        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
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
            condition = getDefaultAccountSequenceCondition();
        }

        if (condition.accountName != null && !condition.accountName.trim().equals("")) {
            Resaler user = Resaler.findOneByLoginName(condition.accountName);
            if (user != null) {
                condition.account = AccountUtil.getResalerAccount(user);
            } else {
                condition.account = new Account();
                condition.account.id = -1L;
            }
        }
        condition.accountType = AccountType.RESALER;
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
            Resaler resaler = Resaler.findById(accountSequence.account.uid);
            if (resaler != null) {
                accountSequence.accountName = resaler.loginName;
            }
            setOrderInfo(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);

        render(accountSequencePage, summary, condition);
    }

    protected static AccountSequenceCondition getDefaultAccountSequenceCondition() {
        AccountSequenceCondition condition = new AccountSequenceCondition();
        condition.createdAtBegin = DateHelper.beforeDays(7);
        return condition;
    }


    /**
     * 查询商户资金明细.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("suppliers_account_reports")
    public static void showSupplierReport(AccountSequenceCondition condition, Long supplierId, Long shopId) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = getDefaultAccountSequenceCondition();
        }

        if (shopId != null && !shopId.equals(0L)) {
            condition.accountType = AccountType.SHOP;
            condition.account = AccountUtil.getShopAccount(shopId);
        } else if (supplierId != null && !supplierId.equals(0L)) {
            condition.accountType = AccountType.SUPPLIER;
            condition.account = AccountUtil.getSupplierAccount(supplierId);
//        } else {
//            condition.account = new Account();
//            condition.account.id = -1L;
        } else {
            condition.accountType = AccountType.SUPPLIER;
        }

        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
            if (accountSequence.account != null) {
                Supplier supplier = Supplier.findById(accountSequence.account.uid);
                if (supplier != null) {
                    accountSequence.supplierName = supplier.otherName + "/" + supplier.fullName;
                    accountSequence.accountName = supplier.loginName;
                }
                setOrderInfo(accountSequence);
            }
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (supplierId != null) {
            List<Shop> independentShops = Shop.getIndependentShops(supplierId);
            if (independentShops == null) {
                independentShops = new ArrayList<>();
            }
            renderArgs.put("independentShops", independentShops);
        }

        render(accountSequencePage, summary, supplierList, condition, supplierId, shopId);
    }

    /**
     * 查询商户提现汇总.
     *
     * @param condition 查询条件对象
     */
    @ActiveNavigation("suppliers_withdraw_reports")
    public static void showSupplierWithdrawReport(SupplierWithdrawCondition condition, Long supplierId, Long shopId) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = new SupplierWithdrawCondition(); //默认显示提现申请待审批的商户记录，统计周期为最近7天
            condition.createdAtBegin = DateHelper.beforeDays(6);
            condition.createdAtEnd = new Date();
            condition.withdrawBillStatus = WithdrawBillStatus.APPLIED;  //待审批 (时间和商户查询条件均为空)
        }
        if (shopId != null && !shopId.equals(0L)) {
            condition.accountType = AccountType.SHOP;
            condition.account = AccountUtil.getShopAccount(shopId);

        } else if (supplierId != null && !supplierId.equals(0L)) {
            condition.accountType = AccountType.SUPPLIER;
            condition.account = AccountUtil.getSupplierAccount(supplierId);
        } else {
            condition.accountType = AccountType.SUPPLIER;
        }
        if (condition.createdAtBegin == null && condition.createdAtEnd == null && supplierId == 0 || supplierId == null) {
            condition.withdrawBillStatus = WithdrawBillStatus.APPLIED;  //待审批 (时间和商户查询条件均为空)
        }
        if (condition.createdAtBegin == null && condition.createdAtEnd == null) {
            condition.createdAtBegin = DateHelper.beforeDays(6);
            condition.createdAtEnd = new Date();
        }
        if (condition.createdAtBegin == null && condition.createdAtEnd != null) {
            condition.createdAtBegin = com.uhuila.common.util.DateUtil.getBeforeDate(condition.createdAtEnd, 7);
        }

        List<SupplierWithdrawReport> supplierWithdrawList = SupplierWithdrawReport.query(condition);

        // 分页
        ValuePaginator<SupplierWithdrawReport> supplierWithdrawReportPage = utils.PaginateUtil.wrapValuePaginator(supplierWithdrawList, pageNumber, PAGE_SIZE);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (supplierId != null) {
            List<Shop> independentShops = Shop.getIndependentShops(supplierId);
            Supplier supplier = Supplier.findById(supplierId);
            if (independentShops == null) {
                independentShops = new ArrayList<>();
            }
            else if(supplier!=null){
                renderArgs.put("independentShopsSupplierName", supplier.getName());
            }
            renderArgs.put("independentShops", independentShops);

            Shop independentShop = Shop.find("supplierId=?", supplierId).first();
            String independentShopsName = independentShop.name;
            renderArgs.put("independentShopsName", independentShopsName);


        }

        render(supplierList, supplierWithdrawReportPage, condition, supplierId, shopId);
    }


    /**
     * 查询活动金账户资金明细
     */
    @ActiveNavigation("promotion_account_reports")
    public static void showPromotionReport(AccountSequenceCondition condition) {
        int pageNumber = getPageNumber();
        if (condition == null) {
            condition = getDefaultAccountSequenceCondition();
        }
        condition.account = AccountUtil.getPromotionAccount(Operator.defaultOperator());

        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
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
            condition = getDefaultAccountSequenceCondition();
        }

        condition.account = AccountUtil.getUhuilaAccount(Operator.defaultOperator());
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
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
            condition = getDefaultAccountSequenceCondition();
        }

        condition.account = AccountUtil.getPlatformCommissionAccount(Operator.defaultOperator());
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
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
            condition = getDefaultAccountSequenceCondition();
        }

        condition.account = AccountUtil.getPlatformIncomingAccount(Operator.defaultOperator());
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
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
            condition = getDefaultAccountSequenceCondition();
        }

        condition.account = AccountUtil.getPlatformWithdrawAccount(Operator.defaultOperator());
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
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
            condition = getDefaultAccountSequenceCondition();
        }

        condition.account = AccountUtil.getFinancingIncomingAccount(Operator.defaultOperator());
        JPAExtPaginator<AccountSequence> accountSequencePage = AccountSequence.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        for (AccountSequence accountSequence : accountSequencePage.getCurrentPage()) {
            setOrderInfo(accountSequence);
            setPlatform(accountSequence);
        }

        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);
        render(accountSequencePage, summary, condition);
    }

    /**
     * 商户资金明细的统计
     */
    @ActiveNavigation("suppliers_account_reports")
    public static void statisticSupplierReport(AccountSequenceCondition condition, Long supplierId, Long shopId) {
        if (condition == null) {
            condition = getDefaultAccountSequenceCondition();
        }


        if (shopId != null && !shopId.equals(0L)) {
            condition.accountType = AccountType.SHOP;
            condition.account = AccountUtil.getShopAccount(shopId);
        } else if (supplierId != null && !supplierId.equals(0L)) {
            condition.accountType = AccountType.SUPPLIER;
            condition.account = AccountUtil.getSupplierAccount(supplierId);
//        } else {
//            condition.account = new Account();
//            condition.account.id = -1L;
        } else {
            condition.accountType = AccountType.SUPPLIER;
        }


        List<AccountSequenceStatistic> statisticList = AccountSequence.statisticByCondition(condition);
        AccountSequenceSummary summary = AccountSequence.findSummaryByCondition(condition);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render("OperateReports/showSupplierReport.html", statisticList, summary, supplierList, condition,supplierId,shopId);
    }
}

