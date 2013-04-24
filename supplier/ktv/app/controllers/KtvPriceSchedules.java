package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomType;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: yan
 * Date: 13-4-12
 * Time: 下午2:09
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvPriceSchedules extends Controller {
    public static void index(Shop shop, KtvRoomType roomType) {
        initParams(null);
        render(shop, roomType);
    }

    public static void jsonSearch(Date startDay, Date endDay, Shop shop, KtvRoomType roomType) {
        Logger.info("startDay=" + startDay + ", endDay=" + endDay + ", shopId=" + shop.id + ", roomType=" + roomType);
        if (startDay.after(endDay)) {
            error();
        }

        List<KtvPriceSchedule> schedules = KtvPriceSchedule.getKtvPriceSchedules(startDay, endDay, shop, roomType);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("yyyy-MM-dd").create();
        renderJSON(gson.toJson(schedules));
    }


    public static void add() {
        initParams(null);
        System.out.println("-------");
        render();
    }

    private static void initParams(KtvPriceSchedule priceSchedule) {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);

        List<KtvRoomType> roomTypeList = KtvRoomType.findRoomTypeList(supplier);
        String shopIds = setShopIds(priceSchedule);
        renderArgs.put("shopIds", shopIds);
        renderArgs.put("shops", shops);
        renderArgs.put("roomTypeList", roomTypeList);
    }

    public static void create(@Valid KtvPriceSchedule priceSchedule, List<String> useWeekDays) {
        priceSchedule.useWeekDay = StringUtils.join(useWeekDays, ",");
        checkTime(null, priceSchedule);
        if (Validation.hasErrors()) {
            initParams(priceSchedule);
            render("/KtvPriceSchedules/add.html", priceSchedule);
        }
        priceSchedule.createdAt = new Date();
        priceSchedule.deleted = DeletedStatus.UN_DELETED;
        priceSchedule.save();
        index(null, null);
    }


    private static void checkTime(Long id, KtvPriceSchedule priceSchedule) {
        Set<Shop> shops = priceSchedule.shops;
        Validation.required("priceSchedule.shop", shops);
        if (shops != null && shops.size() > 0) {

            List<KtvPriceSchedule> scheduleList = KtvPriceSchedule.getSchedules(id, priceSchedule);
            for (KtvPriceSchedule schedule : scheduleList) {
//                if (!schedule.useWeekDay.contains(priceSchedule.useWeekDay)) {
//                    continue;
//                }

                for (Shop shop : shops) {
                    for (Shop existedShop : schedule.shops) {
                        if (!shop.id.equals(existedShop.id)) {
                            continue;
                        }
                        if (priceSchedule.startDay.after(schedule.startDay) && priceSchedule.endDay.before(schedule.endDay)) {
                            Validation.addError("priceSchedule.day", "该日期范围有交叉，请确认！");
                            break;
                        }

                        //10：00~12：00  交叉的可能时间段09:00~11:00 或 11：00~13：00
                        if (!(priceSchedule.endTime.compareTo(schedule.startTime) <= 0 || priceSchedule.startTime.compareTo(schedule.endTime) >= 0)) {
                            Validation.addError("priceSchedule.useTime", "该时间段有交叉，请确认！");
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void edit(Long id) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        if (priceSchedule == null) {
            error("没有该时间段的价格信息！请确认!");
            return;
        }
        initParams(priceSchedule);
        render(priceSchedule);
    }

    private static String setShopIds(KtvPriceSchedule priceSchedule) {
        if (priceSchedule == null) {
            return "";
        }
        String shopIds = ",";
        if (priceSchedule.shops != null && priceSchedule.shops.size() > 0) {
            for (Shop shop : priceSchedule.shops) {
                shopIds += shop.id + ",";
            }
        }
        return shopIds;
    }

    public static void update(Long id, @Valid KtvPriceSchedule priceSchedule, List<String> useWeekDays) {
        priceSchedule.useWeekDay = StringUtils.join(useWeekDays, ",");
        checkTime(id, priceSchedule);
        if (Validation.hasErrors()) {
            initParams(priceSchedule);
            System.out.println(Validation.errors().get(0));
            render("KtvPriceSchedules/edit.html", priceSchedule);
        }
        KtvPriceSchedule.update(id, priceSchedule);
        redirect("/ktv/price-schedule");
    }

    public static void delete(Long id) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        if (priceSchedule == null) {
            index(null, null);
            return;
        }
        priceSchedule.delete();

        index(null, null);
    }
}
