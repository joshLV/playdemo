package models.logger;

import models.mail.MailMessage;
import models.mail.MailUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author likang
 *         Date: 12-7-27
 */
public class EmailAppender extends AppenderSkeleton{
    private String subject;
    private String receiver;
    private String from;

    @Override
    protected void append(LoggingEvent loggingEvent) {
        MailMessage message = new MailMessage();
        message.setSubject(subject);
        message.addRecipient(receiver);
        message.setFrom(from);
        message.setContent(loggingEvent.getMessage().toString());
        MailUtil.sendEmailLoggerMail(message);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
