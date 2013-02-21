package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.operator.OperateRole;
import models.operator.OperateUser;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 操作员CRUD
 *
 * @author yanjy
 */
@With(OperateRbac.class)
@ActiveNavigation("user_search")
public class OperateUsers extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 操作员一览
     */
    @ActiveNavigation("user_search")
    public static void index() {
        String page = request.params.get("page");
        String loginName = request.params.get("loginName");
        Long id = OperateRbac.currentUser().id;
        OperateUser operateUser = OperateUser.findById(id);

        List<String> keyList = new ArrayList<String>();
        for (OperateRole role : operateUser.roles) {
            keyList.add(role.key);
        }

        if (!keyList.contains("admin")) {
            redirect("/profile");
        }
        for (String s : keyList) {
            System.out.println("s:" + s);
        }

        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<OperateUser> operateUserPage = OperateUser.getSupplierUserList(
                loginName, pageNumber, PAGE_SIZE);

        render(operateUserPage, loginName);

    }

    /**
     * 操作员添加页面
     */
    @ActiveNavigation("user_add")
    public static void add() {
        List rolesList = OperateRole.findAll();
        render(rolesList);
    }

    /**
     * 创建操作员
     *
     * @param operateUser 操作员信息
     */
    @ActiveNavigation("user_add")
    public static void create(@Valid OperateUser operateUser) {
        if (operateUser.roles == null) {
            Validation.addError("operateUser.roles", "validation.selected");
        }
        if (Validation.hasErrors()) {
            List rolesList = OperateRole.findAll();
            String roleIds = getRoleIds(operateUser.roles);
            render("OperateUsers/add.html", operateUser, roleIds, rolesList);
        }
        Images.Captcha captcha = Images.captcha();
        String password_salt = captcha.getText(6);
        // 密码加密
        operateUser.encryptedPassword = DigestUtils.md5Hex(operateUser.encryptedPassword
                + password_salt);
        // 随机吗
        operateUser.passwordSalt = password_salt;
        operateUser.lastLoginAt = new Date();
        operateUser.createdAt = new Date();
        operateUser.lockVersion = 0;
        operateUser.deleted = DeletedStatus.UN_DELETED;
        // 获得IP
        operateUser.lastLoginIP = request.remoteAddress;
        operateUser.save();
        index();
    }

    /**
     * 逻辑删除操作员
     */
    @ActiveNavigation("user_add")
    public static void delete(Long id) {
        OperateUser supplierUser = OperateUser.findById(id);
        supplierUser.deleted = DeletedStatus.DELETED;
        supplierUser.save();
        index();
    }

    /**
     * 操作员编辑页面
     */
    @ActiveNavigation("user_add")
    public static void edit(Long id) {
        OperateUser operateUser = OperateUser.findById(id);
        Set<Long> roleIds = getRoleIdSet(operateUser.roles);

        List rolesList = OperateRole.findAll();
        operateUser.roles.addAll(rolesList);

        render(operateUser, roleIds, rolesList);
    }

    /**
     * 操作员信息修改
     *
     * @param id          ID
     * @param operateUser 用户信息
     */
    @ActiveNavigation("user_add")
    public static void update(Long id, @Valid OperateUser operateUser) {
        if (operateUser.roles == null) {
            Validation.addError("operateUser.roles", "validation.selected");
        }

        if (Validation.hasErrors()) {
            List rolesList = OperateRole.findAll();
            Set<Long> roleIds = getRoleIdSet(operateUser.roles);
            operateUser.id = id;
            render("OperateUsers/edit.html", operateUser, roleIds, rolesList);
        }
        // 更新用户信息
        OperateUser.update(id, operateUser);

        index();
    }


    private static String getRoleIds(List<OperateRole> roles) {
        String roleIds = "";
        if (roles != null && !roles.isEmpty()) {
            for (OperateRole role : roles) {
                if (role != null) {
                    roleIds += role.id + ",";
                }
            }
        }
        return roleIds;
    }

    private static Set<Long> getRoleIdSet(List<OperateRole> roles) {
        Set<Long> roleIds = new HashSet<>();
        if (roles != null && !roles.isEmpty()) {
            for (OperateRole role : roles) {
                if (role != null) {
                    roleIds.add(role.id);
                }
            }
        }
        return roleIds;
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param id        OperateUser的id
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static void checkLoginName(Long id, String loginName, String mobile) {
        String returnFlag = OperateUser.checkValue(id, loginName, mobile);
        renderJSON(returnFlag);
    }

}