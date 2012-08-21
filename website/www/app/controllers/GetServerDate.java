package controllers;

import play.mvc.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-21
 * Time: 下午9:44
 */
public class GetServerDate extends Controller {
    public static void time() {
        SimpleDateFormat format = new SimpleDateFormat("MMM d,yyyy HH:mm:ss", Locale.ENGLISH);
        renderText(format.format(new Date()));
    }

}
