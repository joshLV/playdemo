package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.SupplierInjector;
import models.ktv.KtvSalesPromotion;
import models.sales.Shop;
import models.supplier.Supplier;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * ktv促销活动管理
 * <p/>
 * User: wangjia
 * Date: 13-4-18
 * Time: 上午10:36
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvSalesPromotions extends Controller {
    public static void index() {
        render();
    }

    public static void add() {
        initParams(null);
        render();
    }

    public static void create(@Valid KtvSalesPromotion salesPromotion) {
        if (Validation.hasErrors()) {
            initParams(salesPromotion);
            render("/KtvSalesPromotions/add.html", salesPromotion);
        }
//        System.out.println(salesPromotion.name + "《=========salesPromotion.name:");
//        System.out.println(salesPromotion.promotionType + "《=========salesPromotion.promotionType:");
//        System.out.println(salesPromotion.endDay + "《=========salesPromotion.endDay:");
//        System.out.println(salesPromotion.endTime + "《=========salesPromotion.endTime:");
//        System.out.println(salesPromotion.startDay + "《=========salesPromotion.startDay:");
//        System.out.println(salesPromotion.startTime + "《=========salesPromotion.startTime:");
        salesPromotion.createdAt = new Date();
        salesPromotion.deleted = DeletedStatus.UN_DELETED;
        salesPromotion.save();
        index();
    }

    private static void initParams(KtvSalesPromotion salesPromotion) {
        //初始化促销类型
        List<String> promotionTypeList = new LinkedList<>();
        promotionTypeList.add("CONTINUOUS_RESERVE_DISCOUNT");
        promotionTypeList.add("CONTINUOUS_RESERVE_REDUCTION");
        promotionTypeList.add("ADVANCED_RESERVE_DISCOUNT");
        promotionTypeList.add("ADVANCED_RESERVE_REDUCTION");

        Supplier supplier = SupplierRbac.currentUser().supplier;
        List<Shop> shops = Shop.findShopBySupplier(supplier.id);

//        List<models.ktv.KtvPromotionType> promotionTypeList = models.ktv.KtvPromotionType.findRoomTypeList(supplier);
//        String shopIds = setShopIds(priceSchedule);
//        renderArgs.put("shopIds", shopIds);
        renderArgs.put("shops", shops);
        renderArgs.put("promotionTypeList", promotionTypeList);
    }
}
