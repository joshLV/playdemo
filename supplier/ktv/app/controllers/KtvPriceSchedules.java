package controllers;

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
        renderJSON("[\n" +
                "    {\n" +
                "        \"startDay\":\"2013-04-11\"," +
                "        \"endDay\":\"2013-04-16\",\n" +
                "        \"weekday\":\"1,3,4,5\",\n" +
                "        \"startTime\":\"10:00\",\n" +
                "        \"endTime\":\"12:00\",\n" +
                "        \"price\":\"30.00\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"startDay\":\"2013-04-08\",\n" +
                "        \"endDay\":\"2013-04-16\",\n" +
                "        \"weekday\":\"1,2,3,5\",\n" +
                "        \"startTime\":\"14:00\",\n" +
                "        \"endTime\":\"16:00\",\n" +
                "        \"price\":\"50.00\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"startDay\":\"2013-04-10\",\n" +
                "        \"endDay\":\"2013-04-11\",\n" +
                "        \"weekday\":\"1,2,3,4,5\",\n" +
                "        \"startTime\":\"18:00\",\n" +
                "        \"endTime\":\"22:00\",\n" +
                "        \"price\":\"60.00\"\n" +
                "    }\n" +
                "]");

    }

    public static void add() {
        initParams();
        render();
    }

    private static void initParams() {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);

        List<KtvRoomType> roomTypeList = KtvRoomType.findRoomTypeList(supplier);
        renderArgs.put("shops", shops);
        renderArgs.put("roomTypeList", roomTypeList);
    }

    public static void create(@Valid KtvPriceSchedule priceSchedule, List<String> useWeekDays) {
        priceSchedule.useWeekDay = StringUtils.join(useWeekDays, ",");
        priceSchedule.createdAt = new Date();
        priceSchedule.save();
        index();
    }

    public static void edit(Long id) {
        initParams();
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        if (priceSchedule==null){
            error("没有该时间段的价格信息！请确认!");
            return;
        }
        String shopIds = ",";
        if (priceSchedule.shops != null && priceSchedule.shops.size() > 0) {
            for (Shop shop : priceSchedule.shops) {
                shopIds += shop.id + ",";
            }
        }
        render(priceSchedule, shopIds);
    }

    public static void update(Long id, KtvPriceSchedule priceSchedule) {
        KtvPriceSchedule.update(id,priceSchedule);
        index();
    }

    public static void delete(Long id) {
        index();

    }
}
