package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.order.GiftCard;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Controller;

import java.util.Date;

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

        GiftCard giftCard = GiftCard.find("byPasswordAndDisabled", password, DeletedStatus.UN_DELETED).first();
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

        String t = Codec.UUID();
        Cache.add(t, giftCard.id, "30mn");

        Goods goods = giftCard.goods;

        render("WebsiteGiftCards/appointment.html", t, goods);
    }

    public static void appointment(String username, String mobile, String address, String postcode,
                                   Date date, String message, String t) {
        Long gid = (Long)Cache.get(t);
        GiftCard giftCard = GiftCard.findById(gid);

    }

    public static void showAppointment() {
        render("WebsiteGiftCards/appointment.html");
    }
}
