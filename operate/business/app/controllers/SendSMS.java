package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.*;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
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


    public static void index(models.sales.SendSMSInfoCondition condition) {

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
//        System.out.println("pagepage"+pageNumber);
        if (condition == null) {
            condition = new SendSMSInfoCondition();
        }

        JPAExtPaginator<models.sales.SendSMSInfo> smsList = models.sales.SendSMSInfo.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        smsList.setBoundaryControlsEnabled(true);

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
                && SendSMSInfo.count("deleted=? and taskNo=? ", DeletedStatus.UN_DELETED,taskTempNo)!=0)
        {
            Validation.addError("taskTempNo", "validation.existed");
        }

        if (Validation.hasErrors()) {
            render("SendSMS/add.html", taskTempNo, tempMobile, tempECouponSn, tempText);
        }

        //用Pattern的split()方法把字符串按"/"分割
        Pattern p = Pattern.compile("[/]+");


        String[] mobileResult = p.split(tempMobile);
        String[] ECouponSnResult = p.split(tempECouponSn);

//        System.out.println("lengthlength"+mobileResult.length);

        int length = mobileResult.length >= ECouponSnResult.length ? ECouponSnResult.length : mobileResult.length;

//        System.out.println("mobilelength"+mobileResult.length );
//        System.out.println("ecouponlength"+ECouponSnResult.length );
        for (int i = 0; i < length; i++) {
            sms = new SendSMSInfo();

            String regular = "1[3,4,5,8]{1}\\d{9}";
            Pattern pattern = Pattern.compile(regular);
            sms.mobile = mobileResult[i];
            sms.eCouponSn = ECouponSnResult[i];
//            sms.mobile = sms.mobile.replace('-', ' ').replace(':', ' ').replaceAll(" ", "");
//            sms.eCouponSn = sms.eCouponSn.replace('-', ' ').replace(':', ' ').replaceAll(" ", "");
//           sms.mobile=deleteExtraSpace(sms.mobile);
//            sms.eCouponSn=deleteExtraSpace(sms.eCouponSn);
            sms.mobile = sms.mobile.replaceAll("\\s*", "");
            sms.eCouponSn = sms.eCouponSn.replaceAll("\\s*", "");

//
            System.out.println("****************");
            System.out.println();
            System.out.println("mobile" + sms.mobile);
//            System.out.println("hihihi");
            System.out.println("coupon" + sms.eCouponSn);
            Matcher m = p.matcher(sms.mobile);
//            System.out.println("resultresult" + m);
            //sms.mobile.length() != 11
            if (sms.mobile.length() == 11) {
//              System.out.println("mobilemobile"+deleteExtraSpace(sms.mobile));

                //删除开始和结尾处的空格，并将中间的多个连续的空格合并成一个
                System.out.println("4");
//                System.out.println("truein");
//                System.out.println("ecouponecoupon" + sms.eCouponSn);
                if (sms.eCouponSn.length() <= 5) {
//                    System.out.println("continuecontiune");
                    System.out.println("1");
                    continue;
                }
//                System.out.println("hihi");
//                System.out.println("mobilemobile"+sms.mobile);
                Matcher matcher = pattern.matcher(sms.mobile);
//                System.out.println("nonomobilemobile" + sms.mobile);
                if (!matcher.matches()) {
//                    System.out.println("mobilemobile" + sms.mobile);
                    System.out.println("2");
                    continue;
                }
                sms.taskNo = taskTempNo;

                // $coupon  ${coupon}
                sms.text = tempText.replaceAll("\\$\\{coupon\\}",sms.eCouponSn);

                sms.createdAt = new Date();
                sms.deleted = DeletedStatus.UN_DELETED;
                System.out.println("good");
                sms.save();
            } else {
                System.out.println("3");
                continue;
            }


        }


        send(sms);
    }

    public static void send(SendSMSInfo sms) {

        List<SendSMSInfo> smsList = SendSMSInfo.find("deleted=? and taskNo=? order by createdAt desc", DeletedStatus.UN_DELETED,sms.taskNo).fetch();

        render(smsList);
    }

    public static void sucSend(SendSMSInfo sms) {



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
