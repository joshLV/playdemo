package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.Address;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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

    public static void add() {
        render();
    }

    public static void edit(long id) {
        Address address = Address.findById(id);
        render(address);
    }

    public static void show(long id) {
        Address address = Address.findById(id);
        render(address);
    }

    public static void create(long selectedAddressId, Address address) {
        if (selectedAddressId == 0) {
            address.user = WebTrace.getUser();
            address.save();
            render("Addresses/show.html", address);
        }

        Address selectedAddress = Address.findById(selectedAddressId);
        render("Addresses/show.html", selectedAddress);
    }

    public static void update(Address address) {
        address.user = getUser();
        if ("true".equals(address.isDefault)) {
            Address.updateToUnDefault(getUser());
        }
        address.isDefault = "true";
        address.save();
        render("Addresses/show.html", address);
    }

    public static void updateDefault(long id) {
        Address address = Address.findById(id);
        if (address != null) {
            Address.updateToUnDefault(getUser());
            address.isDefault = "true";
            address.save();
        }
        ok();
    }

    /**
     * 如果被删除的地址是默认地址，则系统自动选取一个作为默认地址.
     *
     * @param id
     */
    public static void delete(long id) {
        Address address = Address.findById(id);
        if (address != null) {
            if ("true".equals(address.isDefault)) {
                address.delete();
                List<Address> addressList = Address.findAll();
                if (addressList.size() > 0) {
                    Address defaultAddress = addressList.get(0);
                    defaultAddress.isDefault = "true";
                    defaultAddress.save();
                    renderJSON(defaultAddress.id);
                }
                ok();
            }
            address.delete();
        }
//        Address.delete("id=?", id);

        ok();
    }

}