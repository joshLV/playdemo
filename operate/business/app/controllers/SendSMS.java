package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.*;
import models.sms.SMSUtil;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
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
    public static int PAGE_SIZE = 15;

    public static void index(SendSMSTaskCondition condition) {

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new SendSMSTaskCondition();
        }

        JPAExtPaginator<models.sales.SendSMSTask> smsTaskList = SendSMSTask.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        smsTaskList.setBoundaryControlsEnabled(true);

        render(smsTaskList);
    }


    public static void details(String taskNo) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        SendSMSInfoCondition condition = new SendSMSInfoCondition();
        condition.taskNo = taskNo;

        JPAExtPaginator<models.sales.SendSMSInfo> smsList = SendSMSInfo.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        render(smsList);
    }

    public static void add() {
        render();
    }

    public static void create(String taskTempNo, String tempMobile, String tempECouponSn, String tempText) {
        SendSMSInfo sms = new SendSMSInfo();

        if (StringUtils.isBlank(taskTempNo))
            Validation.addError("taskTempNo", "validation.required");

        if (StringUtils.isBlank(tempMobile))
            Validation.addError("tempMobile", "validation.required");

        if (StringUtils.isBlank(tempECouponSn))
            Validation.addError("tempECouponSn", "validation.required");

        if (StringUtils.isBlank(tempText))
            Validation.addError("tempText", "validation.required");

        if (!StringUtils.isBlank(taskTempNo)
                && SendSMSInfo.count("deleted=? and taskNo=? ", DeletedStatus.UN_DELETED, taskTempNo) != 0) {
            Validation.addError("taskTempNo", "validation.existed");
        }

        if (Validation.hasErrors()) {
            render("SendSMS/add.html", taskTempNo, tempMobile, tempECouponSn, tempText);
        }


        Pattern p = Pattern.compile("[,\\s]+");


        String[] mobileResult = p.split(tempMobile);
        String[] ECouponSnResult = p.split(tempECouponSn);

        int length = mobileResult.length >= ECouponSnResult.length ? ECouponSnResult.length : mobileResult.length;

        SendSMSTask smsTask = new SendSMSTask();
        smsTask.taskNo = taskTempNo;

        for (int i = 0; i < length; i++) {
            sms = new SendSMSInfo();

            String regular = "1[3,4,5,8]{1}\\d{9}";
            Pattern pattern = Pattern.compile(regular);
            sms.mobile = mobileResult[i];
            sms.eCouponSn = ECouponSnResult[i];

            sms.mobile = sms.mobile.replaceAll("\\s*", "");
            sms.eCouponSn = sms.eCouponSn.replaceAll("\\s*", "");

            Matcher m = p.matcher(sms.mobile);

            if (sms.mobile.length() == 11) {


                if (sms.eCouponSn.length() <= 5) {

                    continue;
                }

                Matcher matcher = pattern.matcher(sms.mobile);

                if (!matcher.matches()) {

                    continue;
                }
                sms.taskNo = taskTempNo;

                sms.text = tempText.replaceAll("\\$\\{coupon\\}", sms.eCouponSn);

                sms.createdAt = new Date();
                sms.deleted = DeletedStatus.UN_DELETED;

                sms.save();


            } else {

                continue;
            }


        }

        List<SendSMSInfo> smsList = SendSMSInfo.find("deleted=? and taskNo=?", DeletedStatus.UN_DELETED, taskTempNo).fetch();
        smsTask.unfinished = (long) smsList.size();
        smsTask.finished = 0L;

        smsTask.total = smsTask.unfinished + smsTask.finished;
        smsTask.deleted = DeletedStatus.UN_DELETED;
        smsTask.createdAt = new Date();
        smsTask.save();

        send(taskTempNo);
    }

    public static void send(String taskTempNo) {

        List<SendSMSInfo> smsList = SendSMSInfo.find("deleted=? and taskNo=? order by createdAt desc", DeletedStatus.UN_DELETED, taskTempNo).fetch();

        render(smsList, taskTempNo);
    }


    public static void sucSend(final String taskTempNo, String scheduledTime, String timer) throws ParseException {
        SendSMSTask smsTask = SendSMSTask.find("deleted=? and taskNo=? ", DeletedStatus.UN_DELETED, taskTempNo).first();

        //timer equals "0"
        if (timer.indexOf("0") != -1) {
            System.out.println("instantly");
            Date now = new Date();

            smsTask.scheduledTime = new Date(now.getTime() + 1000);
            System.out.println("dfasdfasdfasdf"+  smsTask.scheduledTime);
            smsTask.save();

        }
        if (timer.indexOf("1") != -1) {
            if (!StringUtils.isBlank(scheduledTime)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                smsTask.scheduledTime =sdf.parse(scheduledTime);
                smsTask.save();
            }
        }

        index(null);

    }

    //删除开始和结尾处的空格
    public static String deleteExtraSpace(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0 || str.equals(" ")) {
            return new String();
        }
        char[] oldStr = str.toCharArray();
        int len = str.length();
        char[] tmpStr = new char[len];
        boolean keepSpace = false;
        int j = 0;//the index of new string
        for (int i = 0; i < len; i++) {
            char tmpChar = oldStr[i];
            if (oldStr[i] != ' ') {
                tmpStr[j++] = tmpChar;
                keepSpace = true;
            } else if (keepSpace) {
                tmpStr[j++] = tmpChar;
                keepSpace = false;
            }
        }

        //unlike c/c++,no "\0" at the end of a string. So,do the copy again...
        int newLen = j;
        if (tmpStr[j - 1] == ' ') {
            newLen--;
        }
        char[] newStr = new char[newLen];
        for (int i = 0; i < newLen; i++) {
            newStr[i] = tmpStr[i];
        }
        return new String(newStr);
    }

}
