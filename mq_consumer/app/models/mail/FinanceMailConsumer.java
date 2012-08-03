package models.mail;

import play.jobs.OnApplicationStart;

/**
 * @author likang
 *         Date: 12-8-3
 */

@OnApplicationStart(async = true)
public class FinanceMailConsumer extends MailConsumer{
    @Override
    protected String queue() {
        return MailUtil.FINANCE_NOTIFICATION;
    }
}
