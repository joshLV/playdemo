package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemSkuAddRequest;
import com.taobao.api.response.ItemSkuAddResponse;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.accounts.AccountType;
import models.ktv.KtvPriceSchedule;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.resale.Resaler;
import models.sales.Shop;
import models.supplier.Supplier;
import models.taobao.TaobaoKtvUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
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
        initParams(null, shop, roomType);
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
        TaobaoKtvUtil.addGoodsSku(null, SupplierRbac.currentUser());
        initParams(null, null, null);
        render();
    }

    private static void initParams(KtvPriceSchedule priceSchedule, Shop shop, KtvRoomType roomType) {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);
        List<KtvRoomType> roomTypeList = KtvRoomType.findRoomTypeList(supplier);
        //初始页面，设置门店和包厢类型，并根据门店和包厢查询该门店是否设置包厢数量
        if ((shop == null || shop.id == null) && shops.size() > 0) {
            shop = shops.get(0);
        }
        if ((roomType == null || roomType.id == null) && roomTypeList.size() > 0) {
            roomType = roomTypeList.get(0);
        }
        List<KtvRoom> ktvRoomList = KtvRoom.findKtvRoom(roomType, shop);
        String shopIds = setShopIds(priceSchedule);
        renderArgs.put("ktvRoomList", ktvRoomList);
        renderArgs.put("shopIds", shopIds);
        renderArgs.put("shops", shops);
        renderArgs.put("roomTypeList", roomTypeList);
    }

    public static void create(@Valid KtvPriceSchedule priceSchedule, List<String> useWeekDays) {
        Supplier supplier = SupplierRbac.currentUser().supplier;
        priceSchedule.useWeekDay = StringUtils.join(useWeekDays, ",");
        checkTime(null, priceSchedule);
        if (Validation.hasErrors()) {
            initParams(priceSchedule, null, null);
            render("/KtvPriceSchedules/add.html", priceSchedule);
        }
        priceSchedule.createdAt = new Date();
        priceSchedule.deleted = DeletedStatus.UN_DELETED;
        priceSchedule.save();


        index(null, null);
    }

    /**
     * 页面验证
     */
    private static void checkTime(Long id, KtvPriceSchedule priceSchedule) {
        Set<Shop> shops = priceSchedule.shops;
        Validation.required("priceSchedule.shop", shops);
        if (shops != null && shops.size() > 0) {
            List<KtvPriceSchedule> scheduleList = KtvPriceSchedule.getSchedules(id, priceSchedule);
            for (KtvPriceSchedule schedule : scheduleList) {
                for (Shop shop : shops) {
                    //检查门店是否设置相应包厢类型数量
                    checkShopRoomNumber(schedule, shop);
                    //检查是否有交叉时间
                    checkShopTime(priceSchedule, schedule, shop);
                }
            }
        }
    }

    /**
     * 检查门店是否设置相应包厢类型数量
     */
    private static void checkShopRoomNumber(KtvPriceSchedule priceSchedule, Shop shop) {
        List<KtvRoom> rooms = KtvRoom.findKtvRoom(priceSchedule.roomType, shop);
        if (rooms.size() == 0) {
            Validation.addError("priceSchedule.shop", "【" + shop.name + "】没有添加该包厢类型的数量！");
        }
    }


    /**
     * 检查是否有交叉时间
     */
    private static void checkShopTime(KtvPriceSchedule priceSchedule, KtvPriceSchedule schedule, Shop shop) {
        for (Shop existedShop : schedule.shops) {
            if (!shop.id.equals(existedShop.id)) {
                continue;
            }
            //10：00~12：00  交叉的可能时间段09:00~11:00 或 11：00~13：00
            if (!(priceSchedule.endTime.compareTo(schedule.startTime) <= 0 || priceSchedule.startTime.compareTo(schedule.endTime) >= 0)) {
                Validation.addError("priceSchedule.useTime", "该时间段有交叉，请确认！");
                break;
            }
        }
    }

    public static void edit(Long id) {
        KtvPriceSchedule priceSchedule = KtvPriceSchedule.findById(id);
        TaobaoKtvUtil.updateSku(priceSchedule, SupplierRbac.currentUser());
        if (priceSchedule == null) {
            error("没有该时间段的价格信息！请确认!");
            return;
        }
        initParams(priceSchedule, null, null);
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
            initParams(priceSchedule, null, null);
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
