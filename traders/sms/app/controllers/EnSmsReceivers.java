package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.FieldCheckUtil;
import models.admin.SupplierUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.VerifyCouponType;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import play.mvc.Controller;

import java.util.Date;

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

        System.out.println("mobile=" + mobile + ",msg=" + msg + ",username="
                + username + ",pwd=" + pwd + ",dt=" + dt + ",code=" + code);

        // TODO: 检查dt和pwd
        if (!validUserPassDt(username, pwd, dt)) {
            renderText("Invaild request!");
        }

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

    private static boolean validUserPassDt(String username, String pwd, String dt) {
        return true;
    }

}
