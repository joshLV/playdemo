package controllers;

import java.util.List;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import com.uhuila.common.constants.DeletedStatus;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-26
 * Time: 上午9:50
 */
@With(OperateRbac.class)
@ActiveNavigation("supplierUsers_index")
public class OperateSupplierUsers extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 操作员一览
     */
    public static void index(Long supplierId) {
        String page = request.params.get("page");
        String loginName = request.params.get("loginName");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<SupplierUser> supplierUsersPage = SupplierUser
                .getSupplierUserList(loginName,
                        supplierId, pageNumber,
                        PAGE_SIZE);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        renderArgs.put("loginName", loginName);
        render(supplierUsersPage, supplierList, supplierId);
    }

    /**
     * 操作员添加页面
     */
    @ActiveNavigation("supplierUsers_add")
    public static void add() {
        List rolesList = SupplierRole.findRoleOrderById();
        Long supplierId = null;
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (supplierList != null && supplierList.size() > 0) {
            supplierId = supplierList.get(0).id;
            checkShops(supplierId);
            renderShopList(supplierId);
        }
        render(rolesList, supplierList);
    }

    private static void checkShops(Long supplierId) {
        if (!Shop.containsShop(supplierId)) {
            renderArgs.put("noShop", "noShop");
            Validation.addError("supplierUser.supplierId", "validation.noShop");
        }
    }

    private static void renderShopList(Long supplierId) {
        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        renderArgs.put("shopList", shopList);
    }

    /**
     * 创建操作员
     *
     * @param supplierUser 操作员信息
     */
    @ActiveNavigation("supplierUsers_add")
    public static void create(@Valid SupplierUser supplierUser) {
        checkValid(null, supplierUser);
        supplierUser.create(supplierUser.supplier.id);
        index(null);
    }

    /**
     * 逻辑删除操作员
     */
    public static void delete(Long id) {
        SupplierUser user = SupplierUser.findById(id);
        user.deleted = DeletedStatus.DELETED;
        user.save();
        index(null);
    }

    /**
     * 操作员编辑页面
     */
    public static void edit(Long id) {
        SupplierUser supplierUser = SupplierUser.findById(id);
        String roleIds = "";
        if (supplierUser.roles != null && supplierUser.roles.size() > 0) {
            for (SupplierRole role : supplierUser.roles) {
                roleIds += role.id + ",";
            }
        }
        List rolesList = SupplierRole.findRoleOrderById();
        Long supplierId = supplierUser.supplier.id;
        List shopList = Shop.findShopBySupplier(supplierId);
        render(supplierUser, roleIds, rolesList, shopList);
    }

    /**
     * 操作员信息修改
     *
     * @param id           ID
     * @param supplierUser 用户信息
     */
    public static void update(Long id, @Valid SupplierUser supplierUser) {
        checkValid(id, supplierUser);
        // 更新用户信息
        SupplierUser.update(id, supplierUser);
        index(null);
    }

    /**
     * 验证
     *
     * @param supplierUser 操作员信息
     */
    private static void checkValid(Long id, SupplierUser supplierUser) {
        Validation.required("supplierUser.encryptedPassword", supplierUser.encryptedPassword);
        Validation.required("supplierUser.confirmPassword", supplierUser.confirmPassword);
        Validation.match("validation.jobNumber", supplierUser.jobNumber, "^[0-9]*");
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (supplierUser.roles != null && supplierUser.roles.size() > 0) {
                for (SupplierRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            supplierUser.id = id;
            if (id != null) {
                render("OperateSupplierUsers/edit.html", supplierUser, roleIds, rolesList);
            } else {
                render("OperateSupplierUsers/add.html", supplierUser, roleIds, rolesList);
            }
        }

    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static void checkLoginName(Long id,Long supplierId, String loginName, String mobile, String jobNumber) {
        String returnFlag = SupplierUser.checkValue(id, loginName, mobile, jobNumber,
                supplierId);

        renderJSON(returnFlag);
    }

}
