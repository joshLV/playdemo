package controllers;

import models.accounts.Account;
import models.accounts.WithdrawAccount;
import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillCondition;
import models.accounts.WithdrawBillStatus;
import models.accounts.util.AccountUtil;
import models.admin.SupplierUser;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.Prepayment;
import models.resale.Resaler;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static models.accounts.AccountType.SUPPLIER;

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
        if (bill.account.accountType == SUPPLIER) {
            Supplier supplier = Supplier.findById(uid);
            supplierFullName = supplier.fullName;
        }
        render(bill, uid, sum, supplierFullName);
    }

    /**
     * 审批提现申请.
     *
     * @param id
     * @param action
     * @param fee
     * @param comment
     */
    public static void approve(Long id, String action, BigDecimal fee, String comment) {
        WithdrawBill bill = WithdrawBill.findById(id);
        if (bill.status != WithdrawBillStatus.APPLIED) {
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

            sendAgreedSMS(bill, comment);
        } else if (action.equals("reject")) {
            bill.reject(comment);

            sendRejectedSMS(bill, comment);
        }
        index(null);
    }

    /**
     * 发送拒绝提现申请的短信.
     *
     * @param bill
     * @param comment
     */
    private static void sendRejectedSMS(WithdrawBill bill, String comment) {
        sendWithdrawnSMS(bill, comment, "申请提现:" + bill.amount + " 未通过.");

    }

    /**
     * 发送同意提现申请的短信.
     *
     * @param bill
     * @param comment
     */
    private static void sendAgreedSMS(WithdrawBill bill, String comment) {
        sendWithdrawnSMS(bill, comment, " 申请提现:" + bill.amount + " 已结款.");
    }

    private static void sendWithdrawnSMS(WithdrawBill bill, String comment, String sendContent) {
        Supplier supplier = Supplier.findById(bill.account.uid);

        String title = null;
        String mobile = null;


        if (StringUtils.isNotBlank(comment)) {
            sendContent += " 备注:" + comment;
        }

        switch (bill.account.accountType) {
            case SUPPLIER:
                String[] arrayName = bill.applier == null ? new String[0] : bill.applier.split("-");
                String admin = null;
                if (ArrayUtils.isNotEmpty(arrayName)) {
                    admin = (arrayName.length == 2) ? arrayName[1] : arrayName[0];
                }
                SupplierUser supplierUser = SupplierUser.findAdmin(bill.account.uid, admin);
                if (supplierUser != null) {
                    mobile = supplierUser.mobile;
                    title = arrayName[0];
                }
                sendContent += "如有疑问请致电4006262166";
                if (StringUtils.isNotBlank(OperateRbac.currentUser().mobile)) {
                    sendContent += "或致电" + OperateRbac.currentUser().mobile;
                }
                if (supplier != null && StringUtils.isNotBlank(supplier.accountLeaderMobile) && supplierUser != null && !supplier.accountLeaderMobile.equals(supplierUser.mobile)) {
                    SMSUtil.send(sendContent, supplier.accountLeaderMobile);
                }
                break;
            case CONSUMER:
                User user = User.findById(bill.account.uid);
                UserInfo userInfo = UserInfo.find("byUser", user).first();
                if (user == null || userInfo == null) {
                    return;
                }
                mobile = user.mobile;
                if (StringUtils.isNotBlank(userInfo.fullName)) {
                    title = userInfo.fullName;
                } else if (StringUtils.isNotBlank(user.loginName)) {
                    title = user.loginName;
                }
                sendContent += "如有疑问请致电4006262166";
                break;
            case RESALER:
                Resaler resaler = Resaler.findById(bill.account.uid);
                if (resaler == null) {
                    return;
                }
                mobile = resaler.mobile;
                title = bill.applier;
                sendContent += "如有疑问请致电4006262166";
                if (StringUtils.isNotBlank(OperateRbac.currentUser().mobile)) {
                    sendContent += "或致电" + OperateRbac.currentUser().mobile;
                }
                break;
        }
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(title)) {
            return;
        }
        sendContent = "【一百券】" + title + ", " + sendContent;
        SMSUtil.send(sendContent, mobile);

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
            List<WithdrawAccount> withdrawAccountList = WithdrawAccount.findByUser(supplier.id, SUPPLIER);

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
        List<Supplier> supplierList = getWithdrawSupplierList();
        Account supplierAccount = AccountUtil.getSupplierAccount(supplierId);
        Supplier supplier = Supplier.findById(supplierId);
        //结算总额
        BigDecimal amount = supplierAccount.getWithdrawAmount();
        //单笔预付款金额
        List<Prepayment> prepayments = Prepayment.getUnclearedPrepayments(supplier.id);
        BigDecimal prepaymentBalance = prepayments.size() > 0 ? prepayments.get(0).getBalance() : BigDecimal.ZERO;
        //实际需打款金额
        BigDecimal needPay = prepaymentBalance.compareTo(amount) >= 0 ? BigDecimal.ZERO : amount.subtract(prepaymentBalance);
        Long prepaymentId = prepaymentBalance.compareTo(BigDecimal.ZERO) > 0 ? prepayments.get(0).id : null;
        List<WithdrawAccount> withdrawAccountList = WithdrawAccount.findByUser(supplierId, SUPPLIER);

        render("/WithdrawApproval/settle.html", supplierList, supplierId, withdrawDate, amount, supplierAccount,
                supplier, prepaymentBalance, prepaymentId, needPay, withdrawAccountList);
    }

    /**
     * 结算商户资金,将结算金额与预付款金额进行绑定.
     *
     * @param supplierAccount
     * @param withdrawDate
     * @param withdrawAccountId
     * @param amount
     * @param fee
     * @param comment
     */
    public static void settle(Account supplierAccount, Date withdrawDate,
                              Long withdrawAccountId, BigDecimal amount, BigDecimal fee, String comment, Long prepaymentId) {
        //生成结算账单
        WithdrawAccount withdrawAccount = WithdrawAccount.findByIdAndUser(withdrawAccountId, supplierAccount.uid, SUPPLIER);
        WithdrawBill bill = new WithdrawBill();
        if (withdrawAccount != null) {
            bill.userName = withdrawAccount.userName;
            bill.bankCity = withdrawAccount.bankCity;
            bill.bankName = withdrawAccount.bankName;
            bill.subBankName = withdrawAccount.subBankName;
            bill.cardNumber = withdrawAccount.cardNumber;
        }
        bill.amount = amount;
        bill.fee = fee == null ? BigDecimal.ZERO : fee;
        Supplier supplier = Supplier.findById(supplierAccount.uid);
        //申请提现
        bill.apply(OperateRbac.currentUser().userName, supplierAccount, supplier.otherName);
        //审批提现
        Prepayment prepayment = null;
        if (prepaymentId != null) {
            prepayment = Prepayment.findById(prepaymentId);
        }
        int withdrawCount = bill.agree(fee, comment, withdrawDate, prepayment);
        if (withdrawCount > 0 && prepaymentId != null) {
            //将结算金额与预付款金额进行绑定
            if (prepayment != null && prepayment.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
                boolean payAll = Prepayment.pay(prepayment, bill.amount.add(bill.fee));
            }
            //发送结算通知短信
            sendWithdrawnSMS(bill, comment, "您的账户中有" + bill.amount + "已结款, 手续费" + bill.fee + "元,请查收.");
        }
        index(null);
    }
}
