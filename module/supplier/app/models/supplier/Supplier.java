package models.supplier;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.ImageSize;
import com.uhuila.common.util.PathUtil;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "suppliers")
public class Supplier extends Model {
    @Required
    @MaxSize(100)
    @Column(name = "domain_name")
    public String domainName;
    @Required
    @MaxSize(50)
    @Column(name = "full_name")
    public String fullName;
    @Column(name = "created_at")
    public Date createdAt;
    @Column(name = "updated_at")
    public Date updatedAt;
    @Enumerated(EnumType.STRING)
    public SupplierStatus status;
    public String logo;
    public String description;
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Transient
    public String getSmallLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, ImageSize.SMALL);
    }

    @Transient
    public String getOriginalLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, ImageSize.ORIGINAL);
    }

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "http://img0.uhlcdndev.net");

    public static void update(Supplier supplier) {
        Supplier sp = findById(supplier.id);
        if (sp == null) {
            return;
        }
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
        return find("deleted=? order by createdAt DESC", DeletedStatus.UN_DELETED).fetch();
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
