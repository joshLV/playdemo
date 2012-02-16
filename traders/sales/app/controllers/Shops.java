package controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.sales.Shop;
import models.sales.Pager;
import play.mvc.Controller;

public class Shops extends Controller {

    
    public static void create(Shop shop){
        
        validation.required(shop.name);
        validation.required(shop.address);
        validation.required(shop.phone);
        
        if(validation.hasErrors()) {
           
            params.flash();
            validation.keep();
            renderTemplate("shop-add.html",params);
        }
        
        shop.company_id = 1;
        shop.area_id= 1;
        shop.deleted = 0;
        shop.create();
        list();
        
    }
    
    public static void add(){
        renderTemplate("shop-add.html");
    }
    
    public static void  update(long id,Shop shop){
        
        Shop sp = Shop.findById(id);
        
        validation.required(shop.name);
        validation.required(shop.address);
        validation.required(shop.phone);
        
        if(validation.hasErrors()) {
            params.flash();
            validation.keep();
            edit(shop.id);
        }
System.out.println("aaaaaaaa");
        sp.name = shop.name;
        sp.address = shop.address;
        sp.phone = shop.phone;
        sp.create();
        list();
    }
    
    public static void  edit(long id){
        
        Shop shop = Shop.findById(id);
        
        renderTemplate("shop-edit.html",shop);
    }
    
    public static void delete(long id){
        
        Shop.deleted(id);
        list();
    }
    
    public static void list(){
        
        StringBuffer search = new StringBuffer();
        search.append("companyId=? and deleted=?");
        ArrayList queryparams = new ArrayList();
        
        long company_id = 1;
        queryparams.add(company_id);
        queryparams.add(0);
        
        if(params.get("shopname") != null){
            search.append(" and name like ?");
            queryparams.add("%"+params.get("shopname").trim()+"%");
        }
        
        if(params.get("shopaddr") != null){
            search.append(" and address like ?");
            queryparams.add("%"+params.get("shopaddr").trim()+"%");
        }
        
        
        Pager pager = new Pager();
        //pager.params = params.all().;
        pager.pageSize = 3;
        pager.numsize = 4;
        if(params.get("page") != null){
            pager.currPage = Integer.parseInt(params.get("page"));
        }
        pager.totalCount = Shop.count(search.toString(),queryparams.toArray());

        List<Shop> list = Shop.find(search.toString(), queryparams.toArray()).fetch(pager.currPage, pager.pageSize);
        pager.list = list;
        pager.totalPager();
        renderTemplate("shop-list.html",pager);
        
        
    }
    
    
    
    
}
