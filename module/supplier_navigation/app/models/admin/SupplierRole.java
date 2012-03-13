package models.admin;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "supplier_roles")
public class SupplierRole extends Model {

    public String text;

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

    @ManyToMany(cascade = CascadeType.REFRESH,mappedBy="roles",
            fetch=FetchType.LAZY)
    public Set<SupplierUser> cusers;

}

