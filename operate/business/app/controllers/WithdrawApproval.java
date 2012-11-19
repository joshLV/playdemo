package controllers;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.WithdrawAccount;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillCondition;
import models.accounts.WithdrawBillStatus;
import models.accounts.util.AccountUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 审批提现申请
 *
 * @author likang
 *         Date: 12-5-7
 */

@With(OperateRbac.class)
@ActiveNavigation("withdraw_approval_index")
public class WithdrawApproval extends Controller {
    private static final int PAGE_SIZE = 20;

    public static void index(WithdrawBillCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new WithdrawBillCondition();
        }


        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);


        render(billPage, condition);
    }

    public static void detail(Long id, Long uid) {
        WithdrawBill bill = WithdrawBill.findById(id);
        if (bill == null) {
            error("withdraw bill not found");
        }
        List<WithdrawBill> withdrawBillList = WithdrawBill.find("status=? and applier=?", WithdrawBillStatus.SUCCESS, bill.applier).fetch();
        BigDecimal temp = BigDecimal.ZERO;
        Double sum = 0d;
        String supplierFullName = "";
        for (WithdrawBill b : withdrawBillList) {
            sum += temp.add(b.amount).doubleValue();
        }
        if (bill.account.accountType == AccountType.SUPPLIER) {
            Supplier supplier = Supplier.findById(uid);
            supplierFullName = supplier.fullName;
        }
        render(bill, uid, sum, supplierFullName);
    }

    public static void approve(Long id, String action, BigDecimal fee, String comment) {
        WithdrawBill bill = WithdrawBill.findById(id);
        if (bill == null || bill.status != WithdrawBillStatus.APPLIED) {
            error("cannot find the withdraw bill or the bill is processed");
            return;
        }
        if (action.equals("agree")) {
            if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
                error("invalid fee");
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(bill.appliedAt);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date withdrawDate = cal.getTime();
            bill.agree(fee, comment, withdrawDate);
        } else if (action.equals("reject")) {
            bill.reject(comment);
        }
        index(null);
    }

    /**
     * 进入结算页面.
     */
    public static void initSettle() {
        List<Supplier> supplierList = getWithdrawSupplierList();

        render("/WithdrawApproval/settle.html", supplierList);
    }

    private static List<Supplier> getWithdrawSupplierList() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<Supplier> supplierResult = new ArrayList<>();

        for (Supplier supplier : supplierList) {
            Account supplierAccount = AccountUtil.getSupplierAccount(supplier.id);
            BigDecimal amount = supplierAccount.getWithdrawAmount();
            List<WithdrawAccount> withdrawAccountList = WithdrawAccount.findByUser(supplier.id, AccountType.SUPPLIER);

            if (amount.compareTo(BigDecimal.ZERO) > 0 && CollectionUtils.isNotEmpty(withdrawAccountList)) {
                supplier.otherName += "(账户金额:" + amount + ")";
                supplierResult.add(supplier);
            }
        }
        return supplierResult;
    }

    /**
     * 结算信息确认.
     */
    public static void confirmSettle(Long supplierId, Date withdrawDate) {
        System.out.println("supplierId:" + supplierId);
        List<Supplier> supplierList = getWithdrawSupplierList();
        Account supplierAccount = AccountUtil.getSupplierAccount(supplierId);
        Supplier supplier = Supplier.findById(supplierId);
        List<WithdrawAccount> withdrawAccountList = WithdrawAccount.findByUser(supplierId, AccountType.SUPPLIER);
        BigDecimal amount = supplierAccount.getWithdrawAmount();//AccountSequence.getIncomeAmount(supplierAccount, DateUtil.getBeginOfDay(withdrawDate));
        render("/WithdrawApproval/settle.html", supplierList, supplierId, withdrawDate, withdrawAccountList, amount, supplierAccount, supplier);
    }

    /**
     * 结算商户资金.
     *
     * @param supplierAccount
     * @param withdrawDate
     * @param withdrawAccountId
     * @param amount
     * @param fee
     * @param comment
     */
    public static void settle(Account supplierAccount, Date withdrawDate,
                              Long withdrawAccountId, BigDecimal amount, BigDecimal fee, String comment) {
        //生成结算账单
        System.out.println("withdrawAccountId:" + withdrawAccountId);
        WithdrawAccount withdrawAccount = WithdrawAccount.findByIdAndUser(withdrawAccountId, supplierAccount.uid, AccountType.SUPPLIER);
        WithdrawBill bill = new WithdrawBill();
        bill.userName = withdrawAccount.userName;
        bill.bankCity = withdrawAccount.bankCity;
        bill.bankName = withdrawAccount.bankName;
        bill.subBankName = withdrawAccount.subBankName;
        bill.cardNumber = withdrawAccount.cardNumber;
        bill.amount = amount;
        bill.fee = fee;
        Supplier supplier = Supplier.findById(supplierAccount.uid);
        bill.apply(OperateRbac.currentUser().userName, supplierAccount, supplier.otherName);
        bill.agree(fee, comment, withdrawDate);
        index(null);
    }
}
