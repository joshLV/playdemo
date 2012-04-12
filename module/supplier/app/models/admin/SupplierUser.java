package models.admin;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.modules.view_ext.annotation.Mobile;
import play.mvc.Http.Request;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "supplier_users")
public class SupplierUser extends Model {
	@Column(name = "login_name")
	@Required
	public String loginName;

	@Required
	@Mobile
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

	@Column(name = "user_name")
	public String userName;
	
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
	@OrderBy("id")
	public List<SupplierRole> roles;

	@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	@JoinTable(name = "supplier_permissions_users",
	inverseJoinColumns = @JoinColumn(name = "permission_id"),
	joinColumns = @JoinColumn(name = "user_id"))
	public Set<SupplierPermission> permissions;

	/**
	 * 查询操作员信息
	 * 
	 * @param loginName 用户名
	 * @param pageNumber 页数
	 * @param pageSize 记录数
	 * @return 操作员信息
	 */
	public static JPAExtPaginator<SupplierUser> getSupplierUserList(String loginName, Long supplierId,
			int pageNumber, int pageSize) {
		StringBuffer sql = new StringBuffer();
		Map params = new HashMap();
		sql.append("supplier.id = :supplierId");
		params.put("supplierId", supplierId);
		
//		sql.append(" and loginName <> 'admin' ");
		
		sql.append(" and deleted = :deleted ");
		params.put("deleted", DeletedStatus.UN_DELETED);

		if (StringUtils.isNotBlank(loginName)) {
			sql.append(" and loginName like :loginName");
			params.put("loginName", loginName + "%");
		}

		JPAExtPaginator<SupplierUser> usersPage = new JPAExtPaginator<>("SupplierUser s", "s",
				SupplierUser.class, sql.toString(), params).orderBy("createdAt desc");
		usersPage.setPageNumber(pageNumber);
		usersPage.setPageSize(pageSize);
		usersPage.setBoundaryControlsEnabled(false);
		return usersPage;
	}

	/**
	 * 更新操作员信息
	 *
	 * @param id    ID
	 * @param user 用户信息
	 */
	public static void update(long id, SupplierUser supplierUser) {
		SupplierUser updatedUser = SupplierUser.findById(id);
		if (StringUtils.isNotEmpty(supplierUser.encryptedPassword) && 
				!"******".equals(supplierUser.encryptedPassword) && 
				!supplierUser.encryptedPassword.equals(DigestUtils.md5Hex(updatedUser.encryptedPassword))) {
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
	 * @param supplierUserId
	 */
	public static String checkValue(Long id, String loginName, String mobile, Long supplierId) {

		StringBuilder sq = new StringBuilder("loginName = ? and supplier=? ");
		List params = new ArrayList();
		params.add(loginName);
		params.add(new Supplier(supplierId));
		if (id != null) {
			sq.append("and id <> ?");
			params.add(id);
		}
		List<SupplierUser> supplierUserList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
		String returnFlag = "0";
		//用户名存在的情况
		if (supplierUserList.size() > 0) returnFlag = "1";
		else {
			sq = new StringBuilder("mobile = ? ");
			params = new ArrayList();
			params.add(mobile);
			if (id != null) {
				sq.append("and id <> ?");
				params.add(id);
			}
			//手机存在的情况
			List<SupplierUser> mList = SupplierUser.find(sq.toString(), params.toArray()).fetch();
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
		// 随机码
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
		Supplier supplier = Supplier.find("byDomainName", domainName).first();
		if (supplier == null) {
			return null;
		}

		List<SupplierUser> all = SupplierUser.findAll();
		for (SupplierUser user : all) {
			Logger.info("  ----- user.id:" + user.id + ", supplierId:" + user.supplier.id + ", loginName:" + user.loginName);
		}

		return SupplierUser.find("bySupplierAndLoginName", supplier, loginName).first();
	}

}
