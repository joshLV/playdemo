package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
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
@With({SecureCAS.class, WebsiteInjector.class})
public class Addresses extends Controller {

    public static void index() {
        List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
        render(addressList);
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

    public static void showDefault() {
        Address address = Address.findDefault(SecureCAS.getUser());
        render("Addresses/show.html", address);
    }

    public static void create(Address address) {
        Address defaultAddress = Address.findDefault(SecureCAS.getUser());
        if (defaultAddress != null) {
            defaultAddress.isDefault = false;
            defaultAddress.save();
        }

        address.user = SecureCAS.getUser();
        address.save();

        render("Addresses/line.html", address);
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
        Address.delete(id, SecureCAS.getUser());

        ok();
    }
}