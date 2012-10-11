package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Area;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 门店管理控制器.
 * <p/>
 * User: sujie
 * Date: 4/12/12
 * Time: 1:57 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("shops_index")
public class OperateShops extends Controller {

    private static final int PAGE_SIZE = 20;

    /**
     * 门店一览信息
     */
    public static void index(Shop shopCondition) {
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (shopCondition == null) {
            shopCondition = new Shop();
        }
        ModelPaginator<Shop> shopPage = Shop.query(shopCondition, pageNumber, PAGE_SIZE);
        for (Shop shop : shopPage) {
            Supplier supplier = Supplier.findById(shop.supplierId);
            if (supplier != null) {
                shop.supplierName = supplier.fullName;
            }
        }

        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(shopPage, supplierList, shopCondition);
    }

    /**
     * 添加门店
     *
     * @param shop 门店对象
     */
    public static void create(@Valid Shop shop) {
        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            add(shop);
        }

        shop.deleted = DeletedStatus.UN_DELETED;
        shop.createdAt = new Date();
        shop.create();
        index(null);

    }

    /**
     * 添加门店页面展示
     */
    @ActiveNavigation("shops_add")
    public static void add(Shop shop) {
        renderParams(shop);
        render(shop);
    }

    private static void renderParams(Shop shop) {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (StringUtils.isNotEmpty(shop.areaId)) {
            Area district = Area.findParent(shop.areaId);
            shop.districtId = district.id;
            Area city = Area.findParent(district.id);
            shop.cityId = city.id;
        }
        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        String cityId = StringUtils.isEmpty(shop.cityId) ? cities.get(0).getId() : shop.cityId;
        List<Area> districts = Area.findAllSubAreas(cityId);
        //商圈列表
        String districtId = StringUtils.isEmpty(shop.districtId) ? districts.get(0).getId() : shop.districtId;
        List<Area> areas = Area.findAllSubAreas(districtId);
        if (shop.areaId == null && areas != null && areas.get(0) != null) {
            shop.areaId = areas.get(0).id;
        }

        renderArgs.put("districts", districts);
        renderArgs.put("areas", areas);
        renderArgs.put("cities", cities);
        renderArgs.put("supplierList", supplierList);
    }

    /**
     * 区域商圈的联动
     *
     * @param areaId
     */
    public static void showAreas(String areaId) {
        //商圈列表
        renderJSON(Area.findAllSubAreas(areaId));
    }

    /**
     * 修改门店信息
     *
     * @param id   门店标识
     * @param shop 修改后的门店
     */
    public static void update(long id, @Valid Shop shop) {
        Shop sp = Shop.findById(id);
        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            edit(id, shop);
        }
        sp.areaId = shop.areaId;
        sp.name = shop.name;
        sp.address = shop.address;
        sp.phone = shop.phone;
        sp.managerMobiles = shop.managerMobiles;
        sp.updatedAt = new Date();
        sp.longitude = shop.longitude;
        sp.latitude = shop.latitude;
        sp.save();
        index(null);
    }

    /**
     * 编辑门店页面展示
     *
     * @param id 门店标识
     */
    public static void edit(long id, Shop shop) {
        Shop originalShop;
        if (id > 0) {
            originalShop = Shop.findById(id);
        } else {
            originalShop = shop;
        }
        if (originalShop == null) {
            error(404, "没有找到该门店!");
        }

        Supplier supplier = Supplier.findById(originalShop.supplierId);
        if (supplier != null) {
            originalShop.supplierName = supplier.fullName;
        }

        renderParams(originalShop);

        renderArgs.put("shop", originalShop);
        render();
    }

    /**
     * 逻辑删除门店
     *
     * @param id 门店标识
     */
    public static void delete(long id) {
        Shop.delete(id);
        index(null);
    }

    @ActiveNavigation("goods_add")
    public static void showGoodsShops(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        render(shopList);
    }
    @ActiveNavigation("supplierUsers_add")
     public static void showSupplierShops(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        render(shopList);
    }
}
