package controllers;

import models.sales.Goods;
import models.supplier.Supplier;
import models.supplier.SupplierContract;
import models.supplier.SupplierContractCondition;
import models.supplier.SupplierContractImage;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import operate.rbac.annotations.Right;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collections;
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
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        Boolean hasViewContractPermission = ContextedPermission.hasPermission("VIEW_SUPPLIER_CONTRACT");

        int pageNumber = getPage();

        if (condition == null) {
            condition = new SupplierContractCondition();
        }
        Boolean hasManagerViewContractPermission = ContextedPermission.hasPermission("MANAGER_VIEW_SUPPLIER_CONTRACT");

        condition.hasManagerViewContractPermission = hasManagerViewContractPermission;
        condition.operatorId = OperateRbac.currentUser().id;

        JPAExtPaginator<SupplierContract> contractPage = SupplierContract.findByCondition(condition, pageNumber,
                PAGE_SIZE);

        List<Supplier> supplierList = Supplier.findUnDeleted();

        render(supplierList, contractPage, hasContractManagementPermission, hasViewContractPermission);
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void add(SupplierContract contract) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            render(supplierList, contract);
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void edit(Long contractId) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission == true) {
            SupplierContract contract = SupplierContract.findById(contractId);
            Supplier supplier = Supplier.findById(contract.supplierId);
            String supplierName = supplier.otherName;
            render(contractId, supplier, contract, supplierName);
        } else {
            index(null);
        }
    }

    public static void view(Long contractId) {
        Boolean hasViewContractPermission = ContextedPermission.hasPermission("VIEW_SUPPLIER_CONTRACT");
        if (hasViewContractPermission) {
            SupplierContract contract = SupplierContract.findById(contractId);
            Collections.sort(contract.supplierContractImagesList);
            render(contract);
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void update(Long contractId, @Valid SupplierContract contract) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission == true) {
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
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void updateDescription(Long imageId, String description) {

        System.out.println(imageId + "===imageId>>");
        System.out.println(description + "===description>>");
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission == true) {
            SupplierContractImage image = SupplierContractImage.findById(imageId);
            if (image != null) {
                image.description = description;
                image.save();
//                renderJSON("1");
//                uploadContract(image.contract.supplierId, image.contract.id);
            } else {
//                renderJSON("0");
                index(null);
            }
        } else {
            index(null);
        }
    }


    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void create(@Valid SupplierContract contract) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission == true) {
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
        } else {
            index(null);
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
    public static void uploadContract(Long supplierId, Long contractId) {
        Boolean hasContractManagementPermission = ContextedPermission.hasPermission("SUPPLIER_CONTRACT_MANAGEMENT");
        if (hasContractManagementPermission == true) {
            SupplierContract contract = SupplierContract.findById(contractId);
            render(supplierId, contractId, contract);
        } else {
            index(null);
        }
    }


    private static void checkExpireAt(SupplierContract contract) {
        if (contract.effectiveAt == null || contract.expireAt == null || contract.expireAt.before(contract.effectiveAt)) {
            Validation.addError("contract.expireAt", "validation.beforeThanContractEffectiveAt");
        }
    }

    @Right("SUPPLIER_CONTRACT_MANAGEMENT")
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
