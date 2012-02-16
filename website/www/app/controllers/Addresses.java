package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.Address;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 用户地址控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 1:43 PM
 */
@With(WebTrace.class)
public class Addresses extends Controller {

    public static void index() {

    }

    public static void add(){
        render();
    }
    
    public static void edit(long id){
        Address address = Address.findById(id);
        render(address);
    }
    
    public static void show(long id){
        Address address = Address.findById(id);
        render(address);
    }

    public static void create(long selectedAddressId, Address address) {
        if (selectedAddressId == 0) {
            address.save();
            render(address);
        }

        Address selectedAddress = Address.findById(selectedAddressId);
        render(selectedAddress);
    }

    public static void update(Address address) {
        address.save();
        ok();
    }

    public static void delete(long id) {
        Address address = Address.findById(id);
        if (address != null) {
            address.delete();
        }
//        Address.delete("id=?", id);

        ok();
    }

}