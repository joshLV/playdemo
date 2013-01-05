package controllers;

import play.mvc.Controller;

/**
 * 领时网络短信接收接口。
 *
 * 联系人：朱伟琴.
 */
public class LingShiSmsReceivers extends Controller {

    public static void getSms() {
        String mobile = params.get("SrcMobile");
        String msg = params.get("Content");
        String code = params.get("AppendID");

        // 这家返回的code是完整的号码，需要另外处理
        if (code != null && code.length() > 4) {
            code = code.substring(code.length() - 4);
        }

        String result = SmsReceiverUtil.processMessage(mobile, msg, code);
        renderText(result);
    }

}
