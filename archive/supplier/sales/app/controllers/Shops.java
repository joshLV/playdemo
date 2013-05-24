package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Area;
import models.sales.Shop;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

@With(SupplierRbac.class)
@ActiveNavigation("shops_index")
public class Shops extends Controller {

    private static final int PAGE_SIZE = 20;

    /**
     * 门店一览信息
     */
    public static void index(Shop shopCondition) {
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        Long supplierId = SupplierRbac.currentUser().supplier.id;
        if (shopCondition == null) {
            shopCondition = new Shop();
        }
        shopCondition.supplierId = supplierId;
        ModelPaginator<Shop> shopPage = Shop.query(shopCondition, pageNumber, PAGE_SIZE);
        render(shopPage, shopCondition);
    }

    /**
     * 添加门店
     *
     * @param shop 门店对象
     */
    public static void create(@Valid Shop shop) {
        if (Validation.hasErrors()) {
            renderParams(shop);
            render("Shops/add.html", shop);
        }
        shop.supplierId = SupplierRbac.currentUser().supplier.id;
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
        Area district = Area.findParent(shop.areaId);
        if (StringUtils.isEmpty(shop.areaId) || district == null) {
            //城市列表
            List<Area> cities = Area.findAllSubAreas(null);
            shop.cityId = shop.areaId;
            renderArgs.put("cities", cities);
            return;
        } else {
            shop.setDistrictId(district.id);
            Area city = Area.findParent(district.id);
            shop.cityId = city.id;
        }

        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        String cityId = StringUtils.isEmpty(shop.cityId) ? cities.get(0).getId() : shop.cityId;

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
            shop.id = id;
            render("/Shops/edit.html", shop, id);
        }
        if (shop.areaId == null) {
            sp.areaId = shop.cityId;
        } else {
            sp.areaId = shop.areaId;
        }
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
    public static void edit(long id) {
        Shop shop = Shop.findById(id);
        renderParams(shop);
        render(shop);
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
}
