package play.modules.emaillogs;

import java.io.Serializable;

/**
 * User: tanglq
 * Date: 13-2-21
 * Time: 下午2:45
 */
public class EmailLogMessage implements Serializable {

    private static final long serialVersionUID = 3169328103113062871L;

    public static final String MQ_KEY = "log.error.mail";

    public String applicationName;

    public String message;

    public String host;

    private EmailLogMessage() {

    }

    public static EmailLogMessage build() {
        return new EmailLogMessage();
    }

    public EmailLogMessage applicationName(String value) {
        this.applicationName = value;
        return this;
    }

    public EmailLogMessage message(String value) {
        this.message = value;
        return this;
    }

    public EmailLogMessage host(String value) {
        this.host = value;
        return this;
    }

}
