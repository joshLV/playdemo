package controllers;

import play.Logger;
import play.mvc.Controller;

public class EnSmsReceivers extends Controller {

    /**
     * 消费者验证：mobiles=1391234567&msg=9527&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     * 店员验证：  mobiles=15900002342&msg=#xxxx#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     */
    public static void getSms() {
        String mobile = params.get("mobiles");
        String msg = params.get("msg");
        String username = params.get("username");
        String pwd = params.get("pwd");
        String dt = params.get("dt");
        String code = params.get("code");

        Logger.info("EnSmsReceiver: mobile=" + mobile + ",msg=" + msg + ",username="
                + username + ",pwd=" + pwd + ",dt=" + dt + ",code=" + code);

        // TODO: 检查dt和pwd
        if (!validUserPassDt(username, pwd, dt)) {
            renderText("Invaild request!");
        }

        String result = SmsReceiverUtil.processMessage(mobile, msg, code);
        renderText(result);
    }

    private static boolean validUserPassDt(String username, String pwd, String dt) {
        return true;
    }

}
