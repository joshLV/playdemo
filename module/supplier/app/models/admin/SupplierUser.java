package models.admin;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.RandomNumberUtil;
import models.accounts.Account;
import models.accounts.util.AccountUtil;
import models.sales.Shop;
import models.supplier.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;

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
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "supplier_users")
public class SupplierUser extends Model {

    private static final long serialVersionUID = 812328646862L;

    @Column(name = "login_name")
    @Required
    public String loginName;

    @Required
    @Mobile
    public String mobile;

    /**
     * 工号.
     * 用于短信验证方式，消费者向指定号码回复工号，即可完成验证。
     * TODO: 员工工号需要保证在同一个Supplier中唯一
     */
    @Column(name = "job_number")
    @Required
    @MaxSize(value = 6)
    public String jobNumber;

    @Column(name = "encrypted_password")
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

    @Column(name = "user_name")
    public String userName;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    @Transient
    public String confirmPassword;

    @Transient
    public String oldPassword;

    @OneToOne
    @JoinColumn(name = "shop_id", nullable = true)
    public Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_user_type")
    public SupplierUserType supplierUserType;

    /**
     * 记录最后一次使用的shopId.
     */
    @Column(name = "last_shop_id")
    public Long lastShopId;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_users_roles",
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    @OrderBy("id")
    public List<SupplierRole> roles;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_permissions_users",
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    public Set<SupplierPermission> permissions;

    @Column(name = "default_ui_version")
    public String defaultUiVersion;

    /**
     * 识别码。
     * 用于标识一个用户，在关联微信，微博时使用。
     * 注意这个值会是一次性的使用，在绑定成功后会消失.
     */
    @Column(name = "idCode", unique = true)
    public String idCode;

    /**
     * 微信OpenId.
     * 通过OpenId，我们可以识别微信发起者对应的SupplierUser.
     */
    @Column(name = "weixin_open_id")
    public String weixinOpenId;


    public SupplierUser() {
        supplierUserType = SupplierUserType.HUMAN;
        createdAt = new Date();
    }

    /**
     * 查询操作员信息
     *
     * @param loginName  用户名
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return 操作员信息
     */
    public static JPAExtPaginator<SupplierUser> getSupplierUserList(String loginName, String userName, String jobNumber,
                                                                    Long supplierId, Long shopId,
                                                                    int pageNumber, int pageSize) {
        return getSupplierUserList(SupplierUserType.HUMAN, loginName, userName, jobNumber, supplierId, shopId, pageNumber, pageSize);
    }

    /**
     * 查询操作员信息
     *
     * @param loginName  用户名
     * @param pageNumber 页数
     * @param pageSize   记录数
     * @return 操作员信息
     */
    public static JPAExtPaginator<SupplierUser> getSupplierUserList(SupplierUserType type,
                                                                    String loginName, String userName,
                                                                    String jobNumber, Long supplierId,
                                                                    Long shopId,
                                                                    int pageNumber, int pageSize) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        if (supplierId != null && supplierId > 0) {
            sql.append(" and supplier.id = :supplierId");
            params.put("supplierId", supplierId);
        }
        if (shopId != null && shopId > 0) {
            sql.append(" and shop.id = :shopId");
            params.put("shopId", shopId);
        }

        if (type != null) {
            if (type == SupplierUserType.HUMAN) {
                sql.append(" and (s.supplierUserType = :supplierUserType or s.supplierUserType is null)");
            } else {
                sql.append(" and s.supplierUserType = :supplierUserType");
            }
            params.put("supplierUserType", type);
        }


        sql.append(" and deleted = :deleted ");
        params.put("deleted", DeletedStatus.UN_DELETED);

