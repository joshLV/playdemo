package models.sms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: likang
 */
public class SMSMessage implements Serializable {
    private String content;
    /**
     * 端口参数。
     * 比如通道为 10690091 分配号为 99 客户自己端口参数为 1028 那最终用户收到后显示的端口号为 10690091991028
     */
    private String code = "0000";
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("SMSMessage [content=" + content + ",code=" + code + ",phones=");
        for(String phone : phoneNumbers){
            str.append(phone).append(",");
        }
        str.append("]");
        return str.toString();
    }
}
