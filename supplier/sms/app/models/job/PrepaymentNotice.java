package models.job;

import models.mail.MailMessage;
import models.mail.MailUtil;
import models.order.ECoupon;
import models.order.Prepayment;
import models.supplier.Supplier;
import play.Play;
import play.jobs.Job;
import play.jobs.On;

import java.math.BigDecimal;
import java.text.ParseException;
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
    private static String[] NOTIFICATION_EMAILS = Play.configuration.getProperty("prepayment_notification.email.receiver", "tangliqun@uhuila.com,sujie@uhuila.com").split(",");

    /**
     * 在可用预付款少于10%时给财务发邮件预警
     * <p/>
     * 邮件内容：[商户名]可用预付款(总额-已消费)已少于10%。[该商户预付款金额明细url]
     *
     * @throws ParseException
     */
    @Override
    public void doJob() throws ParseException {
        List<Supplier> suppliers = Supplier.findUnDeleted();
        for (Supplier supplier : suppliers) {
            List<Prepayment> prepayments = Prepayment.findNotExpiredBySupplier(supplier);
            for (Prepayment prepayment : prepayments) {
                BigDecimal consumedAmount = ECoupon.getConsumedAmount(prepayment);  //已消费
                BigDecimal availableBalance = prepayment.getAvailableBalance(consumedAmount);    //可用预付款
                if (availableBalance.compareTo(prepayment.amount.divide(BigDecimal.TEN)) < 0) {
                    sendNotification(prepayment, consumedAmount, availableBalance);
                    prepayment.warning = true;
                    prepayment.save();
                }
            }
        }
    }

    private static void sendNotification(Prepayment prepayment, BigDecimal consumedAmount, BigDecimal balance) {
        // 发邮件
        MailMessage message = new MailMessage();
        message.addRecipient(NOTIFICATION_EMAILS);
        message.setFrom("yibaiquan <noreplay@uhuila.com>");
        message.setSubject("可用预付款已少于百分之十");
        message.putParam("prepaymentId", prepayment.id);
        message.putParam("supplier", prepayment.supplier.otherName);
        message.putParam("effectiveAt", prepayment.effectiveAt);
        message.putParam("expireAt", prepayment.expireAt);
        message.putParam("amount", prepayment.amount);
        message.putParam("consumed", consumedAmount);
        message.putParam("balance", balance);
        message.setTemplate("prepayment");
        MailUtil.sendCommonMail(message);
    }
}
