package notifiers;

import models.mail.MailMessage;
import play.mvc.Mailer;

public class FindPassWordMails extends Mailer {

    public static void notify(MailMessage message) {
    	System.out.println("aaaaaaaaaaaaaaaaaaaaa");
        setSubject("[优惠啦] 找回密码");
        addRecipient(message.getEmail());
        setFrom("Uhuila <noreplay@uhuila.com>");
        send(message);
    }

}
