package controllers.real;

import controllers.OperateRbac;
import models.order.ExpressCompany;
import models.order.Freight;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User: wangjia
 * Date: 13-5-15
 * Time: 上午11:16
 */
@With(OperateRbac.class)
@ActiveNavigation("freight_index")
public class Freights extends Controller {
    public static void index() {
        int pageNumber = getPage();
        List<Freight> freightList = Freight.findUnDeleted();
        render(freightList);
    }

    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<ExpressCompany> expressList = ExpressCompany.findAll();
        render(supplierList, expressList);
    }

    public static void create(@Valid Freight freight) {
        if (freight.supplier == null) {
            Validation.addError("freight.supplier.id", "validation.selected");
        }
        if (freight.express == null) {
            Validation.addError("freight.express.id", "validation.selected");
        }
        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            List<ExpressCompany> expressList = ExpressCompany.findAll();
            render("real/Freights/add.html", supplierList, expressList, freight);
        }
        freight.create();
        index();
    }

    public static void edit(Long id) {
        Freight freight = Freight.findById(id);
        render(freight);
    }

    public static void update(Long id, @Valid Freight freight) {
        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            List<ExpressCompany> expressList = ExpressCompany.findAll();
            render("real/Freights/edit.html", freight, id, supplierList, expressList);
        }
        Freight.update(id, freight);
        index();
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    public static void delete(long id) {
        Freight.delete(id);
        index();
    }


}
