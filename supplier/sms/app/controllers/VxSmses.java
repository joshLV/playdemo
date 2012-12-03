package controllers;

import java.util.Date;

import models.sms.VxSms;

import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;

public class VxSmses extends Controller {

    public static void send(String mobiles, String content, String smsType) {
        if (StringUtils.isBlank(mobiles) || StringUtils.isBlank(content)) {
            renderText("1|内容为空");
        }
        String[] aryMobiles = mobiles.split(",");
        for (String mobile : aryMobiles) {
            VxSms vxSms = new VxSms();
            vxSms.mobile = mobile;
            vxSms.message = content;
            vxSms.smsType = smsType;
            vxSms.createdAt = new Date();
            vxSms.save();
        }
        renderText("0|发送成功");
    }
}
