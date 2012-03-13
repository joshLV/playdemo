package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "shops")
public class Shop extends Model {


    @Column(name = "company_id")
    public long companyId;

    @Column(name = "area_id")
    public String areaId;

    public String no;

    @Required
    public String name;

    @Required
    public String address;

    public String phone;

    public String traffic;

    @Column(name = "is_close")
    public String isClose;

    public float latitude;

    public float longitude;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Column(name = "updated_by")
    public String updatedBy;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "display_order")
    public String displayOrder;

    @ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "shops", fetch = FetchType.LAZY)
    public Set<Goods> goods = new HashSet<>();

    /**
     * 读取某商户的全部门店记录
     *
     * @param companyId
     * @return
     */
    public static List<Shop> findShopByCompany(long companyId) {
        return Shop.find("byCompanyIdAndDeleted", companyId, DeletedStatus.UN_DELETED).fetch();
    }

    /**
     * 虚拟删除
     *
     * @param id
     * @return
     */
    public static boolean deleted(long id) {
        Shop shop = Shop.findById(id);
        shop.deleted = DeletedStatus.DELETED;
        shop.save();
        return true;
    }

}
