package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.uhuila.common.constants.DeletedStatus;

import models.sales.Area;
import models.sales.AreaType;
import models.sales.Shop;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;

import play.mvc.With;
import navigation.annotations.ActiveNavigation;
import controllers.supplier.cas.SecureCAS;

@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("shops_index")
public class Shops extends Controller {

    private static final int PAGE_SIZE = 20;

    /**
     * 门店一览信息
     */
    public static void index() {
        String page = params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        long companyId = getCompanyId();
        Shop shopCondition = new Shop();
        shopCondition.companyId = companyId;
        shopCondition.name = params.get("shopname");
        shopCondition.address = params.get("shopaddr");
        ModelPaginator<Shop> shopPage = Shop.query(shopCondition, pageNumber, PAGE_SIZE);
        render(shopPage);
    }

    /**
     * 添加门店
     *
     * @param shop 门店对象
     */
    public static void create(@Valid Shop shop) {
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            add(shop);
        }

        shop.companyId = 1;
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.createdAt = new Date();
        shop.create();
        index();

    }

    /**
     * 添加门店页面展示
     */
    @ActiveNavigation("shops_add")
    public static void add(Shop shop) {
        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        List<Area> districts = Area.findAllSubAreas(cities.get(0).getId());
        //商圈列表
        List<Area> areas = Area.findAllSubAreas(districts.get(0).getId());
        renderArgs.put("shop", shop);
        render(cities, districts, areas);
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
        if (validation.hasErrors()) {
            edit(id, shop);
        }
        sp.areaId = shop.areaId;
        sp.name = shop.name;
        sp.address = shop.address;
        sp.phone = shop.phone;
        sp.updatedAt = new Date();
        sp.save();
        index();
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
        }

        //城市列表
        List<Area> cities = Area.findAllSubAreas(null);
        //区域列表
        List<Area> districts = Area.findAllSubAreas(cities.get(0).getId());
        //商圈列表
        List<Area> areas = Area.findAllSubAreas(districts.get(0).getId());
        renderArgs.put("shop", originalShop);
        render(cities, districts, areas);
    }

    /**
     * 逻辑删除门店
     *
     * @param id 门店标识
     */
    public static void delete(long id) {
        Shop.delete(id);
        index();
    }

    private static long getCompanyId() {
        //todo
        return 1;
    }
}
