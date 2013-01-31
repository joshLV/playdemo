package controllers;

import models.sales.Goods;
import models.supplier.Supplier;
import models.supplier.SupplierContract;
import models.supplier.SupplierContractCondition;
import models.supplier.SupplierContractImage;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 商户合同
 * <p/>
 * User: wangjia
 * Date: 13-1-23
 * Time: 下午6:31
 */

@With(OperateRbac.class)
@ActiveNavigation("suppliers_contracts")
public class SuppliersContracts extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index(SupplierContractCondition condition) {
//        List<Supplier> supplierList = Supplier.findAll();
//        Supplier supplier = supplierList.get(0);
//        Long id = supplier.id;
        int pageNumber = getPage();

        if (condition == null) {
            condition = new SupplierContractCondition();
        }

        JPAExtPaginator<SupplierContract> contractPage = SupplierContract.findByCondition(condition, pageNumber,
                PAGE_SIZE);

        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(supplierList, contractPage);
    }

    public static void add(SupplierContract contract) {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(supplierList, contract);
    }

    public static void edit(Long contractId) {
//        List<Supplier> supplierList = Supplier.findAll();
//        Supplier supplier = supplierList.get(0);
//        Long supplierId = supplier.id;
        SupplierContract contract = SupplierContract.findById(contractId);
        Supplier supplier = Supplier.findById(contract.supplierId);
        String supplierName = supplier.otherName;
        render(contractId, supplier, contract, supplierName);
    }

    public static void view(Long contractId) {
        SupplierContract contract = SupplierContract.findById(contractId);
        render(contract);
    }

    public static void update(Long contractId, @Valid SupplierContract contract) {
        checkExpireAt(contract);
        if (StringUtils.isBlank(contract.description)) {
            Validation.addError("contract.description", "validation.required");
        }
        SupplierContract currentContract = SupplierContract.findById(contractId);
        Supplier supplier = Supplier.findById(currentContract.supplierId);
        String supplierName = supplier.otherName;
        if (Validation.hasErrors()) {
            render("SuppliersContracts/edit.html", contractId, supplier, contract, supplierName);
        }
        contract.updatedBy = OperateRbac.currentUser().loginName;
        SupplierContract.update(contractId, contract);
        uploadContract(currentContract.supplierId, contractId);
    }

    public static void create(@Valid SupplierContract contract) {
        checkExpireAt(contract);
        if (contract.supplierId == 0 || contract.supplierId == null) {
            Validation.addError("contract.supplierId", "validation.selectExisted");
        }
        if (StringUtils.isBlank(contract.description)) {
            Validation.addError("contract.description", "validation.required");
        }
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (Validation.hasErrors()) {
            render("SuppliersContracts/add.html", supplierList, contract);
        }
        Supplier supplier = Supplier.findById(contract.supplierId);
        SupplierContract newContract = new SupplierContract(supplier);
        newContract.createdBy = OperateRbac.currentUser().loginName;
        newContract.effectiveAt = contract.effectiveAt;
        newContract.expireAt = contract.expireAt;
        newContract.description = contract.description;
        newContract.create();
        newContract.save();
        uploadContract(newContract.supplierId, newContract.id);
    }

    public static void uploadContract(Long supplierId, Long contractId) {
        SupplierContract contract = SupplierContract.findById(contractId);
        render(supplierId, contractId, contract);
    }

    private static void checkExpireAt(SupplierContract contract) {
        if (contract.effectiveAt == null || contract.expireAt == null || contract.expireAt.before(contract.effectiveAt)) {
            Validation.addError("contract.expireAt", "validation.beforeThanEffectiveAt");
        }
    }

    public static void delete(long id) {
        SupplierContract.delete(id);
        index(null);
    }

    /**
     * 删除商户合同一个图片
     *
     * @param id
     */
    public static void deleteImage(Long id) {
        SupplierContractImage images = SupplierContractImage.findById(id);
        images.delete();
        renderJSON("");
    }

    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
