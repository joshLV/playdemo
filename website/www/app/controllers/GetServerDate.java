package controllers;

import play.mvc.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用于秒杀，得到当前服务器时间。
 */
public class GetServerDate extends Controller {
    public static void time() {
        SimpleDateFormat format = new SimpleDateFormat("MMM d,yyyy HH:mm:ss", Locale.ENGLISH);
        renderText(format.format(new Date()));
    }

}
