package models.mail;

import org.apache.commons.mail.EmailAttachment;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

public class MailMessage implements Serializable {
    private static final long serialVersionUID = -8570949259782104651L;

    private List<String> recipients;
    private List<String> bccs;
    private List<String> ccs;
    private List<EmailAttachment> attachments;
    private String subject;
    private String content;
    private String from;

    private Map<String, Object> params;

    private String template;

    public MailMessage() {
        this.subject = "";
        this.content = "";
        this.from = "";
        this.recipients = new ArrayList<>();
        this.bccs = new ArrayList<>();
        this.ccs = new ArrayList<>();
        this.params = new HashMap<>();
        this.attachments = new ArrayList<>();
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

    public void addRecipient(String... recipient) {
        this.recipients.addAll(Arrays.asList(recipient));
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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<String> getBccs() {
        return bccs;
    }

    public void setBccs(List<String> bccs) {
        this.bccs = bccs;
    }

    @Transient
    @JsonIgnore
    public String getOneBcc() {
        return bccs.size() > 0 ? bccs.get(0) : null;
    }

    public void addBcc(String... bcc) {
        this.bccs.addAll(Arrays.asList(bcc));
    }

    public List<String> getCcs() {
        return ccs;
    }

    public void setCcs(List<String> ccs) {
        this.ccs = ccs;
    }

    @Transient
    @JsonIgnore
    public String getOneCc() {
        return ccs.size() > 0 ? ccs.get(0) : null;
    }

    public void addCc(String... ccs) {
        this.ccs.addAll(Arrays.asList(ccs));
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachments(EmailAttachment... attachments) {
        this.attachments.addAll(Arrays.asList(attachments));
    }
}