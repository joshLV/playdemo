package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPriceSchedule;
import models.sales.Shop;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

/**
 * @author likang
 *         Date: 13-4-19
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvDailySchedule extends Controller {

    public static void index(Long shopId){
        List<Shop> shops = SupplierRbac.currentUser().supplier.getShops();
        if (shopId == null && shops.size() > 0) {
            shopId = shops.get(0).id;
        }

        render(shops, shopId);
    }

    public static void jsonRoom(Long shopId, Date day) {
        Map<String, Object> jsonParams = KtvPriceSchedule.dailyScheduleOverview(shopId, day);
        if (jsonParams == null) {
            error("shop not found");
        }

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(jsonParams));
    }

}
