package models.payment;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;
import java.util.Map;

/**
 * @author likang
 * Date: 12-5-27
 */

@Entity
@Table(name = "payment_journal")
public class PaymentJournal extends Model {
    private static final String STEP_REQUEST= "form_request";
    private static final String STEP_NOTIFY = "back_notify";
    private static final String STEP_RETURN = "url_return";
    private static final String RESULT_OK = "success";
    private static final String RESULT_FAILED = "failed";

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "order_number")
    public String orderNumber;

    public String step;

    @Lob
    public String params;

    @Lob
    public String content;

    public String result;

    public PaymentJournal(){
        this.createdAt = new Date();
    }

    public static void savePayRequestJournal(String orderNumber, String description, String payAmount,
                                             String partner, String bankCode, String remoteAddress, String form){
        PaymentJournal paymentJournal = new PaymentJournal();
        paymentJournal.orderNumber = orderNumber;
        paymentJournal.step = STEP_REQUEST;
        paymentJournal.params = "orderNumber:" + orderNumber
                + ";description:" + description
                + ";amount:" + payAmount
                + ";partner:" + partner
                + ";bankCode:" + bankCode
                + ";remoteAddress:" + remoteAddress;
        paymentJournal.content = form;
        paymentJournal.result = RESULT_OK;
        paymentJournal.save();
    }

    public static void saveNotifyJournal(String orderNumber, Map<String, String[]> params, Map<String, String> result, boolean success){
        saveCallbackJournal(STEP_NOTIFY, orderNumber, params, result, success);
    }

    public static void saveUrlReturnJournal(String orderNumber, Map<String, String[]> params, Map<String, String> result, boolean success){
        saveCallbackJournal(STEP_RETURN, orderNumber, params, result, success);
    }

    private static void saveCallbackJournal(String step, String orderNumber, Map<String, String[]> params, Map<String, String> result, boolean success){
        PaymentJournal paymentJournal = new PaymentJournal();
        paymentJournal.orderNumber = orderNumber;
        paymentJournal.step = step;
        paymentJournal.params = join(params);
        paymentJournal.content = joinResult(result);
        paymentJournal.result = success ? RESULT_OK : RESULT_FAILED;
        paymentJournal.save();
    }

    public static String joinResult(Map<String, String> params){
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()){
            result.append(entry.getKey())
                    .append(":")
                    .append(entry.getValue())
                    .append(";");
        }
        return result.toString();
    }

    public static String join(Map<String, String[]> params){
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String[]> entry : params.entrySet()){
            StringBuilder value = new StringBuilder();
            for(String v : entry.getValue()){
                value.append(v).append("-");
            }
            result.append(entry.getKey())
                  .append(":")
                  .append(value)
                  .append(";");
        }
        return result.toString();
    }
}
