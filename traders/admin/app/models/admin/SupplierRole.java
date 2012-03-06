package models.admin;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "supplier_roles")
public class SupplierRole extends Model {

    public String name;

    public String key;

    public String description;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

}

