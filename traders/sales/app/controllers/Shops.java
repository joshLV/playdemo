package controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import models.Shop;
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
        shop.save();
        list();
        
    }
    
    public static void add(){
        renderTemplate("shop-add.html");
    }
    
    public static void  update(Shop shop){
        
        Shop sp = Shop.findById(shop.id);
        
        validation.required(shop.name);
        validation.required(shop.address);
        validation.required(shop.phone);
        
        if(validation.hasErrors()) {
            params.flash();
            validation.keep();
            edit(shop.id);
        }

        sp.name = shop.name;
        sp.address = shop.address;
        sp.phone = shop.phone;
        sp.save();
        list();
    }
    
    public static void  edit(long id){
        
        Shop shop = Shop.findById(id);
        
        renderTemplate("shop-edit.html",shop);
    }
    
    public static void delete(long id){
        Shop shop  = Shop.findById(id);
        shop.deleted = 1;
        shop.save();
        
        list();
    }
    
    public static void list(){
        
        int size = 10;
        int page = 0;
        if(params.get("page") != null){
             page = Integer.parseInt(params.get("page"));
        }
        
        long company_id = 1;
        List<Shop> list = Shop.find("company_id=? and deleted=?", company_id,0).fetch(page, size);
        renderTemplate("shop-list.html",list);
        
        
    }
    
    
    
    
}
