package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Area;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
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
    public static final String BASE_URL = Play.configuration.getProperty("application.baseUrl", "");
    private static final int PAGE_SIZE = 20;

    /**
     * 门店一览信息
     */
    public static void index(Shop shopCondition) {
        int pageNumber = getPage();
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
        shop.name = StringUtils.trimToEmpty(shop.name);
        shop.address = StringUtils.trimToEmpty(shop.address);
        shop.phone = StringUtils.trimToEmpty(shop.phone);
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.createdAt = new Date();
        shop.lockVersion = 0;
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
        Area district = Area.findParent(shop.areaId);
        if (StringUtils.isEmpty(shop.areaId) || district == null) {
            //城市列表
            List<Area> cities = Area.findAllSubAreas(null);
            if (CollectionUtils.isNotEmpty(cities)) {
                List<Area> districts = Area.findAllSubAreas(cities.get(0).id);
                renderArgs.put("districts", districts);
                if (CollectionUtils.isNotEmpty(districts)) {
                    List<Area> areas = Area.findAllSubAreas(districts.get(0).id);
                    renderArgs.put("areas", areas);
                }
            }

            shop.cityId = shop.areaId;
            renderArgs.put("cities", cities);

            renderArgs.put("supplierList", supplierList);
            return;
        } else {
            shop.setDistrictId(district.id);
            Area city = Area.findParent(district.id);
            shop.cityId = city.id;
        }

        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        String cityId = StringUtils.isEmpty(shop.cityId) ? cities.get(0).id : shop.cityId;

        List<Area> districts = Area.findAllSubAreas(cityId);
        //商圈列表
        String districtId = StringUtils.isEmpty(shop.getDistrictId()) ? districts.get(0).getId() : shop.getDistrictId();
        List<Area> areas = Area.findAllSubAreas(districtId);
        if (shop.areaId == null && areas != null && areas.size() > 0 && areas.get(0) != null) {
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
        int page = getPage();
        Shop sp = Shop.findById(id);
        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            edit(id, shop);
        }
        sp.transport = shop.transport;
        if (shop.areaId == null) {
            sp.areaId = shop.cityId;
        } else {
            sp.areaId = shop.areaId;
        }
        sp.name = StringUtils.trimToEmpty(shop.name);
        sp.address = StringUtils.trimToEmpty(shop.address);
        sp.phone = StringUtils.trimToEmpty(shop.phone);
        sp.managerMobiles = shop.managerMobiles;
        sp.updatedAt = new Date();
        sp.longitude = shop.longitude;
        sp.latitude = shop.latitude;
        sp.independentClearing = shop.independentClearing;
        sp.save();
        sp.createAccountsIfNeeded();
        redirectUrl(page);
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
        renderArgs.put("page", getPage());
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
        Supplier supplier = Supplier.findById(supplierId);
        boolean ktvSupplier = false;
        if ("1".equals(supplier.getProperty(Supplier.KTV_SUPPLIER))) {
            ktvSupplier = true;
        }
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        render(shopList, ktvSupplier);
    }

    @ActiveNavigation("supplierUsers_add")
    public static void showSupplierShops(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        render(shopList);
    }

    public static void showIndependentShops(Long supplierId) {
        List<Shop> independentShops = Shop.getIndependentShops(supplierId);
        if (independentShops == null) {
            independentShops = new ArrayList<>();
        }
        render(independentShops);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    private static void redirectUrl(int page) {
        if (Play.mode.isDev()) {
            redirect("http://localhost:9303/" + "shops?page=" + page);
        } else {
            redirect(BASE_URL + "/shops?page=" + page);
        }
    }
}
