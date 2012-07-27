package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.Address;
import models.consumer.User;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.breadcrumbs.BreadcrumbList;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

import static play.Logger.warn;

@With({SecureCAS.class, WebsiteInjector.class})
public class UserAddresses extends Controller {
    /**
     * 地址列表
     */
    public static void index() {
        User user = SecureCAS.getUser();

        List<Address> addressList = Address.findByOrder(user);
        BreadcrumbList breadcrumbs = new BreadcrumbList("收货地址", "/addresses");

        render(addressList, breadcrumbs, user);
    }

    public static void edit(long id) {
        User user = SecureCAS.getUser();
        Address address = Address.findById(id);

        List<Address> addressList = Address.findByOrder(user);

        render("UserAddresses/form.html", addressList, address);
    }

    public static void create(@Valid Address address) {
        User user = SecureCAS.getUser();

        checkAddress(address, user);

        address.user = SecureCAS.getUser();
        address.createdAt = new Date();

        if (address.isDefault == null) {
            address.isDefault = false;
        }
        if (address.isDefault) {
            Address.updateToUnDefault(SecureCAS.getUser());
        }
        address.create();

        index();
    }

    private static void checkAddress(Address address, User user) {
        if (StringUtils.isBlank(address.getPhone()) && StringUtils.isBlank(address.mobile)) {
            Validation.addError("address.mobile", "validation.required");
        }
        if (Validation.hasErrors()) {
            List<Address> addressList = Address.findByOrder(user);
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("UserAddresses/index.html", addressList, address);
        }
    }

    public static void update(Long id, @Valid Address address) {
        User user = SecureCAS.getUser();

        checkAddress(address, user);

        if (address.isDefault == null) {
            address.isDefault = false;
        }

        Address.update(id, address);

        index();
    }

    public static void updateDefault(long id) {
        Address.updateDefault(id, SecureCAS.getUser());

        index();
    }

    /**
     * 如果被删除的地址是默认地址，则系统自动选取一个作为默认地址.
     *
     * @param id
     */
    public static void delete(long id) {
        Address.delete(id, SecureCAS.getUser());

        index();
    }
}