        if (StringUtils.isNotBlank(loginName)) {
            sql.append(" and loginName like :loginName");
            params.put("loginName", "%" + loginName + "%");
        }
        if (StringUtils.isNotBlank(userName)) {
            sql.append(" and userName like :userName");
            params.put("userName", userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            sql.append(" and jobNumber =:jobNumber");
            params.put("jobNumber", jobNumber);
        }
        JPAExtPaginator<SupplierUser> usersPage = new JPAExtPaginator<>("SupplierUser s", "s",
                SupplierUser.class, sql.toString(), params).orderBy("createdAt desc");
        usersPage.setPageNumber(pageNumber);
        usersPage.setPageSize(pageSize);
        return usersPage;
    }


    /**
     * 查询操作员信息
     *
     * @param supplierId 商户标识
     * @return 操作员信息
     */
    public static List<SupplierUser> findBySupplier(Long supplierId) {
        return find("supplier.id=? and deleted=? and (supplierUserType=? or supplierUserType is null)", supplierId, DeletedStatus.UN_DELETED, SupplierUserType.HUMAN).fetch();
    }

    /**
     * 更新操作员信息
     *
     * @param id           ID
     * @param supplierUser 用户信息
     */
    public static void update(long id, SupplierUser supplierUser) {
        SupplierUser updatedUser = SupplierUser.findById(id);
        String updatedUser_encryptedPassword =
                StringUtils.isNotEmpty(updatedUser.encryptedPassword) ?
                        updatedUser.encryptedPassword : "!&NOTSETPASSWORD!";

        if (StringUtils.isNotEmpty(supplierUser.encryptedPassword) &&
                !"******".equals(supplierUser.encryptedPassword) &&
                !supplierUser.encryptedPassword.equals(DigestUtils.md5Hex(updatedUser_encryptedPassword))) {
            Images.Captcha captcha = Images.captcha();
            String passwordSalt = captcha.getText(6);
            //随机码
            updatedUser.passwordSalt = passwordSalt;
            updatedUser.encryptedPassword = DigestUtils.md5Hex(supplierUser.encryptedPassword + passwordSalt);
        }
        updatedUser.roles = supplierUser.roles;
        updatedUser.loginName = supplierUser.loginName;
        updatedUser.userName = supplierUser.userName;
        updatedUser.mobile = supplierUser.mobile;
        updatedUser.shop = supplierUser == null ? null : supplierUser.shop;
        updatedUser.updatedAt = new Date();
        updatedUser.jobNumber = supplierUser.jobNumber;

        updatedUser.save();
    }

    /**
     * 判断用户名和手机是否唯一
     *
     * @param loginName  用户名
     * @param mobile     手机
     * @param supplierId
     */
    public static String checkValue(Long id, String loginName, String mobile, String jobNumber, Long supplierId) {
        StringBuilder sq = new StringBuilder("deleted =? and loginName = ? and supplier=? ");
        List params = new ArrayList();
        params.add(DeletedStatus.UN_DELETED);
        params.add(loginName);
        Supplier supplier = new Supplier();
        supplier.id = supplierId;
        params.add(supplier);
        if (id != null) {
            sq.append("and id <> ?");
            params.add(id);
        }
        List<SupplierUser> supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
        //用户名存在的情况
        if (supplierUserList.size() > 0) {
            return "1";
        }
        sq = new StringBuilder("deleted=? and mobile = ? and supplier=? ");
        params.clear();
        params.add(DeletedStatus.UN_DELETED);
        params.add(mobile);
        params.add(supplier);
        if (id != null) {
            sq.append("and id <> ?");
            params.add(id);
        }
        //手机存在的情况
        supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
        if (supplierUserList.size() > 0) {
            return "2";
        }
        //工号存在
        sq = new StringBuilder("deleted=? and jobNumber = ? and supplier=? ");
        params.clear();
        params.add(DeletedStatus.UN_DELETED);
        params.add(jobNumber);
        params.add(supplier);
        if (id != null) {
            sq.append("and id <> ?");
            params.add(id);
        }
        supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
        if (supplierUserList.size() > 0) {
            return "3";
        }

        return "0";
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
        // 随机码
        passwordSalt = password_salt;
        lockVersion = 0;

        this.supplier = supplier;
        this.loginName = this.loginName.toLowerCase().trim();

        deleted = DeletedStatus.UN_DELETED;
        return super.create();
    }

