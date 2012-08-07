package models.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import play.db.jpa.Model;

@Entity
@Table(name = "supplier_roles")
public class SupplierRole extends Model {

    private static final long serialVersionUID = 84730609113062L;

    @Column(name="role_text")
    public String text;

    @Column(name="role_key")
    public String key;

    public String description;

    /**
     * 加载版本，使用应用程序加载时间，在处理完成后，删除不是当前loadVersion的记录，以完成同步.
     */
    @Column(name = "load_version")
    public long loadVersion;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_users_roles", 
        inverseJoinColumns = @JoinColumn(name= "user_id"), 
        joinColumns = @JoinColumn(name = "role_id"))
    public Set<SupplierUser> users;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_permissions_roles", 
        inverseJoinColumns = @JoinColumn(name= "permission_id"), 
        joinColumns = @JoinColumn(name = "role_id"))
    public Set<SupplierPermission> permissions;   
    
    public static SupplierRole findByKey(String key){
        return SupplierRole.find("byKey",key).first();
    }

    public List<SupplierPermission> getSortedPermissions() {
        List<SupplierPermission> sortedPermissions = new ArrayList<>();
        sortedPermissions.addAll(permissions);
        Collections.sort(sortedPermissions, new Comparator<SupplierPermission>() {
            @Override
            public int compare(SupplierPermission o1, SupplierPermission o2) {
                return (int) (o1.id - o2.id);
            }
        });
        return sortedPermissions;
    }
    
    /**
     * 查询系统管理员以外的角色
     * @return
     */
	public static List findRoleOrderById() {
		List rolesList = SupplierRole.find(" order by id").fetch();
		return rolesList;
	}
    
}

