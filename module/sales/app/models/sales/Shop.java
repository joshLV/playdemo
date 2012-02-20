package models.sales;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;


@Entity
@Table(name = "shops")
public class Shop extends Model {
    @Column(name = "company_id")
    public long companyId;

    @Column(name = "area_id")
    public long areaId;

    public String no;

    public String name;

    public String address;

    public String phone;

    public String traffic;

    @Column(name = "is_close")
    public String isClose;

    public float latitude;

    public float longitude;

    @Column(name = "created_at")
    public String createdAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_at")
    public String updatedAt;

    @Column(name = "updated_by")
    public String updatedBy;

    public int deleted;

    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "display_order")
    public String displayOrder;


    /**
     * 读取某商户的全部门店记录
     *
     * @param companyId
     * @return
     */
    public static List<Shop> findShopByCompany(long companyId) {
        return Shop.find("company_id=? and deleted=0", companyId).fetch();
    }

    /**
     * 虚拟删除
     *
     * @param id
     * @return
     */
    public static boolean deleted(long id) {
        Shop shop = Shop.findById(id);
        shop.deleted = 1;
        shop.save();
        return true;
    }

}