    // FIXME: findAdmin这个名字，是指只找Admin用户？这个应该是findUser
    public static SupplierUser findAdmin(Long supplierId, String admin) {
        Supplier supplier = Supplier.findById(supplierId);
        return find("bySupplierAndLoginNameAndDeleted", supplier, admin, DeletedStatus.UN_DELETED).first();
    }

    public static SupplierUser findUserByDomainName(String domainName, String loginName) {
        Supplier supplier = Supplier.find("byDomainNameAndDeleted", domainName, DeletedStatus.UN_DELETED).first();
        if (supplier == null) {
            return null;
        }
        return SupplierUser.find("bySupplierAndLoginNameAndDeleted", supplier, loginName, DeletedStatus.UN_DELETED).first();
    }

    /**
     * 修改密码
     *
     * @param newUser  新用户
     * @param password 密码
     */
    public static void updatePassword(SupplierUser newUser, String password) {
        // 随机码
        Images.Captcha captcha = Images.captcha();
        String newPasswordSalt = captcha.getText(6);
        newUser.passwordSalt = newPasswordSalt;
        // 新密码
        newUser.encryptedPassword = DigestUtils.md5Hex(password + newPasswordSalt);
        newUser.save();

    }

    /**
     * 验证手机是否存在
     *
     * @param mobile
     * @return
     */
    public static boolean checkMobile(String mobile) {
        SupplierUser supplierUser = SupplierUser.find("mobile = ? and deleted = ?", mobile, DeletedStatus.UN_DELETED).first();
        if (supplierUser != null) {
            return true;
        }
        return false;
    }

    /**
     * 找回密码，更新新密码
     *
     * @param supplierUserId
     * @param mobile
     * @param password
     */
    public static void updateFindPwd(Long supplierUserId, String mobile,
                                     String password) {
        SupplierUser supplierUser = SupplierUser.find("id = ? and mobile = ?", supplierUserId, mobile).first();
        if (supplierUser != null) {
            Images.Captcha captcha = Images.captcha();
            String password_salt = captcha.getText(6);
            // 密码加密
            supplierUser.encryptedPassword = DigestUtils.md5Hex(password
                    + password_salt);
            // 随机码
            supplierUser.passwordSalt = password_salt;
            supplierUser.save();
        }

    }

    /**
     * 查询操作员信息
     *
     * @param mobile   手机
     * @param supplier 商户ID
     * @return 操作员
     */
    public static SupplierUser findByMobileAndSupplier(String mobile, Supplier supplier) {
        SupplierUser supplierUser = SupplierUser.find("deleted = ? and mobile = ? and supplier = ?",
                DeletedStatus.UN_DELETED, mobile, supplier).first();
        return supplierUser;
    }

    public static SupplierUser findByUnDeletedId(Long id) {
        SupplierUser supplierUser = SupplierUser.find("deleted = ? and id = ?",
                DeletedStatus.UN_DELETED, id).first();
        return supplierUser;
    }

    public Account getSupplierAccount() {
        if (shop != null && shop.independentClearing) {
            return AccountUtil.getShopAccount(shop.id);
        }
        if (supplier != null) {
            return AccountUtil.getSupplierAccount(supplier.id);
        }
        return null;
    }

    /**
     * 生成商户操作员唯一的识别码.
     */
    public static String generateAvailableIdCode() {
        String randomNumber;
        do {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // do nothing.
            }
            randomNumber = RandomNumberUtil.generateSerialNumber(6);
        } while (isNotUniqueIdCode(randomNumber));
        return randomNumber;
    }

    private static boolean isNotUniqueIdCode(String randomNumber) {
        return SupplierUser.find("from SupplierUser where idCode=?", randomNumber).fetch().size() > 0;
    }
}
