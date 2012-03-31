package controllers;

import java.util.Date;
import java.util.List;

import models.admin.OperateRole;
import models.admin.OperateUser;
import operate.rbac.annotations.ActiveNavigation;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import com.uhuila.common.constants.DeletedStatus;

/**
 * 操作员CRUD
 *
 * @author yanjy
 *
 */

@With({OperateRbac.class })
@ActiveNavigation("user_search")
public class OperateUsers extends Controller {
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
        JPAExtPaginator<OperateUser> supplierUsersPage = OperateUser.getSupplierUserList(
                loginName, pageNumber, PAGE_SIZE);
        render(supplierUsersPage,loginName);
    }

    /**
     * 操作员添加页面
     *
     */
    @ActiveNavigation("user_add")
    public static void add() {
        List rolesList = OperateRole.findAll();
        render(rolesList);
    }

    /**
     * 创建操作员
     *
     * @param supplierUser
     *            操作员信息
     */
    @ActiveNavigation("user_add")
    public static void create(@Valid OperateUser supplierUser) {
        if (Validation.hasErrors()) {
            List rolesList = OperateRole.findAll();
            String roleIds = "";
            if (supplierUser.roles != null && !supplierUser.roles.isEmpty()) {
                for (OperateRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("OperateUsers/add.html", supplierUser, roleIds, rolesList);
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
        supplierUser.deleted = DeletedStatus.UN_DELETED;
        // 获得IP
        supplierUser.lastLoginIP = request.remoteAddress;
        supplierUser.save();
        index();
    }

    /**
     * 逻辑删除操作员
     *
     */
    public static void delete(Long id) {
        OperateUser supplierUser = OperateUser.findById(id);
        supplierUser.deleted = DeletedStatus.DELETED;
        supplierUser.save();
        index();
    }

    /**
     * 操作员编辑页面
     *
     */
    public static void edit(Long id) {
        OperateUser supplierUser = OperateUser.findById(id);
        String roleIds = "";
        if (supplierUser.roles != null && !supplierUser.roles.isEmpty()) {
            for (OperateRole role : supplierUser.roles) {
                roleIds += role.id + ",";
            }
        }

        List rolesList = OperateRole.findAll();
        supplierUser.roles.addAll(rolesList);

        render(supplierUser, roleIds, rolesList);
    }

    /**
     * 操作员信息修改
     *
     * @param id
     *            ID
     * @param supplierUser
     *            用户信息
     */
    public static void update(Long id, @Valid OperateUser supplierUser) {
        if (Validation.hasErrors()) {
            List rolesList = OperateRole.findAll();
            String roleIds = "";
            if (!supplierUser.roles.isEmpty()) {
                for (OperateRole role : supplierUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("OperateUsers/add.html", supplierUser, roleIds, rolesList);
        }
        // 更新用户信息
        OperateUser.update(id, supplierUser);

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
        String returnFlag = OperateUser.checkValue(id,loginName, mobile);
        renderJSON(returnFlag);
    }

}