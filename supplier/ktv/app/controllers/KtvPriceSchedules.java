package controllers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomType;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * User: yan
 * Date: 13-4-12
 * Time: 下午2:09
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvPriceSchedules extends Controller {
    public static void index() {
        render();
    }

    public static void jsonSearch(Date startDay, Date endDay) {
        List<KtvPriceSchedule> schedules =
                KtvPriceSchedule.find("startDay <= ? and endDay >= ? or ( startDay <= ? and endDay >= ?  )",
                startDay, startDay, endDay, endDay).fetch();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(schedules));
    }

    public static void add() {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);

        List<KtvRoomType> roomTypeList = KtvRoomType.findRoomTypeList(supplier);
        render(shops, roomTypeList);
    }

    public static void create(@Valid KtvPriceSchedule priceSchedule, List<String> useWeekDays) {
        priceSchedule.useWeekDay = StringUtils.join(useWeekDays, ",");
        priceSchedule.save();
        index();
    }

    public static void edit(Long id) {
        render();
    }

    public static void update(Long id, KtvPriceSchedule priceSchedule) {
        render();
    }

    public static void delete(Long id) {
        index();

    }
}
