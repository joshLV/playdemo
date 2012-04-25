package models.supplier;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.ImageSize;
import com.uhuila.common.util.PathUtil;
import models.sales.Brand;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.beans.Transient;
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
    @Match("^[a-zA-Z0-9\\-]{3,20}$")
    @Column(name = "domain_name")
    public String domainName;
    /**
     * 公司名称
     */
    @Required
    @MaxSize(50)
    @Column(name = "full_name")
    public String fullName;

    /**
     * 职务
     */
    @Required
    @MaxSize(100)
    public String position;

    /**
     * 负责人手机号
     */
    @Mobile
    public String mobile;

    /**
     * 负责人联系电话
     */
    @Phone
    public String phone;

    /**
     * 负责人姓名
     */
    @Required
    @Column(name = "user_name")
    public String userName;

    @Column(name = "login_name")
    public String loginName;

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
    public String remark;
    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn(name = "`display_order`")
    @JoinColumn(name = "supplier_id")
    public List<Brand> brands;

    public Supplier(Long id) {
        this.id = id;
    }

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


    public static void update(Long id, Supplier supplier) {
        Supplier sp = findById(id);
        if (sp == null) {
            return;
        }
        if (StringUtils.isNotBlank(supplier.logo)) {
            sp.logo = supplier.logo;
        }
        sp.domainName = supplier.domainName;
        sp.fullName = supplier.fullName;
        sp.remark = supplier.remark;
        sp.mobile = supplier.mobile;
        sp.phone = supplier.phone;
        sp.position = supplier.position;
        sp.userName = supplier.userName;
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
