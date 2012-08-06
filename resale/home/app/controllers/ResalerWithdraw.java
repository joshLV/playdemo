package controllers;

import controllers.modules.resale.cas.SecureCAS;
import models.accounts.*;
import models.accounts.util.AccountUtil;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.resale.Resaler;
import models.sms.SMSUtil;
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

    private static String NOTIFICATION_EMAIL = Play.configuration.getProperty("withdraw_notification.email.receiver", "jingyue.gong@seewi.com.cn");
    private static String NOTIFICATION_MOBILE = Play.configuration.getProperty("withdraw_notification.mobile", "").trim();

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
        if(withdraw.apply(resaler.loginName, account)){
            index();
        }else {
            error("申请失败");
        }
    }

    private static void sendNotification(WithdrawBill withdrawBill) {
        // 发邮件
        MailMessage message = new MailMessage();
        message.addRecipient(NOTIFICATION_EMAIL);
        message.setSubject("用户提现提醒");
        message.putParam("applier", withdrawBill.applier);
        message.putParam("amount", withdrawBill.amount);
        message.setTemplate("withdraw");
        MailUtil.sendFinanceNotificationMail(message);

        if(!"".equals(NOTIFICATION_MOBILE)){
            SMSUtil.send("一百券用户" + withdrawBill.applier + "申请提现" + withdrawBill.amount + "元", NOTIFICATION_MOBILE);
        }
    }
}
