package models.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "supplier_navigations")
public class SupplierNavigation extends Model {

    public String name;

    public String description;

    public String action;

    public String url;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    public SupplierNavigation parent;

    public List<SupplierNavigation> children = new ArrayList<SupplierNavigation>();

    public boolean hasLink() {
        return url != null || action != null;
    }

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

}
