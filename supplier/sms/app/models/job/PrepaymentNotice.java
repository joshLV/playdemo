package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.WithdrawBill;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.Prepayment;
import models.supplier.Supplier;
import play.Play;
import play.jobs.Job;
import play.jobs.On;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 每天整点检查预付款销售情况，发送短信提醒10%剩余金额的供应商
 * <p/>
 * User: sujie
 * Date: 12/3/12
 * Time: 11:34 AM
 */
@On("0 0 12 * * ?")
public class PrepaymentNotice extends Job {
    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("withdraw_notification.email.receiver", "jingyue.gong@seewi.com.cn").split(",");

    /**
     * 在预付款余额少于10%时给财务发邮件预警
     *
     * 邮件内容：[商户名]预付款余额(总额-已消费)已少于10%。[该商户预付款金额明细url]
     *
     * @throws ParseException
     */
    @Override
    public void doJob() throws ParseException {
        List<Supplier> suppliers = Supplier.findUnDeleted();
        for (Supplier supplier : suppliers) {
            List<Prepayment> prepayments = Prepayment.find("deleted = ? and expireAt>? and supplier.id=?",
                    DeletedStatus.UN_DELETED,
                    new Date(),
                    supplier.id).fetch();

        }
    }

    private static void sendNotification(WithdrawBill withdrawBill) {
        // 发邮件
        MailMessage message = new MailMessage();
        message.addRecipient(NOTIFICATION_EMAILS);
        message.setFrom("yibaiquan <noreplay@uhuila.com>");
        message.setSubject("预付款余额已少于10%");
        message.putParam("applier", withdrawBill.applier);
        message.putParam("amount", withdrawBill.amount);
        message.putParam("withdraw", withdrawBill.id);
        message.setTemplate("withdraw");
        MailUtil.sendPrepaymentNoticeMail(message);
    }

}
