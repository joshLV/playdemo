package controllers;

import controllers.supplier.SupplierInjector;
import models.ktv.KtvRoom;
import models.ktv.KtvRoomType;
import models.supplier.Supplier;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User: yan
 * Date: 13-4-11
 * Time: 下午3:32
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class KtvRoomTypes extends Controller {
    public static void index() {
        List<KtvRoomType> ktvRoomTypeList = KtvRoomType.findAll();
        render(ktvRoomTypeList);
    }

    public static void add() {
        render();
    }

    public static void create(KtvRoomType ktvRoomType) {
        ktvRoomType.supplier = SupplierRbac.currentUser().supplier;
        ktvRoomType.save();
        index();
    }

    public static void edit(Long id) {
        KtvRoomType ktvRoomType = KtvRoomType.findById(id);
        render(ktvRoomType);
    }

    public static void update(Long id, KtvRoomType ktvRoomType) {
        KtvRoomType kt = KtvRoomType.findById(id);
        kt.name = ktvRoomType.name;
        kt.save();
        render();
    }

    public static void delete(Long id) {
        KtvRoomType ktvRoomType = KtvRoomType.findById(id);
        if (ktvRoomType == null) {
            return;
        }
        ktvRoomType.delete();
        index();

    }
}
