package controllers;

import java.util.List;
import models.consumer.Address;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;

/**
 * 用户地址控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 1:43 PM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Addresses extends Controller {

    public static void index() {
        render();
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
            address.user = SecureCAS.getUser();
            address.save();
            render("Addresses/show.html", address);
        }

        Address selectedAddress = Address.findById(selectedAddressId);
        render("Addresses/show.html", selectedAddress);
    }

    public static void update(Address address) {
        address.user = SecureCAS.getUser();
        if (address.isDefault == null) {
            address.isDefault = false;
        }
        if (address.isDefault) {
            Address.updateToUnDefault(SecureCAS.getUser());
        }
        address.isDefault = true;
        address.save();
        render("Addresses/show.html", address);
    }

    public static void updateDefault(long id) {
        Address.updateDefault(id, SecureCAS.getUser());

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
            if (address.isDefault != null && address.isDefault) {
                address.delete();
                List<Address> addressList = Address.findAll();
                if (addressList.size() > 0) {
                    Address defaultAddress = addressList.get(0);
                    defaultAddress.isDefault = true;
                    defaultAddress.save();
                    renderJSON(defaultAddress.id);
                }
                ok();
            } else {
                address.delete();
            }
        }
//        Address.delete("id=?", id);

        ok();
    }

}