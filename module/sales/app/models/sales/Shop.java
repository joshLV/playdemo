package models.sales;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;

@Entity
@Table(name = "shops")
public class Shop extends Model {

    private static final long serialVersionUID = 36632320609113062L;

    @Column(name = "supplier_id")
    public long supplierId;

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

    @Column(name = "manager_mobiles")
    public String managerMobiles;   //店长手机号，以半角逗号','分割

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


    @Transient
    public String supplierName;

    @Transient
    public String cityId;

    @Transient
    public String districtId;


    public static final String CACHEKEY = "SHOP";

    public static final String CACHEKEY_SUPPLIERID = "SHOP_SUPPLIER_ID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_SUPPLIERID + this.supplierId);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_SUPPLIERID + this.supplierId);
        super._delete();
    }

    /**
     * 读取某商户的全部门店记录
     *
     * @param supplierId
     * @return
     */
    public static List<Shop> findShopBySupplier(Long supplierId) {
        return Shop.find("bySupplierIdAndDeleted", supplierId, DeletedStatus.UN_DELETED).fetch();
    }


    public static boolean containsShop(Long supplierId) {
        return Shop.count("bySupplierIdAndDeleted", supplierId, DeletedStatus.UN_DELETED) > 0;
    }


    public static ModelPaginator<Shop> query(Shop shopCondition, int pageNumber, int pageSize) {
        StringBuilder search = new StringBuilder();
        search.append("deleted=?");
        ArrayList queryParams = new ArrayList();
        queryParams.add(DeletedStatus.UN_DELETED);

        if (shopCondition.supplierId > 0) {
            search.append(" and supplierId=?");
            queryParams.add(shopCondition.supplierId);
        }

        if (!StringUtils.isBlank(shopCondition.name)) {
            search.append(" and (name like ? or address like ?)");
            queryParams.add("%" + shopCondition.name.trim() + "%");
            queryParams.add("%" + shopCondition.name.trim() + "%");
        }

        ModelPaginator<Shop> shopPage = new ModelPaginator<>(Shop.class, search.toString(),
                queryParams.toArray()).orderBy("createdAt desc");
        shopPage.setPageNumber(pageNumber);
        shopPage.setPageSize(pageSize);
        return shopPage;
    }

    /**
     * 虚拟删除
     *
     * @param id
     * @return
     */
    public static boolean delete(long id) {
        Shop shop = Shop.findById(id);
        if (shop != null) {
            shop.deleted = DeletedStatus.DELETED;
            shop.save();
            return true;
        }
        return false;
    }

    public String getAreaName() {
        String areaName;
        Area area = Area.findById(areaId);
        if (area == null)
            return "";
        else
            areaName = area.name;
        return areaName;
    }
}
