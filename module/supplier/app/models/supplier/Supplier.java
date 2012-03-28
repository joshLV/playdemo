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

/**
 * 供应商（商户）
 * <p/>
 * author:sujie@uhuila.com
 */
@Entity
@Table(name = "suppliers")
public class Supplier extends Model {
    /**
     * 域名
     */
    @Required
    @MaxSize(100)
    @Column(name = "domain_name")
    public String domainName;
    /**
     * 商户名称
     */
    @Required
    @MaxSize(50)
    @Column(name = "full_name")
    public String fullName;
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;
    @Enumerated(EnumType.STRING)
    /**
     * 状态
     */
    public SupplierStatus status;
    /**
     * logo图片路径
     */
    public String logo;
    /**
     * 描述
     */
    public String description;
    /**
     * 删除状态
     */
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
            ("image.server", "img0.uhcdn.com");

    @Override
    public boolean create() {
        deleted = DeletedStatus.UN_DELETED;
        status = SupplierStatus.NORMAL;
        createdAt = new Date();

        return super.create();
    }


    public static void update(Long id,Supplier supplier) {
        System.out.println("update");
        System.out.println("id:" + id);
        Supplier sp = findById(id);
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
    
    
    @Override
    public String toString() {
        return "Supplier[" + this.fullName + "@" + this.domainName + "(" + this.id + ")]";
    }
}
