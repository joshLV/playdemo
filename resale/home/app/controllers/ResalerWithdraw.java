package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.WithdrawBill;
import models.accounts.util.AccountUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.resale.Resaler;
import models.sms.SMSMessage;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * @author  likang
 * Date: 12-5-7
 */
@With(SecureCAS.class)
public class ResalerWithdraw extends Controller{

    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("withdraw_notification.email.receiver", "tangliqun@uhuila.com").split(",");
    private static String[] NOTIFICATION_MOBILES = Play.configuration.getProperty("withdraw_notification.mobile", "").trim().split(",");

    public static void index(){
        Resaler resaler = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(resaler.getId());
        List<WithdrawBill> withdrawBills = WithdrawBill.find("account =? order by appliedAt desc",account).fetch();
        render(withdrawBills);
    }

    public static void apply(){
        Resaler resaler = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(resaler.getId());
        render(account);
    }

    public static void create(@Valid WithdrawBill withdraw){
        Resaler resaler = SecureCAS.getResaler();
        Account account = AccountUtil.getResalerAccount(resaler.getId());

        if(Validation.hasErrors()){
            render("ResalerWithdraw/apply.html", withdraw, account);
        }
        if(withdraw.amount.compareTo(account.amount)>0){
            Validation.addError("withdraw.amount", "提现金额不能大于余额！！");
            render("ResalerWithdraw/apply.html", withdraw, account);
        }
        if(withdraw.apply(resaler.loginName, account, resaler.userName)){
            index();
        }else {
            error("申请失败");
        }
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
        message.putParam("uid", withdrawBill.account.uid);
        message.setTemplate("withdraw");
        MailUtil.sendCommonMail(message);

        if(NOTIFICATION_MOBILES.length > 0 && !"".equals(NOTIFICATION_MOBILES[0])){
            new SMSMessage("一百券用户" + withdrawBill.applier + "申请提现" + withdrawBill.amount + "元",
                    NOTIFICATION_MOBILES).send();
        }
    }
}
