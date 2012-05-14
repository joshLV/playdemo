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

        List<Supplier> supplierList = Supplier.findAll();

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
        List<Supplier> supplierList = Supplier.findAll();
        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        List<Area> districts = Area.findAllSubAreas(cities.get(0).getId());
        //商圈列表
        List<Area> areas = Area.findAllSubAreas(districts.get(0).getId());
        render(supplierList, cities, districts, areas, shop);
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
            edit(id, shop);
        }
        sp.areaId = shop.areaId;
        sp.name = shop.name;
        sp.address = shop.address;
        sp.phone = shop.phone;
        sp.updatedAt = new Date();
        sp.save();
        index(null);
    }

    /**
     * 编辑门店页面展示
     *
     * @param id 门店标识
     */
    public static void edit(long id, Shop shop) {
        Shop originalShop = shop;
        originalShop.id = id;
        if (originalShop.areaId == null) {
            originalShop = Shop.findById(id);
            if (originalShop == null) {
                error(404, "没有找到该门店");
            }
            Supplier supplier = Supplier.findById(originalShop.supplierId);
            if (supplier != null) {
                originalShop.supplierName = supplier.fullName;
            }

        }

        List<Supplier> supplierList = Supplier.findAll();
        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        List<Area> districts = Area.findAllSubAreas(cities.get(0).getId());
        //商圈列表
        List<Area> areas = Area.findAllSubAreas(districts.get(0).getId());


        renderArgs.put("shop", originalShop);
        render(supplierList, cities, districts, areas);
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
}
