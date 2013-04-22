package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvPromotion;
import models.ktv.KtvPromotionConfig;
import models.ktv.KtvRoomType;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * ktv促销活动管理
 * <p/>
 * User: wangjia
 * Date: 13-4-18
 * Time: 上午10:36
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvPromotions extends Controller {
    public static void index() {
        render();
    }

    public static void add() {
        KtvPromotion promotion = new KtvPromotion();
        promotion.create();
        promotion.deleted = DeletedStatus.DELETED;
        promotion.save();
        Long promotionId = promotion.id;
        List<KtvPromotionConfig> itemList =
                KtvPromotionConfig.find("promotion.id=? and deleted = ? ", promotionId, DeletedStatus.UN_DELETED).fetch();
        initParams(null);
        render(promotionId,itemList);
    }

    public static void create(@Valid KtvPromotion promotion, KtvPromotionConfig promotionConfigs) {
        System.out.println(promotion.shops + "《=========promotion.shops:");
//        System.out.println(promotion.startTime + "《=========promotion.startTime:");
//        System.out.println(promotion.endTime + "《=========promotion.endTime:");
//        System.out.println(promotionConfigs.reducedPrice + "《=========promotion.promotionConfigs.reducedPrice:");
        //判断是否选择了适用包厢
        Set<KtvRoomType> roomTypes = promotion.roomTypes;
        Validation.required("promotion.roomTypes", roomTypes);
//        System.out.println(promotion.promotionType + "《=========salesPromotion.promotionType:");

        Long promotionId=promotion.id;
        //适用时段
        if (promotion.startTime.compareTo(promotion.endTime) > 0) {
            Validation.addError("promotion.startTime", "validation.beforeThanEndTime");
        }
        if (Validation.hasErrors()) {
            initParams(promotion);
            render("/KtvPromotions/add.html", promotion,promotionId);
        }

//        System.out.println(promotion.roomTypes + "《=========promotion.roomTypes:");
//        System.out.println(promotion.name + "《=========salesPromotion.name:");
//        System.out.println(promotion.endDay + "《=========salesPromotion.endDay:");
//        System.out.println(promotion.endTime + "《=========salesPromotion.endTime:");
//        System.out.println(promotion.startDay + "《=========salesPromotion.startDay:");
//        System.out.println(promotion.startTime + "《=========salesPromotion.startTime:");
        promotion.createdAt = new Date();
        promotion.deleted = DeletedStatus.UN_DELETED;
//        promotion.save();
        index();
    }

    private static void initParams(KtvPromotion promotion) {
        //初始化促销类型
        List<String> promotionTypeList = new LinkedList<>();
        promotionTypeList.add("CONTINUOUS_RESERVE_DISCOUNT");
        promotionTypeList.add("CONTINUOUS_RESERVE_REDUCTION");
        promotionTypeList.add("ADVANCED_RESERVE_DISCOUNT");
        promotionTypeList.add("ADVANCED_RESERVE_REDUCTION");
        renderArgs.put("promotionTypeList", promotionTypeList);

        //初始化适用门店
        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shopsList = Shop.findShopBySupplier(supplier.id);

        String shopIds = setShopIds(promotion);
//        System.out.println(shopIds + "《=========shopIds:");
        renderArgs.put("shopIds", shopIds);

        //初始化适用包厢
        List<KtvRoomType> roomsList = KtvRoomType.findRoomTypeList(supplier);
        renderArgs.put("roomsList", roomsList);

        String roomTypeIds = setRoomTypeIds(promotion);
        renderArgs.put("roomTypeIds", roomTypeIds);

//        List<models.ktv.KtvPromotionType> promotionTypeList = models.ktv.KtvPromotionType.findRoomTypeList(supplier);
//        String shopIds = setShopIds(priceSchedule);
//        renderArgs.put("shopIds", shopIds);
        renderArgs.put("shopsList", shopsList);
        renderArgs.put("promotion", promotion);
    }

    private static String setRoomTypeIds(KtvPromotion promotion) {
        if (promotion == null) {
            return "";
        }
        String roomTypeIds = ",";
        if (promotion.roomTypes != null && promotion.roomTypes.size() > 0) {
            for (KtvRoomType roomType : promotion.roomTypes) {
                roomTypeIds += roomType.id + ",";
            }
        }
        return roomTypeIds;
    }

    private static String setShopIds(KtvPromotion promotion) {
        if (promotion == null) {
            return "";
        }
        String shopIds = ",";
        if (promotion.shops != null && promotion.shops.size() > 0) {
            for (Shop shop : promotion.shops) {
                shopIds += shop.id + ",";
            }
        }
        return shopIds;
    }


    public static void updateItem(Long promotionId, @Valid KtvPromotionConfig item, KtvPromotion promotion) {
        //判断是否选择了适用包厢
        Set<KtvRoomType> roomTypes = promotion.roomTypes;
        Validation.required("promotion.roomTypes", roomTypes);

        //适用时段
        if (promotion.startTime.compareTo(promotion.endTime) > 0) {
            Validation.addError("promotion.startTime", "validation.beforeThanEndTime");
        }
        List<KtvPromotionConfig> itemList =
                KtvPromotionConfig.find("promotion.id=? and deleted = ? ", promotionId, DeletedStatus.UN_DELETED).fetch();
        if (Validation.hasErrors()) {
            initParams(promotion);

            render("/KtvPromotions/add.html", promotion, item, itemList,promotionId);
        }

        KtvPromotion currentPromotion = KtvPromotion.findById(promotionId);
        if (StringUtils.isNotBlank(promotion.name)) {
            currentPromotion.name = promotion.name;
        }
        if (promotion.promotionType != null) {
            currentPromotion.promotionType = promotion.promotionType;
        }
        if (promotion.startDay != null) {
            currentPromotion.startDay = promotion.startDay;
        }
        if (promotion.endDay != null) {
            currentPromotion.endDay = promotion.endDay;
        }
        System.out.println(promotion.shops + "《=========promotion.shops:");
        if (promotion.shops != null) {
            currentPromotion.shops = promotion.shops;
        }
//        if (promotion.startTime != null) {
//            currentPromotion.startTime = promotion.startTime;
//        }
//        if (promotion.endTime != null) {
//            currentPromotion.endTime = promotion.endTime;
//        }
//        if (promotion.roomTypes != null) {
//            currentPromotion.roomTypes = promotion.roomTypes;
//        }

//        currentPromotion.save();
        item.promotion = currentPromotion;

        item.deleted = DeletedStatus.UN_DELETED;
        item.save();

        promotion = KtvPromotion.findById(promotionId);
        initParams(promotion);

        render("/KtvPromotions/add.html", itemList, promotionId, promotion);
    }

    public static void deleteItem(Long itemId, Long promotionId) {
        KtvPromotionConfig item = KtvPromotionConfig.findById(itemId);
        if (item != null) {
            item.deleted = DeletedStatus.DELETED;
            item.save();
        }
        List<KtvPromotionConfig> itemList =
                KtvPromotionConfig.find("promotion.id=? and deleted = ?", promotionId, DeletedStatus.UN_DELETED).fetch();
        KtvPromotion promotion = KtvPromotion.findById(promotionId);
        render("/KtvPromotions/add.html", itemList, promotionId, promotion);
    }


}
