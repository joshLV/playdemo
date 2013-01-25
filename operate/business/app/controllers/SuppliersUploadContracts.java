package controllers;

import models.supplier.Supplier;
import models.supplier.SupplierContractImage;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 上传合同的电子版本
 * <p/>
 * User: wangjia
 * Date: 13-1-23
 * Time: 下午6:31
 */

@With(OperateRbac.class)
@ActiveNavigation("suppliers_upload_contracts")
public class SuppliersUploadContracts extends Controller {
    public static void index() {
        List<Supplier> supplierList = Supplier.findAll();
        Supplier supplier = supplierList.get(0);
        Long id = supplier.id;
        render(id);
    }

    public static void edit() {
        List<Supplier> supplierList = Supplier.findAll();
        Supplier supplier = supplierList.get(0);
        Long supplierId = supplier.id;
        Long contractId = 1l;
        render(supplierId, contractId);
    }

    public static void update(Long id, Supplier supplier) {
        index();
    }

    public static void create(Supplier supplier) {
        index();
    }
}
