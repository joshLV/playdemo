package models.sms;

import models.order.OrderItemsFeeType;
import play.Play;
import util.mq.MQPublisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 短信对象.
 *
 * User: likang
 */
public class SMSMessage implements Serializable {
    private static final long serialVersionUID = -8170943259282104651L;
    private static final String SIGN = "【一百券】";

    // 短信MQ名称
    public static final String SMS_QUEUE = Play.mode.isProd() ? "send_sms" : "send_sms_dev";

    // 短信MQ名称，用于第二通道.
    public static final String SMS2_QUEUE = Play.mode.isProd() ? "send_sms2" : "send_sms2_dev";

    private String content;
    /**
     * 端口参数。
     * 比如通道为 10690091 分配号为 99 客户自己端口参数为 1028 那最终用户收到后显示的端口号为 10690091991028
     */
    private String code = "0000";
    private List<String> phoneNumbers;

    // 计费专用
    private Long orderItemsId;
    private OrderItemsFeeType feeType;

    public SMSMessage(String content, String phoneNumber, String code) {
        this.setContent(content);
        this.phoneNumbers = new ArrayList<>();
        this.code = code;
        this.phoneNumbers.add(phoneNumber);
    }

    public SMSMessage(String content, String phoneNumber) {
        this(content, phoneNumber, "0000");
    }

    public SMSMessage(String content, List<String> phoneNumbers) {
        this.setContent(content);
        this.phoneNumbers = phoneNumbers;
    }


    public SMSMessage(String content, String[] phoneNumbers) {
        this(content, Arrays.asList(phoneNumbers));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content != null && content.endsWith(SIGN)) {
            this.content = content;
            return;
        }
        this.content = content + SIGN;
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

    public Long getOrderItemsId() {
        return orderItemsId;
    }

    public void setOrderItemsId(Long orderItemsId) {
        this.orderItemsId = orderItemsId;
    }

    public void setFeeType(OrderItemsFeeType feeType) {
        this.feeType = feeType;
    }

    public OrderItemsFeeType getFeeType() {
        return feeType;
    }

    public SMSMessage orderItemsId(Long value) {
        this.orderItemsId = value;
        return this;
    }

    public SMSMessage feeType(OrderItemsFeeType feeType) {
        this.feeType = feeType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("SMSMessage [content=" + content + ",code=" + code + ",phones=");
        for (String phone : phoneNumbers) {
            str.append(phone).append(",");
        }
        str.append("]");
        return str.toString();
    }

    /**
     * 通过通道1发出短信.
     */
    public void send() {
        MQPublisher.publish(SMS_QUEUE, this);
    }

    /**
     * 通过通道2发出短信.
     */
    public void send2() {
        MQPublisher.publish(SMS2_QUEUE, this);
    }
}
