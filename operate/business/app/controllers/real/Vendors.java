package controllers.real;

import controllers.OperateRbac;
import models.order.Vendor;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 供货商管理
 * <p/>
 * User: wangjia
 * Date: 13-3-28
 * Time: 下午4:36
 */
@With(OperateRbac.class)
@ActiveNavigation("vendors_index")
public class Vendors extends Controller {
    @ActiveNavigation("vendors_index")
    public static void index(String keyword) {
        int page = getPage();
        List<Vendor> vendorList = Vendor.findByCondition(keyword);
        render(vendorList, page, keyword);
    }

    @ActiveNavigation("vendors_add")
    public static void add() {
        render();
    }

    @ActiveNavigation("vendors_add")
    public static void create(@Valid Vendor vendor) {
        if (Validation.hasErrors()) {
            render("real/Vendors/add.html");
        }
        vendor.createdBy = OperateRbac.currentUser().userName;
        vendor.create();
        index(null);
    }

    public static void edit(Long id) {
        Vendor vendor = Vendor.findById(id);
        render(vendor);
    }


    public static void update(Long id, @Valid Vendor vendor) {
        if (Validation.hasErrors()) {
            render("real/Vendors/edit.html", vendor, id);
        }
        Vendor.update(id, vendor);
        index(null);
    }

    public static void delete(long id) {
        Vendor.delete(id);
        index(null);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }


}
