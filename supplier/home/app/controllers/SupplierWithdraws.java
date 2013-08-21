package controllers;

import controllers.supplier.SupplierInjector;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawAccount;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillCondition;
import models.accounts.WithdrawBillStatus;
import models.admin.SupplierUser;
import models.operator.OperateUser;
import models.order.Prepayment;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 商户的提现管理.
 * <p/>
 * User: sujie
 * Date: 1/25/13
 * Time: 3:41 PM
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierWithdraws extends Controller {
    private static final int PAGE_SIZE = 20;
    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("withdraw_notification.email.receiver", "tangliqun@uhuila.com").split(",");
    private static String[] NOTIFICATION_MOBILES = Play.configuration.getProperty("withdraw_notification.mobile", "").trim().split(",");

    @ActiveNavigation("account_withdraw")
    public static void index(WithdrawBillCondition condition) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new WithdrawBillCondition();
        }
        condition.account = account;
        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);
        WithdrawBill withdraw = WithdrawBill.find("byAccountAndStatus", account, WithdrawBillStatus.APPLIED).first();

        render(account, billPage, condition, withdraw);
    }

    @ActiveNavigation("account_withdraw")
    public static void apply() {
        SupplierUser supplierUser = SupplierRbac.currentUser();

        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();

        WithdrawBill withdraw = WithdrawBill.find("byAccountAndStatus", account, WithdrawBillStatus.APPLIED).first();
        if (withdraw != null) {
            String err = "您有一笔待审批的提现申请，请等待审批完毕再申请提现";
            render(err);
        }

        BigDecimal prepaymentBalance = Prepayment.findAmountBySupplier(supplier);

        Logger.info("account.accountType:" + account.accountType);
        List<WithdrawAccount> withdrawAccounts = account.accountType == AccountType.SHOP ? WithdrawAccount.findByShop(supplierUser.shop.id) :
                WithdrawAccount.findByUser(supplier.getId(), AccountType.SUPPLIER);
        List<Prepayment> prepayments = Prepayment.findBySupplier(supplier);

        Date withDrawEndDate = getSupplierWithdrawEndDate(supplier);

//        BigDecimal withdrawAmount = account.getWithdrawAmount(withDrawEndDate);
        BigDecimal withdrawAmount = account.getWithdrawAmount(withDrawEndDate);
        //商户可提现金额
        BigDecimal supplierWithdrawAmount = account.getSupplierWithdrawAmount(prepaymentBalance, withDrawEndDate);
        Boolean canNotWithdraw = null;
        String salesName = "";
        String salesPhone = "";
        if (supplier.isSetWithdrawAmount() && supplier.withdrawAmount == null) {
            canNotWithdraw = Boolean.TRUE;
        } else if (supplier.isSetWithdrawAmount() && supplier.withdrawAmount != null) {
            canNotWithdraw = Boolean.FALSE;
        }
        OperateUser operateUser = OperateUser.findById(supplier.salesId);
        salesName = operateUser.userName;
        salesPhone = operateUser.mobile;

        //判断该商户是否设置预留金和最少提现金额
        String lessWithdrawAmount = supplier.getProperty(Supplier.SET_LESS_WITHDRAW_AMOUNT);
        String reserveAmount = supplier.getProperty(Supplier.SET_RESERVE_AMOUNT);
        BigDecimal lessWithdrawAmountToBigDecimal = StringUtils.isBlank(lessWithdrawAmount) ? BigDecimal.ZERO : new BigDecimal(lessWithdrawAmount);
        BigDecimal reserveAmountToBigDecimal = StringUtils.isBlank(reserveAmount) ? BigDecimal.ZERO : new BigDecimal(reserveAmount);

        supplierWithdrawAmount = supplierWithdrawAmount.subtract(lessWithdrawAmountToBigDecimal).subtract(reserveAmountToBigDecimal);
        supplierWithdrawAmount = supplierWithdrawAmount.compareTo(BigDecimal.ZERO) > 0 ? supplierWithdrawAmount : BigDecimal.ZERO;

        renderArgs.put("lessWithdrawAmount",lessWithdrawAmountToBigDecimal);
        renderArgs.put("reserveAmount",reserveAmountToBigDecimal);
        render(account, withdrawAccounts, prepaymentBalance, prepayments, withdrawAmount, supplierWithdrawAmount,
                withDrawEndDate, supplier, canNotWithdraw, salesName, salesPhone);
    }

    @ActiveNavigation("account_withdraw")
    public static void create(Long withdrawAccountId, BigDecimal amount, BigDecimal setAmount, Boolean canNotWithdraw) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();
        //销售设置商户的提现金额
        if (canNotWithdraw == Boolean.FALSE) {
            amount = setAmount;
        } else {
            canNotWithdraw = Boolean.TRUE;
        }

        WithdrawBill withdraw = WithdrawBill.find("byAccountAndStatus", account, WithdrawBillStatus.APPLIED).first();
        if (withdraw != null) {
            error("您有一笔待审批的提现申请，请等待审批完毕再申请提现");
        }


        WithdrawAccount withdrawAccount = account.accountType == AccountType.SHOP ?
                WithdrawAccount.findByIdAndUser(withdrawAccountId, supplierUser.shop.id, AccountType.SHOP) :
                WithdrawAccount.findByIdAndUser(withdrawAccountId, supplier.getId(), AccountType.SUPPLIER);
        if (withdrawAccount == null) {
            error("invalid withdraw account");
        }


        if (Boolean.TRUE.compareTo(canNotWithdraw) != 1 && (amount == null || amount.compareTo(account.amount) > 0
                || amount.compareTo(BigDecimal.TEN) < 0)) {
            Validation.addError("amount", "提现金额不能小于10元,且不能大于余额！！");
            params.flash();
            Validation.keep();
            apply();
        }

        withdraw = new WithdrawBill();
        withdraw.userName = withdrawAccount.userName;
        withdraw.account = account;
        withdraw.bankCity = withdrawAccount.bankCity;
        withdraw.bankName = withdrawAccount.bankName;
        withdraw.subBankName = withdrawAccount.subBankName;
        withdraw.cardNumber = withdrawAccount.cardNumber;
        withdraw.amount = amount;

        String accountName = account.accountType == AccountType.SHOP ? supplier.otherName + ":" + supplierUser.shop.name : supplier.otherName;
        if (withdraw.apply(SupplierRbac.currentUser().loginName, account, accountName)) {
            // 不再通知 sendNotification(withdraw);
            supplier.withdrawAmount = null;
            supplier.reason = null;
            supplier.save();
            index(null);
        } else {
            error("申请失败");
        }
    }


    @ActiveNavigation("account_withdraw")
    public static void detail(Long id) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();

        WithdrawBill bill = account.accountType == AccountType.SHOP ?
                WithdrawBill.findByIdAndUser(id, supplierUser.shop.id, AccountType.SHOP) :
                WithdrawBill.findByIdAndUser(id, supplier.getId(), AccountType.SUPPLIER);
        if (bill == null) {
            error("withdraw bill not found");
        }
        render(bill);
    }

    private static Date getSupplierWithdrawEndDate(Supplier supplier) {
        if (supplier.isWithdrawDelay()) {
            Calendar calendar = Calendar.getInstance();
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth >= 5 && dayOfMonth <= 14) {
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 25);
            } else if (dayOfMonth >= 15 && dayOfMonth <= 24) {
                calendar.set(Calendar.DAY_OF_MONTH, 5);
            } else {
                if (dayOfMonth <= 4) {
                    calendar.add(Calendar.MONTH, -1);
                }
                calendar.set(Calendar.DAY_OF_MONTH, 15);
            }
            return DateUtils.ceiling(calendar.getTime(), Calendar.DATE);
        } else {
            return DateUtils.truncate(new Date(), Calendar.DATE);
        }
    }

}
