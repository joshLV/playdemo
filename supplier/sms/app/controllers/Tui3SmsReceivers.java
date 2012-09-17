package controllers;

import models.sms.SMSUtil;
import play.Logger;
import play.mvc.Controller;
import com.uhuila.common.util.FieldCheckUtil;

public class Tui3SmsReceivers extends Controller {
    /**
     * 验证： mobile=xxxx&content=xxxxx&msgid=xxxxx&xh=xxxxx
     */
    public static void getSms() {
        String mobile = params.get("mobile");
        String msg = params.get("content");
        String code = params.get("xh");
        
        if (code != null && code.length() > 4) {
            code = code.substring(code.length() - 4);
        }

        String result = SmsReceiverUtil.processMessage(mobile, msg, code);
        renderText(result);
    }
        
}
