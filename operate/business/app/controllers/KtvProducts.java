package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.ktv.KtvProduct;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Ktv产品管理.
 * <p/>
 * User: wangjia
 * Date: 13-5-7
 * Time: 上午9:52
 */
@With(OperateRbac.class)
@ActiveNavigation("ktv_products_index")
public class KtvProducts extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index(String name, Long supplierId) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        ModelPaginator productPage = KtvProduct.getProductPage(pageNumber, PAGE_SIZE, supplierId, name);

        render(supplierList, productPage, name, supplierId);
    }

    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(supplierList);
    }

    public static void create(@Valid KtvProduct product) {
        if (product.supplier == null || product.supplier.id == 0) {
            Validation.addError("product.supplier", "validation.selected");
        }
        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            render("KtvProducts/add.html", supplierList,product);
        }

        product.createdAt = new Date();
        product.createdBy = OperateRbac.currentUser().loginName;
        product.deleted = DeletedStatus.UN_DELETED;
        product.create();

        index(null, null);
    }

    public static void edit(Long id) {
        KtvProduct product = KtvProduct.findById(id);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(product, supplierList, id);
    }

    public static void update(Long id, @Valid KtvProduct product, String name ) {
        if (Validation.hasErrors()) {
            render("KtvProducts/edit.html", product, id, name);
        }


        KtvProduct.update(id, product, OperateRbac.currentUser().loginName);

        index(null, null);
    }

    public static void delete(Long id) {
        KtvProduct product = KtvProduct.findById(id);
        if (product != null) {
            product.deleted = DeletedStatus.DELETED;
            product.save();
        }
        index(null, null);
    }


}
