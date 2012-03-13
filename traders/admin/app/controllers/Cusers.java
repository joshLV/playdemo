package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import models.admin.SupplierRole;
import models.admin.SupplierUser;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import navigation.annotations.ActiveNavigation;
import navigation.annotations.Right;
import play.mvc.With;

import com.uhuila.common.constants.DeletedStatus;

/**
 * 操作员CRUD
 *
 * @author yanjy
 *
 */

@With({MenuInjector.class})
@ActiveNavigation("user")
public class Cusers extends Controller{
    public static int PAGE_SIZE = 15;
    public static Long companyId=2l;

    /**
     * 操作员一览
     *
     */
    public static void index() {
        String page = request.params.get("page");
        String loginName = request.params.get("loginName");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<SupplierUser> cusersPage = SupplierUser.getCuserList(loginName,companyId,pageNumber,PAGE_SIZE);
        render(cusersPage);
    }


    /**
     * 操作员添加页面
     *
     */
    public static void add() {
        List rolesList = SupplierRole.findAll();
        render(rolesList);
    }

    /**
     * 创建操作员
     *
     * @param cuser 操作员信息
     * @param role 角色ID
     */
    public static void create(@Valid SupplierUser cuser) {
        if (Validation.hasErrors()) {
            List rolesList=SupplierRole.findAll();
            String roleIds = "";
            if(!cuser.roles.isEmpty()){
                for (SupplierRole role:cuser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("Cusers/add.html",cuser,roleIds,rolesList);
        }
        Images.Captcha captcha = Images.captcha();
        String password_salt =captcha.getText(6);
        //密码加密
        cuser.encryptedPassword = DigestUtils.md5Hex(cuser.encryptedPassword+password_salt);
        //随机吗
        cuser.passwordSalt = password_salt;
        cuser.lastLoginAt = new Date();
        cuser.createdAt = new Date();
        cuser.lockVersion = 0;
        cuser.companyId = companyId;
        cuser.deleted = DeletedStatus.UN_DELETED;
        //获得本机IP
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            cuser.lastLoginIP = ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
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
        if(!cuser.roles.isEmpty()){
            for (SupplierRole role:cuser.roles) {
                roleIds += role.id + ",";
            }
        }

        List rolesList = SupplierRole.findAll();
        cuser.roles.addAll(rolesList);

        render(cuser,roleIds,rolesList);
    }

    /**
     * 操作员信息修改
     *
     */
    public static void update(Long id,@Valid SupplierUser cuser) {
        if (Validation.hasErrors()) {
            List rolesList=SupplierRole.findAll();
            String roleIds = "";
            System.out.println("&&&&&&&&&&&"+cuser);
            if(!cuser.roles.isEmpty()){
                for (SupplierRole role:cuser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("Cusers/add.html",cuser,roleIds,rolesList);
            return;
        }
        SupplierUser updCuser = SupplierUser.findById(id);
        updCuser.loginName=cuser.loginName;
        updCuser.mobile=cuser.mobile;
        if (!cuser.encryptedPassword.equals( DigestUtils.md5Hex(updCuser.encryptedPassword))) {
            Images.Captcha captcha = Images.captcha();
            String passwordSalt =captcha.getText(6);
            //随机吗
            updCuser.passwordSalt = passwordSalt;
            updCuser.encryptedPassword = DigestUtils.md5Hex(cuser.encryptedPassword+passwordSalt);
        }
        updCuser.roles=cuser.roles;
        updCuser.lastLoginAt = new Date();
        updCuser.updatedAt = new Date();
        //获得本机IP
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            updCuser.lastLoginIP = ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        updCuser.save();
        index();
    }


    /**
     * 判断用户名手机是否唯一
     *
     * @param loginName
     */
    public static void checkLoginName(String loginName,String mobile) {
        List<SupplierUser> cuserList = SupplierUser.find("byLoginName", loginName).fetch();
        String returnFlag = "0";
        //用户名存在的情况
        if (cuserList.size() >0) returnFlag = "1";
        else {
            //手机存在的情况
            List<SupplierUser> mList = SupplierUser.find("byMobile", mobile).fetch();
            if(mList.size()>0) returnFlag = "2";
        }

        renderJSON(returnFlag);
    }

}
