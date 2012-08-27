package controllers;

import play.mvc.Controller;

public class LingShiSmsReceivers extends Controller {
    /**
     * 消费者验证：mobiles=1391234567&msg=9527&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     * 店员验证：  mobiles=15900002342&msg=#xxxx#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     */
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
