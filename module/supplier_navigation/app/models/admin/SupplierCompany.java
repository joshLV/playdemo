package models.admin;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "supplier_companies")
public class SupplierCompany extends Model {

    @Column(name = "domain_name")
    public String domainName;

    @Column(name = "full_name")
    public String fullName;

    // FIXME: SupplierComapny.status应该是一个枚举值
    public String status;

    public String logo;

    public String description;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

}
