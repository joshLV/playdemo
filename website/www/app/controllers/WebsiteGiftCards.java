package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.uhuila.common.constants.DeletedStatus;
import models.order.GiftCard;
import models.sales.Goods;
import models.sales.ImportedCouponStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Controller;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-7-11
 */
public class WebsiteGiftCards extends Controller {

    public static void index() {
        String randomID = Register.genRandomId();
        render(randomID);
    }

    public static void login(String password, String code, String randomID) {
        String err = null;
        if (StringUtils.isBlank(password)) {
            err = "请填写券密码";
        }
        if (StringUtils.isBlank(code)) {
            err = "请填写验证码";
        }

        GiftCard giftCard = GiftCard.find("byPasswordAndDisabled", password.toUpperCase(), DeletedStatus.UN_DELETED).first();
        if (giftCard == null) {
            err = "对不起，券号无效";
        }
        randomID = StringUtils.trimToEmpty(randomID);

        if (!code.equalsIgnoreCase((String)Cache.get(StringUtils.trimToEmpty(randomID)))) {
            err = "验证码错误,请重新输入";
        }

        if (err != null ) {
            randomID = Register.genRandomId();
            render("WebsiteGiftCards/index.html", err, password, randomID);
        }

        if (giftCard.status == ImportedCouponStatus.UNUSED) {
            String t = Codec.UUID();
            Cache.add(t, giftCard.id, "30mn");

            Goods goods = giftCard.goods;

            render("WebsiteGiftCards/appointment.html", t, goods);
        }else {
            render("WebsiteGiftCards/view.html", giftCard);
        }
    }

    public static void appointment(String username, String mobile, String address, String postcode,
                                   Date date, String message, String t) {
        Long gid = (Long)Cache.get(t);
        GiftCard giftCard = GiftCard.findById(gid);
        if (giftCard == null) {
            index();
        }
        Goods goods = giftCard.goods;
        if (giftCard.status != ImportedCouponStatus.UNUSED) {
            String err = "无效的券";
            render("WebsiteGiftCards/appointment.html", err, t, goods, username, mobile, address, postcode, date, message);
        }
        if (StringUtils.isBlank(username) || StringUtils.isBlank(mobile) || StringUtils.isBlank(address)
                || StringUtils.isBlank(postcode)) {
            String err = "请填写所有必要的信息";
            render("WebsiteGiftCards/appointment.html", err, t, goods, username, mobile, address, postcode, date, message);
        }
        if (date.before(DateUtils.ceiling(new Date(), Calendar.DATE))) {
            String err = "至少预约时间为明天";
            render("WebsiteGiftCards/appointment.html", err, t, goods, username, mobile, address, postcode, date, message);
        }
        Map<String, String> userInputs = params.allSimple();
        userInputs.remove("body");
        userInputs.remove("t");
        userInputs.remove("goods.id");
        giftCard.status = ImportedCouponStatus.USED;
        giftCard.userInput = new Gson().toJson(userInputs);
        giftCard.appliedAt = new Date();
        giftCard.save();

        render("WebsiteGiftCards/view.html", giftCard);

    }

    public static void showAppointment() {
        render("WebsiteGiftCards/appointment.html");
    }
}
