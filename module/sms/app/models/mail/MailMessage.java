package models.mail;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailMessage implements Serializable {
    private static final long serialVersionUID = -8570949259782104651L;

    private List<String> recipients;
    private String subject;
    private String content;
    private String from;

    private Map<String, Object> params;

    public MailMessage() {
        this.subject = "";
        this.content = "";
        this.from = "";
        this.recipients = new ArrayList<>();
        this.params = new HashMap<>();
    }
    public List<String> getRecipients() {
        return recipients;
    }

    @Transient
    @JsonIgnore
    public String getOneRecipient() {
        return recipients.size() > 0 ? recipients.get(0) : null;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public void addRecipient(String recipient) {
        this.recipients.add(recipient);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void putParam(String key, Object obj) {
        this.params.put(key, obj);
    }

    @Transient
    @JsonIgnore
    public Object getParam(String key) {
        return this.params.get(key);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}