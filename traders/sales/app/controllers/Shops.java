package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.uhuila.common.constants.DeletedStatus;

import models.sales.Area;
import models.sales.AreaType;
import models.sales.Shop;
import models.sales.Pager;
import play.data.validation.Valid;
import play.mvc.Controller;

import play.mvc.With;
import navigation.annotations.ActiveNavigation;
import controllers.modules.cas.SecureCAS;

@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("shops_index")
public class Shops extends Controller {

    /**
     * 添加门店
     * @param shop
     */
    public static void create(@Valid Shop shop){
        if(validation.hasErrors()) {
            params.flash();
            validation.keep();
            add();
        }

        shop.companyId = 1;
        shop.deleted = DeletedStatus.UN_DELETED;
        shop.createdAt = new Date();
        shop.create();
        list();

    }

    /**
     * 添加门店页面展示
     */
    public static void add(){
        //城市列表
        List<Area> citylist = Area.findAllSubAreas(null);
        //区域列表
        List<Area> districtslist = Area.findAllSubAreas(citylist.get(0).getId());
        //商圈列表
        List<Area> arealist = Area.findAllSubAreas(districtslist.get(0).getId());
        renderTemplate("shop-add.html",citylist,districtslist,arealist);
    }

    /**
     * 区域商圈的联动
     */
    public static void relation(String areaId){
        //商圈列表
        List<Area> arealist = Area.findAllSubAreas(areaId);
        renderJSON(arealist);
    }
    /**
     * 修改门店信息
     * @param id
     * @param shop
     */
    public static void  update(long id,Shop shop){
        Shop sp = Shop.findById(id);
        if(validation.hasErrors()) {
            params.flash();
            validation.keep();
            edit(shop.id);
        }
        sp.areaId = shop.areaId;
        sp.name = shop.name;
        sp.address = shop.address;
        sp.phone = shop.phone;
        sp.updatedAt = new Date();
        sp.save();
        list();
    }

    /**
     * 编辑门店页面展示
     * @param id
     */
    public static void  edit(long id){
        Shop shop = Shop.findById(id);
        //城市列表
        List<Area> citylist = Area.findAllSubAreas(null);
        //区域列表
        List<Area> districtslist = Area.findAllSubAreas(citylist.get(0).getId());
        //商圈列表
        List<Area> arealist = Area.findAllSubAreas(districtslist.get(0).getId());
        renderTemplate("shop-edit.html",shop,citylist,districtslist,arealist);
    }

    /**
     * 逻辑删除门店
     * @param id
     */
    public static void delete(long id){

        Shop.deleted(id);
        list();
    }

    /**
     * 门店一览信息
     */
    public static void list(){
        StringBuffer search = new StringBuffer();
        search.append("companyId=? and deleted=?");
        ArrayList queryparams = new ArrayList();

        long company_id = 1;
        queryparams.add(company_id);
        queryparams.add(DeletedStatus.UN_DELETED);

        if(params.get("shopname") != null){
            search.append(" and name like ?");
            queryparams.add("%"+params.get("shopname").trim()+"%");
        }

        if(params.get("shopaddr") != null){
            search.append(" and address like ?");
            queryparams.add("%"+params.get("shopaddr").trim()+"%");
        }

        Pager pager = new Pager();
        if(params.get("page") != null){
            pager.currPage = Integer.parseInt(params.get("page"));
        }
        pager.totalCount = Shop.count(search.toString(),queryparams.toArray());
        search.append(" order by created_at desc ");
        List<Shop> list = Shop.find(search.toString(), queryparams.toArray()).fetch(pager.currPage, pager.pageSize);
        pager.totalPager();
        renderTemplate("shop-list.html",list,pager);
    }
}
