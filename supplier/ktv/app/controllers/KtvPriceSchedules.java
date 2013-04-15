package controllers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoomType;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;
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
        initParams(null);
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
        checkTime(priceSchedule);
        if (Validation.hasErrors()) {
            initParams(priceSchedule);
            render("/KtvPriceSchedules/add.html", priceSchedule);
        }
        priceSchedule.createdAt = new Date();
        priceSchedule.deleted = DeletedStatus.UN_DELETED;
        priceSchedule.save();
        index();
    }

    private static void checkTime(KtvPriceSchedule priceSchedule) {
        Set<Shop> shops = priceSchedule.shops;

        Validation.required("priceSchedule.shop", shops);
        if (shops != null && shops.size()>0) {
            List<KtvPriceSchedule> scheduleList = KtvPriceSchedule.find("roomType=?", priceSchedule.roomType).fetch();
            for (KtvPriceSchedule schedule : scheduleList) {
                for (Shop shop : shops) {
                    for (Shop existedShop : schedule.shops) {
                        if (!shop.id.equals(existedShop.id)) {
                            continue;
                        }
                        if (!schedule.useWeekDay.contains(priceSchedule.useWeekDay)) {
                            continue;
                        }
                        if (priceSchedule.startDay.after(schedule.startDay) && priceSchedule.endDay.before(schedule.endDay)) {
                            Validation.addError("priceSchedule.day", "该日期范围有交叉，请确认！");
                            break;
                        }
                        if ((priceSchedule.startTime.compareTo(schedule.startTime) >= 0 && priceSchedule.startTime.compareTo(schedule.endTime) <= 0) || priceSchedule.startTime.compareTo(schedule.endTime) <= 0) {
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
        checkTime(priceSchedule);
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
            index();
            return;
        }
        priceSchedule.delete();

    }
}
