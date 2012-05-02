package notifiers;

import models.mail.CouponMessage;
import play.mvc.Mailer;

public class FindPassWordMails extends Mailer {

    public static void notifyPassWord(CouponMessage message) {

        setSubject("[优惠啦] 找回密码");
        addRecipient(message.getEmail());
        setFrom("Uhuila <noreplay@uhuila.com>");
        send(message);
    }

}
