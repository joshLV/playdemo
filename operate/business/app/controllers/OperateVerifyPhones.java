package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 */
@With(OperateRbac.class)
@ActiveNavigation("supplierVerifyPhones_index")
public class OperateVerifyPhones extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 操作员一览
     */
    public static void index(Long supplierId) {
        String page = request.params.get("page");
        String loginName = request.params.get("loginName");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<SupplierUser> supplierUsersPage = SupplierUser
                .getSupplierUserList(SupplierUserType.ANDROID, loginName, null, null,
                        supplierId, null, pageNumber,
                        PAGE_SIZE);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        renderArgs.put("loginName", loginName);
        render(supplierUsersPage, supplierList, supplierId);
    }

    /**
     * 操作员添加页面
     */
    @ActiveNavigation("supplierVerifyPhones_add")
    public static void add() {
        renderShopList();
        render();
    }

    public static void showSupplierShops(Long id) {
        List<Shop> shopList = Shop.findShopBySupplier(id);
        render(shopList);
    }

    private static void renderShopList() {
        Long supplierId = null;
        List<Supplier> supplierList = Supplier.findUnDeleted();
        if (supplierList != null && supplierList.size() > 0) {
            supplierId = supplierList.get(0).id;
        }
        renderArgs.put("supplierList", supplierList);

        List<Shop> shopList = Shop.findShopBySupplier(supplierId);
        renderArgs.put("shopList", shopList);
    }

    /**
     * 创建电话验证机
     *
     * @param supplierUser 操作员信息
     */
    @ActiveNavigation("supplierVerifyPhones_add")
    public static void create(SupplierUser supplierUser) {
        if (supplierUser.loginName == null || supplierUser.loginName.trim().equals("")) {
            Validation.addError("supplierUser.loginName", "请输入电话号码");
        } else {
            supplierUser.loginName = supplierUser.loginName.trim();
            if (SupplierUser.find("byLoginName", supplierUser.loginName).fetch().size() != 0) {
                Validation.addError("supplierUser.loginName", "该电话号码已存在");
            }
        }
        if (supplierUser.shop == null) {
            Validation.addError("supplierUser.shop.id", "请选择门店");
        }
        if (Validation.hasErrors()) {
            renderShopList();
            render("OperateVerifyPhones/add.html", supplierUser, null, null);
        }

        supplierUser.supplierUserType = SupplierUserType.ANDROID;
        supplierUser.create(supplierUser.supplier.id);
        index(null);
    }

    /**
     * 删除电话验证机
     */
    public static void delete(Long id) {
        SupplierUser user = SupplierUser.findById(id);
        if (user != null) {
            user.deleted = DeletedStatus.DELETED;
            user.save();
        }
        index(null);
    }

    /**
     * 操作员编辑页面
     */
    public static void edit(Long id) {
        SupplierUser supplierUser = SupplierUser.findById(id);
        List<Shop> shopList = Shop.findShopBySupplier(supplierUser.supplier.id);
        render(supplierUser, shopList);
    }

    /**
     * 操作员信息修改
     *
     * @param id        ID
     * @param loginName 电话号码
     */
    public static void update(Long id, String loginName, Long shopId) {
        SupplierUser supplierUser = SupplierUser.findById(id);
        if (supplierUser == null) {
            Validation.addError("supplierUser.loginName", "该记录不存在");
        } else {
            if (loginName == null || loginName.trim().equals("")) {
                Validation.addError("supplierUser.loginName", "请输入电话号码");
            } else {
                loginName = loginName.trim();
                if (!supplierUser.loginName.equals(loginName) && SupplierUser.find("byLoginName", loginName).fetch().size() != 0) {
                    Validation.addError("supplierUser.loginName", "该电话号码已存在");
                }
            }
            supplierUser.loginName = loginName;
        }
        Shop shop = Shop.findById(shopId);
        if (shop == null) {
            Validation.addError("supplierUser.shop", "无效的门店");
        } else {
            if (supplierUser != null && !shop.supplierId.equals(supplierUser.supplier.id)) {
                Validation.addError("supplierUser.shop", "无效的门店");
            }
        }

        if (Validation.hasErrors()) {
            List<Shop> shopList = new ArrayList<>();
            if (supplierUser != null) {
                shopList = Shop.findShopBySupplier(supplierUser.supplier.id);
            }
            render("OperateVerifyPhones/edit.html", supplierUser, shopList);
        }
        // 更新用户信息
        supplierUser.shop = shop;
        supplierUser.save();
        index(null);
    }
}
