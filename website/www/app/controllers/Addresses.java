package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.address.Address;
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
    }

    public static void delete(long id) {
        Address.delete("id = ?", id);
    }

}