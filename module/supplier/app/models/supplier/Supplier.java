package models.supplier;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "companies")
public class Supplier extends Model {
    @Required
    @MaxSize(100)
    @Column(name = "domain_name")
    public String domainName;
    @Required
    @MaxSize(50)
    @Column(name = "full_name")
    public String fullName;
    @Column(name = "registed_at")
    public Date registedAt;
    @Column(name = "updated_at")
    public Date updatedAt;
    @Enumerated(EnumType.STRING)
    public SupplierStatus status;
    public String logo;
    public String description;
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    public static void update(long id, Supplier supplier) {
        Supplier sp = findById(id);
        if (StringUtils.isNotBlank(supplier.logo)) {
            sp.logo = supplier.logo;
        }
        sp.domainName = supplier.domainName;
        sp.fullName = supplier.fullName;
        sp.description = supplier.description;
        sp.updatedAt = new Date();
        sp.save();
    }

    public static void delete(long id) {
        Supplier supplier = Supplier.findById(id);
        if (supplier == null) {
            return;
        }
        if (!DeletedStatus.DELETED.equals(supplier.deleted)) {
            supplier.deleted = DeletedStatus.DELETED;
            supplier.save();
        }
    }

    public static List<Supplier> findUnDeleted() {
        return find("deleted=? order by registedAt DESC", DeletedStatus.UN_DELETED).fetch();
    }

    public static void freeze(long id) {
        updateStatus(id, SupplierStatus.FREEZE);
    }

    public static void unfreeze(long id) {
        updateStatus(id, SupplierStatus.NORMAL);
    }

    private static void updateStatus(long id, SupplierStatus status) {
        Supplier supplier = Supplier.findById(id);
        if (supplier == null) {
            return;
        }
        supplier.status = status;
        supplier.save();
    }
}
