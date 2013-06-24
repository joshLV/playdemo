package controllers;

import controllers.supplier.SupplierInjector;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawAccount;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillCondition;
import models.admin.SupplierUser;
import models.order.Prepayment;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

import static com.uhuila.common.util.DateUtil.getBeginOfDay;

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

        render(account, billPage, condition);
    }

    @ActiveNavigation("account_withdraw")
    public static void apply() {
        SupplierUser supplierUser = SupplierRbac.currentUser();

        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();
        BigDecimal prepaymentBalance = Prepayment.findAmountBySupplier(supplier);

        Logger.info("account.accountType:" + account.accountType);
        System.out.println(supplierUser.shop.id+"=====");
        List<WithdrawAccount> withdrawAccounts = account.accountType == AccountType.SHOP ? WithdrawAccount.findByShop(supplierUser.shop.id) :
                WithdrawAccount.findByUser(supplier.getId(), AccountType.SUPPLIER);
        List<Prepayment> prepayments = Prepayment.findBySupplier(supplier);

        BigDecimal withdrawAmount = account.getWithdrawAmount(getBeginOfDay());
        //商户可提现金额
        BigDecimal supplierWithdrawAmount = account.getSupplierWithdrawAmount(prepaymentBalance, getBeginOfDay());
        Logger.info("withdrawAmount=%s, supplierWithdrawAmount=%s, prepaymentBalance=%s", withdrawAmount.toString(),
                supplierWithdrawAmount.toString(), prepaymentBalance.toString());
        render(account, withdrawAccounts, prepaymentBalance, prepayments, withdrawAmount, supplierWithdrawAmount);
    }

    @ActiveNavigation("account_withdraw")
    public static void create(Long withdrawAccountId, BigDecimal amount) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = supplierUser.getSupplierAccount();
        WithdrawAccount withdrawAccount = account.accountType == AccountType.SHOP ?
                WithdrawAccount.findByIdAndUser(withdrawAccountId, supplierUser.shop.id, AccountType.SHOP) :
                WithdrawAccount.findByIdAndUser(withdrawAccountId, supplier.getId(), AccountType.SUPPLIER);
        if (withdrawAccount == null) {
            error("invalid withdraw account");
        }
        if (amount == null || amount.compareTo(account.amount) > 0 || amount.compareTo(new BigDecimal("10")) < 0) {
            Validation.addError("amount", "提现金额不能小于10元,且不能大于余额！！");
            params.flash();
            Validation.keep();
            apply();
        }

        WithdrawBill withdraw = new WithdrawBill();
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
}
