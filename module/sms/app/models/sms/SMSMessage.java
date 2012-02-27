package models.sms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: likang
 */
public class SMSMessage implements Serializable {
    private String content;
    private List<String> phoneNumbers;

    public SMSMessage(String content, String phoneNumber){
        this.content = content;
        this.phoneNumbers = new ArrayList<>();
        this.phoneNumbers.add(phoneNumber);
    }

    public SMSMessage(String content, List<String> phoneNumbers) {
        this.content = content;
        this.phoneNumbers = phoneNumbers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("SMSMessage [content=" + content + ",phones=");
        for(String phone : phoneNumbers){
            str.append(phone).append(",");
        }
        str.append("]");
        return str.toString();
    }
}
