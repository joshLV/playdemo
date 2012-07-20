package controllers;

import models.sms.SMSUtil;
import play.mvc.Controller;
import com.uhuila.common.util.FieldCheckUtil;

public class LingShiSmsReceivers extends Controller {
    /**
     * 消费者验证：mobiles=1391234567&msg=9527&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     * 店员验证：  mobiles=15900002342&msg=#xxxx#&username=wang&pwd=5a1a023fd486e2f0edbc595854c0d808&dt=1319873904&code=1028
     */
    public static void getSms() {
        String mobile = params.get("SrcMobile");
        String msg = params.get("Content");
        String code = params.get("AppendID");

        if (msg.contains("#")) {
            // 店员验证
            renderText(SmsReceiverUtil.checkClerk(mobile, msg, code));
        } else if (FieldCheckUtil.isNumeric(msg)) {
            // 消费者验证的情况
            renderText(SmsReceiverUtil.checkConsumer(mobile, msg, code));
        } else {
            SMSUtil.send("【券市场】券号格式错误，单个发送\"#券号\"，多个发送\"#券号#券号\"，如有疑问请致电：400-6262-166",
                    mobile, code);
            renderText("msg is wrong");
        }
    }
}
