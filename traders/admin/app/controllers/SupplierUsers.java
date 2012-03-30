package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.cas.SecureCAS;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 操作员CRUD
 *
 * @author yanjy
 */

@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("user_search")
public class SupplierUsers extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 操作员一览
     */
    @ActiveNavigation("user_search")
    public static void index() {
        String page = request.params.get("page");
        String loginName = request.params.get("loginName");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<SupplierUser> supplierUsersPage = SupplierUser.getSupplierUserList(
                loginName, MenuInjector.currentUser().supplier.id, pageNumber, PAGE_SIZE);
        renderArgs.put("loginName", loginName);
        render(supplierUsersPage);
    }

    /**
     * 操作员添加页面
     */
    @ActiveNavigation("user_add")
    public static void add() {
        List rolesList = SupplierRole.findNotAdmin();
        render(rolesList);
    }

    /**
     * 创建操作员
     *
     * @param supplierUser 操作员信息
     */
    @ActiveNavigation("user_add")
    public static void create(@Valid SupplierUser supplierUser) {
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (supplierUser.roles != null && !supplierUser.roles.isEmpty()) {
                for (SupplierRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("SupplierUsers/add.html", supplierUser, roleIds, rolesList);
        }
        
        Images.Captcha captcha = Images.captcha();
        String password_salt = captcha.getText(6);
        // 密码加密
        supplierUser.encryptedPassword = DigestUtils.md5Hex(supplierUser.encryptedPassword
                + password_salt);
        // 随机吗
        supplierUser.passwordSalt = password_salt;
        supplierUser.lastLoginAt = new Date();
        supplierUser.createdAt = new Date();
        supplierUser.lockVersion = 0;
        supplierUser.supplier = MenuInjector.currentUser().supplier;
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        // 获得IP
        supplierUser.lastLoginIP = request.remoteAddress;
        supplierUser.save();
        index();
    }

    /**
     * 逻辑删除操作员
     */
    public static void delete(Long id) {
        SupplierUser user = SupplierUser.findById(id);
        user.deleted = DeletedStatus.DELETED;
        user.save();
        index();
    }

    /**
     * 操作员编辑页面
     */
    public static void edit(Long id) {
        SupplierUser supplierUser = SupplierUser.findById(id);
        String roleIds = "";
        if (supplierUser.roles != null && !supplierUser.roles.isEmpty()) {
            for (SupplierRole role : supplierUser.roles) {
                roleIds += role.id + ",";
            }
        }

        List rolesList = SupplierRole.findNotAdmin();
        supplierUser.roles.addAll(rolesList);

        render(supplierUser, roleIds, rolesList);
    }

    /**
     * 操作员信息修改
     *
     * @param id   ID
     * @param supplierUser 用户信息
     */
    public static void update(Long id, @Valid SupplierUser supplierUser) {
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (!supplierUser.roles.isEmpty()) {
                for (SupplierRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("SupplierUsers/add.html", supplierUser, roleIds, rolesList);
        }
        // 更新用户信息
        SupplierUser.update(id, supplierUser);

        index();
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static void checkLoginName(Long id, String loginName, String mobile) {
        Long supplierId = MenuInjector.currentUser().supplier.id;
        String returnFlag = SupplierUser.checkValue(id, loginName, mobile, supplierId);
        renderJSON(returnFlag);
    }

}