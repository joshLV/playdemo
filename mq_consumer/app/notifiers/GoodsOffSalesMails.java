package notifiers;

import models.mail.MailMessage;
import play.mvc.Mailer;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-30
 * Time: 下午2:44
 */
public class GoodsOffSalesMails extends Mailer {
    public static void notify(MailMessage message) {
        setSubject(message.getSubject());
        addRecipient(message.getOneRecipient());
        setFrom("yibaiquan <noreplay@uhuila.com>");
        send(message);
    }
}
