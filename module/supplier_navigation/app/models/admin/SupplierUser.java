package models.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Match;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import com.uhuila.common.constants.DeletedStatus;

@Entity
@Table(name = "cusers")
public class SupplierUser extends Model {
	@Column(name = "company_id")
	public Long companyId;

	@Column(name = "login_name")
	@Required
	public String loginName;

	@Required
	@Match(value="^1[3|4|5|8][0-9]\\d{4,8}$",message="手机格式不对！")
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

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "cusers_roles", inverseJoinColumns = @JoinColumn(name
			= "role_id"), joinColumns = @JoinColumn(name = "cuser_id"))
	public Set<SupplierRole> roles;

	public static JPAExtPaginator<SupplierUser> getCuserList(String loginName,Long companyId,
			int pageNumber, int pageSize) {
		StringBuffer sql = new StringBuffer();
		Map params= new HashMap();
		sql.append("companyId = :companyId");
		params.put("companyId",companyId);
		
		sql.append(" and deleted = :deleted ");
		params.put("deleted", DeletedStatus.UN_DELETED);
		
		if (StringUtils.isNotBlank(loginName)) {
			sql.append(" and loginName like :loginName");
			params.put("loginName",loginName+"%");
		}
	
		JPAExtPaginator<SupplierUser> cusersPage = new JPAExtPaginator<>("SupplierUser s", "s", 
				SupplierUser.class,sql.toString(),params).orderBy("createdAt desc");
		cusersPage.setPageNumber(pageNumber);
		cusersPage.setPageSize(pageSize);
		cusersPage.setBoundaryControlsEnabled(false);
		return cusersPage;
	}


}
