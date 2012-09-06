package controllers;

import models.sales.SendSMSInfo;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-6
 * Time: 上午11:16
 * To change this template use File | Settings | File Templates.
 */

@With(OperateRbac.class)
@ActiveNavigation("send_sms")
public class SendSMS extends Controller {

    /**
     * 任务号
     */
    public String taskTempNo;

    /**
     * 手机
     */
    public String tempMobile;

    /**
     * 券号
     */

    public String tempECouponSn;

    /**
     * 短信内容
     */
//    @Lob
    public String tempText;


    public static void index() {
        render();
    }

    public static void add() {
        render();
    }


    public static void create(SendSMSInfo sms, String taskTempNo, String tempMobile, String tempECouponSn, String tempText) {

        if (StringUtils.isBlank(taskTempNo))
            Validation.addError("taskTempNo", "validation.required");

        if (StringUtils.isBlank(tempMobile))
            Validation.addError("tempMobile", "validation.required");

        if (StringUtils.isBlank(tempECouponSn))
            Validation.addError("tempECouponSn", "validation.required");

        if (StringUtils.isBlank(tempText))
            Validation.addError("tempText", "validation.required");


        if (Validation.hasErrors()) {

            render("SendSMS/add.html", sms, taskTempNo, tempMobile, tempECouponSn, tempText);
        }

        //用Pattern的split()方法把字符串按"/"分割
        Pattern p = Pattern.compile("[/]+");


        String[] mobileResult = p.split(tempMobile);
        String[] ECouponSnResult = p.split(tempECouponSn);

        int length=mobileResult.length>=ECouponSnResult.length?ECouponSnResult.length:mobileResult.length;

        for (int i = 0; i < length; i++)
        {
            sms.mobile= mobileResult[i];
            sms.eCouponSn=ECouponSnResult[i];
            sms.text=tempText;

            sms.create();
            sms.save();
        }



        send(sms);
    }

    public static void send(SendSMSInfo sms) {
        render();
    }

}
