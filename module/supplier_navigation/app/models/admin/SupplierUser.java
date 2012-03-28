package models.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import models.supplier.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Match;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Request;
import com.uhuila.common.constants.DeletedStatus;

@Entity
@Table(name = "supplier_users")
public class SupplierUser extends Model {
    @Column(name = "login_name")
    @Required
    public String loginName;

    @Required
    @Match(value = "^1[3|4|5|8][0-9]\\d{4,8}$", message = "手机格式不对！")
    public String mobile;

    @Column(name = "encrypted_password")
    @Required
    public String encryptedPassword;

    @Column(name = "password_salt")
    public String passwordSalt;

    @Column(name = "last_login_ip")
    public String lastLoginIP;

    @Column(name = "last_login_at")
    public Date lastLoginAt;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;
	
	@ManyToOne
	@JoinColumn(name="supplier_id")
	public Supplier supplier;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_users_roles",
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    public Set<SupplierRole> roles;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_permissions_users",
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    public Set<SupplierPermission> permissions;

    public static JPAExtPaginator<SupplierUser> getCuserList(String loginName, Long supplierId,
                                                             int pageNumber, int pageSize) {
        StringBuffer sql = new StringBuffer();
        Map params = new HashMap();
        sql.append("supplier.id = :supplierId");
        params.put("supplierId", supplierId);

        sql.append(" and deleted = :deleted ");
        params.put("deleted", DeletedStatus.UN_DELETED);

        if (StringUtils.isNotBlank(loginName)) {
            sql.append(" and loginName like :loginName");
            params.put("loginName", loginName + "%");
        }

        JPAExtPaginator<SupplierUser> cusersPage = new JPAExtPaginator<>("SupplierUser s", "s",
                SupplierUser.class, sql.toString(), params).orderBy("createdAt desc");
        cusersPage.setPageNumber(pageNumber);
        cusersPage.setPageSize(pageSize);
        cusersPage.setBoundaryControlsEnabled(false);
        return cusersPage;
    }

    /**
     * 更新操作员信息
     *
     * @param id    ID
     * @param user 用户信息
     */
    public static void update(long id, SupplierUser user) {
        SupplierUser updatedUser = SupplierUser.findById(id);
        if (StringUtils.isNotEmpty(user.encryptedPassword) && !user.encryptedPassword.equals(DigestUtils.md5Hex(updatedUser.encryptedPassword))) {
            Images.Captcha captcha = Images.captcha();
            String passwordSalt = captcha.getText(6);
            //随机码
            updatedUser.passwordSalt = passwordSalt;
            updatedUser.encryptedPassword = DigestUtils.md5Hex(user.encryptedPassword + passwordSalt);
        }
        updatedUser.loginName = user.loginName;
        updatedUser.mobile = user.mobile;
        updatedUser.lastLoginAt = new Date();
        updatedUser.updatedAt = new Date();
        //获得IP
        updatedUser.lastLoginIP = Request.current().remoteAddress;

        updatedUser.save();
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName 用户名
     * @param mobile    手机
     */
    public static String checkValue(Long id, String loginName, String mobile) {

        StringBuilder sq = new StringBuilder("loginName = ? ");
        List list = new ArrayList();
        list.add(loginName);
        if (id != null) {
            sq.append("and id <> ?");
            list.add(id);
        }
        List<SupplierUser> cuserList = SupplierUser.find(sq.toString(), list.toArray()).fetch();
        String returnFlag = "0";
        //用户名存在的情况
        if (cuserList.size() > 0) returnFlag = "1";
        else {
            sq = new StringBuilder("mobile = ? ");
            list = new ArrayList();
            list.add(mobile);
            if (id != null) {
                sq.append("and id <> ?");
                list.add(id);
            }
            //手机存在的情况
            List<SupplierUser> mList = SupplierUser.find(sq.toString(), list.toArray()).fetch();
            if (mList.size() > 0) returnFlag = "2";
        }

        return returnFlag;
    }

    /**
     * 创建用户信息
     *
     * @param supplierId
     * @return
     */
    public boolean create(Long supplierId) {
        Images.Captcha captcha = Images.captcha();
        Supplier supplier = Supplier.findById(supplierId);
        String password_salt = captcha.getText(6);
        // 密码加密
        encryptedPassword = DigestUtils.md5Hex(encryptedPassword
                + password_salt);
        // 随机吗
        passwordSalt = password_salt;
        createdAt = new Date();
        lockVersion = 0;
        this.supplier = supplier;
        deleted = DeletedStatus.UN_DELETED;
        // 获得IP
        lastLoginIP = Request.current().remoteAddress;
        return super.create();
    }

    // FIXME: findAdmin这个名字，是指只找Admin用户？这个应该是findUser
    public static SupplierUser findAdmin(Long supplierId, String admin) {
        Supplier supplier = Supplier.findById(supplierId);
        return find("bySupplierAndLoginName", supplier, admin).first();
    }
    
    public static SupplierUser findUserByDomainName(String domainName, String loginName) {
        System.out.println("domainName=" + domainName + ", loginName=" + loginName + " ^^^^^^^^^^^^^^^^");
        Supplier supplier = Supplier.find("byDomainName", domainName).first();
        if (supplier == null) {
            System.out.println("&&&&&&&&&&&&&&&&  domain is null");
            return null;
        }
        
        System.out.println("================");
        
        List<SupplierUser> all = SupplierUser.findAll();
        for (SupplierUser user : all) {
            System.out.println("  ----- user.id:" + user.id + ", supplierId:" + user.supplier.id + ", loginName:" + user.loginName);
        }
        
        System.out.println("================");
        System.out.println("     ! -------------- supplier: " + supplier.fullName + ", supplerId=" + supplier.id + ", loginName" + loginName);
        return SupplierUser.find("bySupplierAndLoginName", supplier, loginName).first();
    }
    
}
