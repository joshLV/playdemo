package models.mail;

import play.jobs.OnApplicationStart;

/**
 * @author likang
 *         Date: 12-7-20
 *         Time: 下午5:10
 */
@OnApplicationStart(async = true)
public class OperatorNotificationConsumer extends MailConsumer {
    @Override
    protected String queue() {
        return MailUtil.OPERATOR_NOTIFICATION;
    }
}
