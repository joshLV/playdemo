package controllers;

import java.util.Date;
import java.util.List;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;
import com.uhuila.common.constants.DeletedStatus;
import controllers.supplier.cas.SecureCAS;

/**
 * 操作员CRUD
 * 
 * @author yanjy
 * 
 */

@With({SecureCAS.class, MenuInjector.class })
@ActiveNavigation("user_search")
public class SupplierUsers extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 操作员一览
     * 
     */
    @ActiveNavigation("user_search")
    public static void index() {
        String page = request.params.get("page");
        String loginName = request.params.get("loginName");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<SupplierUser> cusersPage = SupplierUser.getCuserList(
                loginName, MenuInjector.currentUser().supplier.id, pageNumber, PAGE_SIZE);
        render(cusersPage);
    }

    /**
     * 操作员添加页面
     * 
     */
    @ActiveNavigation("user_add")
    public static void add() {
        List rolesList = SupplierRole.findAll();
        render(rolesList);
    }

    /**
     * 创建操作员
     * 
     * @param cuser
     *            操作员信息
     * @param role
     *            角色ID
     */
    @ActiveNavigation("user_add")
    public static void create(@Valid SupplierUser cuser) {
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (cuser.roles != null && !cuser.roles.isEmpty()) {
                for (SupplierRole role : cuser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("SupplierUsers/add.html", cuser, roleIds, rolesList);
        }
        Images.Captcha captcha = Images.captcha();
        String password_salt = captcha.getText(6);
        // 密码加密
        cuser.encryptedPassword = DigestUtils.md5Hex(cuser.encryptedPassword
                + password_salt);
        // 随机吗
        cuser.passwordSalt = password_salt;
        cuser.lastLoginAt = new Date();
        cuser.createdAt = new Date();
        cuser.lockVersion = 0;
        cuser.supplier = MenuInjector.currentUser().supplier;
        cuser.deleted = DeletedStatus.UN_DELETED;
        // 获得IP
        cuser.lastLoginIP = request.remoteAddress;
        cuser.save();
        index();
    }

    /**
     * 逻辑删除操作员
     * 
     */
    public static void delete(Long id) {
        SupplierUser cuser = SupplierUser.findById(id);
        cuser.deleted = DeletedStatus.DELETED;
        cuser.save();
        index();
    }

    /**
     * 操作员编辑页面
     * 
     */
    public static void edit(Long id) {
        SupplierUser cuser = SupplierUser.findById(id);
        String roleIds = "";
        if (cuser.roles != null && !cuser.roles.isEmpty()) {
            for (SupplierRole role : cuser.roles) {
                roleIds += role.id + ",";
            }
        }

        List rolesList = SupplierRole.findAll();
        cuser.roles.addAll(rolesList);

        render(cuser, roleIds, rolesList);
    }

    /**
     * 操作员信息修改
     * 
     * @param id
     *            ID
     * @param cuser
     *            用户信息
     */
    public static void update(Long id, @Valid SupplierUser cuser) {
        if (Validation.hasErrors()) {
            List rolesList = SupplierRole.findAll();
            String roleIds = "";
            if (!cuser.roles.isEmpty()) {
                for (SupplierRole role : cuser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("SupplierUsers/add.html", cuser, roleIds, rolesList);
            return;
        }
        // 更新用户信息
        SupplierUser.update(id, cuser);

        index();
    }

    /**
     * 判断用户名和手机是否唯一
     * 
     * @param loginName
     *            用户名
     * @param mobile
     *            手机
     */
    public static void checkLoginName(Long id,String loginName, String mobile) {
        String returnFlag = SupplierUser.checkValue(id,loginName, mobile);
        renderJSON(returnFlag);
    }

}