package controllers;

import models.sales.*;
import models.supplier.SupplierCategory;
import models.supplier.SupplierCategoryCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User: wangjia
 * Date: 12-11-29
 * Time: 下午1:54
 */

@With(OperateRbac.class)
@ActiveNavigation("suppliers_category_index")
public class SuppliersCategory extends Controller {
    public static final String BASE_URL = Play.configuration.getProperty("application.baseUrl", "");
    public static int PAGE_SIZE = 15;

    public static void index(SupplierCategoryCondition condition) {
        int pageNumber = getPage();
        if (condition == null) {
            condition = new SupplierCategoryCondition();
        }
        JPAExtPaginator<SupplierCategory> supplierCategoryPage = SupplierCategory.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        render(supplierCategoryPage, condition);
    }

    public static void add() {
        render();
    }

    public static void create(@Valid SupplierCategory supplierCategory) {
        if (supplierCategory.code.length() != 2 || !supplierCategory.code.matches("[0-9]+")) {
            Validation.addError("supplierCategory.code", "validation.valid");
        }
        if (!checkUniqueName(supplierCategory.name)) {
            Validation.addError("supplierCategory.name", "validation.unique");
        }
        if (Validation.hasErrors()) {
            render("SuppliersCategory/add.html", supplierCategory);
        }
        supplierCategory.createdBy = OperateRbac.currentUser().loginName;
        supplierCategory.create();
        index(null);
    }

    public static void edit(long id) {
        int page = getPage();
        SupplierCategory supplierCategory = SupplierCategory.findById(id);
        render(supplierCategory, page, id);
    }

    public static void update(Long id, SupplierCategory supplierCategory) {
        SupplierCategory oldSupplierCategory = SupplierCategory.findById(id);
        supplierCategory.code = oldSupplierCategory.code;
        int page = getPage();
        if (supplierCategory.name.compareTo(oldSupplierCategory.name) < 0 && !checkUniqueName(supplierCategory.name)) {
            Validation.addError("supplierCategory.name", "validation.unique");
        }
        if (Validation.hasErrors()) {
            render("/SuppliersCategory/edit.html", id, supplierCategory);
        }
        supplierCategory.updatedBy = OperateRbac.currentUser().loginName;
        SupplierCategory.update(id, supplierCategory);

        index(null);
    }


    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    private static Boolean checkUniqueName(String name) {
        if (SupplierCategory.find("name=?", name).fetch().size() == 0) {
            return true;
        }
        return false;
    }
}
