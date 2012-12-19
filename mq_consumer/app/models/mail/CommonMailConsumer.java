package models.mail;

import play.jobs.OnApplicationStart;

/**
 * 一般情况下只要这个MailConsumer就可以了，MailMessage中注明模板即可，不用写那么多Consumer
 * 如果希望以不同的QUEUE发送邮件，那么自己再写个跟这个类差不多的就行了
 *
 * @author likang
 *         Date: 12-12-19
 */
@OnApplicationStart(async = true)
public class CommonMailConsumer extends MailConsumer{
    @Override
    protected String queue() {
        return MailUtil.COMMON_QUEUE;
    }
}

