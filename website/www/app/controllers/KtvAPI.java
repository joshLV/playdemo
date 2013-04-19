package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.ktv.KtvPriceSchedule;
import play.mvc.Controller;

import java.util.*;

/**
 * User: yan
 * Date: 13-4-17
 * Time: 下午8:35
 */
public class KtvAPI extends Controller {

    public static void jsonRoom(Long shopId, Date day) {
        Map<String, Object> jsonParams = KtvPriceSchedule.dailyScheduleOverview(shopId, day);
        if (jsonParams == null) {
            error("shop not found");
        }

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(jsonParams));
    }

}
