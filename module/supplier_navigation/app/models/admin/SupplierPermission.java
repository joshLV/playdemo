package models.admin;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "supplier_permissions")
public class SupplierPermission extends Model {

    public String name;

    public String key;

    public String description;

    @Column(name="application_name")
    public String applicationName;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    /**
     * 加载版本，使用应用程序加载时间，在处理完成后，删除不是当前loadVersion的记录，以完成同步.
     */
    @Column(name = "load_version")
    public long loadVersion;


    /**
     * 删除不是当前loadVersion的记录，这样可以保证只有一个版本的permission.yml的数据在数据库中.
     * @param applicationName
     * @param loadVersion
     */
    public void clearUndefinedNavigation(String applicationName, long loadVersion) {
        List<SupplierPermission> list = SupplierPermission.find("where applicationName=? and loadVersion<>?", applicationName, loadVersion).fetch();
        for (SupplierPermission nav : list) {
            SupplierPermission.em().remove(nav);
        }
    }    
}
