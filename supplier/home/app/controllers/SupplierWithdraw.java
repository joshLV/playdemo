package controllers;

import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author likang
 * Date: 12-5-9
 */

@With(SupplierRbac.class)
public class SupplierWithdraw extends Controller{
    private static final int PAGE_SIZE = 20;
    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("withdraw_notification.email.receiver", "jingyue.gong@seewi.com.cn").split(",");
    private static String[] NOTIFICATION_MOBILES = Play.configuration.getProperty("withdraw_notification.mobile", "").trim().split(",");

    @Right("STATS")
    @ActiveNavigation("account_withdraw")
    public static void index(WithdrawBillCondition condition){
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = AccountUtil.getSupplierAccount(supplier.getId());

        String page = request.params.get("page");
        int pageNumber =  StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if(condition == null){
            condition = new WithdrawBillCondition();
        }
        condition.account = account;

        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        render(billPage, condition);
    }

    @ActiveNavigation("account_withdraw")
    public static void apply(){
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = AccountUtil.getSupplierAccount(supplier.getId());
        List<WithdrawAccount> withdrawAccounts = WithdrawAccount.findByUser(supplier.getId(), AccountType.SUPPLIER);

        render(account, withdrawAccounts);
    }

    @ActiveNavigation("account_withdraw")
    public static void create(Long withdrawAccountId, BigDecimal amount){
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);
        Account account = AccountUtil.getSupplierAccount(supplier.getId());
        WithdrawAccount withdrawAccount = WithdrawAccount.findByIdAndUser(
                withdrawAccountId, supplier.getId(), AccountType.SUPPLIER);
        if(withdrawAccount == null){
            error("invalid withdraw account");
        }
        if(amount == null || amount.compareTo(account.amount) > 0 || amount.compareTo(new BigDecimal("10")) < 0){
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

        if (withdraw.apply(SupplierRbac.currentUser().loginName, account, supplier.otherName)){
            sendNotification(withdraw);
            index(null);
        }else {
            error("申请失败");
        }
    }


    @ActiveNavigation("account_withdraw")
    public static void detail(Long id){
        Long supplierId = SupplierRbac.currentUser().supplier.id;
        Supplier supplier = Supplier.findById(supplierId);

        WithdrawBill bill = WithdrawBill.findByIdAndUser(id, supplier.getId(), AccountType.SUPPLIER);
        if(bill == null){
            error("withdraw bill not found");
        }
        render(bill);
    }

    private static void sendNotification(WithdrawBill withdrawBill) {
        // 发邮件
        MailMessage message = new MailMessage();
        message.addRecipient(NOTIFICATION_EMAILS);
        message.setFrom("yibaiquan <noreplay@uhuila.com>");
        message.setSubject("用户提现提醒");
        message.putParam("applier", withdrawBill.applier);
        message.putParam("amount", withdrawBill.amount);
        message.putParam("withdraw", withdrawBill.id);
        message.setTemplate("withdraw");
        MailUtil.sendFinanceNotificationMail(message);

        if(NOTIFICATION_MOBILES.length > 0 && !"".equals(NOTIFICATION_MOBILES[0])){
            SMSUtil.send("一百券用户" + withdrawBill.applier + "申请提现" + withdrawBill.amount + "元", NOTIFICATION_MOBILES);
        }
    }
}
