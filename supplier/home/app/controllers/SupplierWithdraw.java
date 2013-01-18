package controllers;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawAccount;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillCondition;
import models.accounts.util.AccountUtil;
import models.order.Prepayment;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

import static com.uhuila.common.util.DateUtil.getBeginOfDay;

/**
 * 1.0版，只做维护
 *
 * @author likang
 *         Date: 12-5-9
 */

@Deprecated
@With(SupplierRbac.class)
public class SupplierWithdraw extends Controller {
    private static final int PAGE_SIZE = 20;
    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("withdraw_notification.email.receiver", "jingyue.gong@seewi.com.cn").split(",");
    private static String[] NOTIFICATION_MOBILES = Play.configuration.getProperty("withdraw_notification.mobile", "").trim().split(",");

    @ActiveNavigation("account_withdraw")
    public static void index(WithdrawBillCondition condition) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = AccountUtil.getSupplierAccount(supplier.getId());

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new WithdrawBillCondition();
        }
        condition.account = account;
        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        render(billPage, condition);
    }

    @ActiveNavigation("account_withdraw")
    public static void apply() {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = AccountUtil.getSupplierAccount(supplier.getId());
        BigDecimal prepaymentBalance = Prepayment.findAmountBySupplier(supplier);

        List<WithdrawAccount> withdrawAccounts = WithdrawAccount.findByUser(supplier.getId(), AccountType.SUPPLIER);
        List<Prepayment> prepayments = Prepayment.findBySupplier(supplier);

        BigDecimal withdrawAmount = account.getWithdrawAmount(getBeginOfDay());
        BigDecimal supplierWithdrawAmount = account.getSupplierWithdrawAmount(prepaymentBalance, getBeginOfDay());
        render(account, withdrawAccounts, prepaymentBalance, prepayments, withdrawAmount, supplierWithdrawAmount);
    }

    @ActiveNavigation("account_withdraw")
    public static void create(Long withdrawAccountId, BigDecimal amount) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = AccountUtil.getSupplierAccount(supplier.getId());
        WithdrawAccount withdrawAccount = WithdrawAccount.findByIdAndUser(
                withdrawAccountId, supplier.getId(), AccountType.SUPPLIER);
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

        if (withdraw.apply(SupplierRbac.currentUser().loginName, account, supplier.otherName)) {
            // 不再通知 sendNotification(withdraw);
            index(null);
        } else {
            error("申请失败");
        }
    }


    @ActiveNavigation("account_withdraw")
    public static void detail(Long id) {
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);

        WithdrawBill bill = WithdrawBill.findByIdAndUser(id, supplier.getId(), AccountType.SUPPLIER);
        if (bill == null) {
            error("withdraw bill not found");
        }
        render(bill);
    }

}
