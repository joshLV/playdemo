package models.payment.tenpay;

import com.tenpay.api.common.CommonResponse;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author likang
 * Date: 12-5-23
 */
public class TenpayResponse extends CommonResponse{
    private static final long serialVersionUID = 2376278303233667473L;
    private static String NOTIFY_ID_KEY = "notify_id";
    private String notifyId;

    public TenpayResponse(Map<String, String> params, String secretKey){
        super(params, secretKey, false);
        this.notifyId = getParameter(NOTIFY_ID_KEY);

    }

    public String getNotifyId(){
        return this.notifyId;
    }
}
