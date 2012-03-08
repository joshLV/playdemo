package models.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.Play;
import play.db.jpa.Model;

@Entity
@Table(name = "supplier_navigations")
public class SupplierNavigation extends Model {

    public String name;
    
    public String text;

    public String description;

    public String action;

    public String url;

    @Column(name="application_name")
    public String applicationName;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    public SupplierNavigation parent;

    public boolean hasLink() {
        return url != null || action != null;
    }

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

    /**
     * 删除不是当前loadVersion的记录，这样可以保证只有一个版本的navigation.yml的数据在数据库中.
     * @param applicationName
     * @param loadVersion
     */
    public static void deleteUndefinedNavigation(String applicationName, long loadVersion) {
        List<SupplierNavigation> list = SupplierNavigation.find("select s from SupplierNavigation s where s.applicationName=? and s.loadVersion <> ?", applicationName, loadVersion).fetch();
        for (SupplierNavigation nav : list) {
            SupplierNavigation.em().remove(nav);
        }
    }
}
