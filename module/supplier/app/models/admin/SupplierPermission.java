package models.admin;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "supplier_permissions")
public class SupplierPermission extends Model {

    private static final long serialVersionUID = 81890609113062L;

    @Column(name="perm_text")
    public String text;

    @Column(name="perm_key")
    public String key;

    public String description;

    @OrderColumn(name="display_order")
    public Integer displayOrder;
    
    @Column(name="application_name")
    public String applicationName;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "parent_id", nullable = true)
    public SupplierPermission parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, targetEntity = SupplierPermission.class)
    @OrderBy("displayOrder")
    public List<SupplierPermission> children;
    
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_permissions_roles", 
        inverseJoinColumns = @JoinColumn(name= "role_id"), 
        joinColumns = @JoinColumn(name = "permission_id"))
    public Set<SupplierRole> roles;    
  
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_permissions_users", 
        inverseJoinColumns = @JoinColumn(name= "user_id"), 
        joinColumns = @JoinColumn(name = "permission_id"))
    public Set<SupplierRole> users;    
    
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "supplier_navigations_permissions", 
        inverseJoinColumns = @JoinColumn(name= "navigation_id"), 
        joinColumns = @JoinColumn(name = "permission_id"))
    public Set<SupplierNavigation> navigations;    
    
    /**
     * 加载版本，使用应用程序加载时间，在处理完成后，删除不是当前loadVersion的记录，以完成同步.
     */
    @Column(name = "load_version")
    public long loadVersion;

    /**
     * 删除不是当前loadVersion的记录，这样可以保证只有一个版本的rbac.xml的数据在数据库中.
     * @param applicationName
     * @param loadVersion
     */
    public static  void deleteUndefinedPermissions(String applicationName, long loadVersion) {
        List<SupplierPermission> list = SupplierPermission.find(
                "applicationName=? and loadVersion <> ? order by parent DESC, id DESC", 
                applicationName, loadVersion).fetch();
        for (SupplierPermission perm : list) {
            perm.delete();
        }
    }
    
    /**
     * 按用户角色得到权限列表。
     * @param userId
     * @return
     */
    public static List<SupplierPermission> findByUserRole(Long userId) {
        // ""and g.id in (select g.id from g.categories c where c.id = :categoryId)"
        return SupplierPermission.find(
                "select p from SupplierPermission p join p.roles r join r.users u where u.id=?",
                userId).fetch();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierPermission)) return false;
        if (!super.equals(o)) return false;

        SupplierPermission that = (SupplierPermission) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
