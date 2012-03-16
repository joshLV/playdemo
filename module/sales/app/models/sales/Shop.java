package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.*;
import java.util.*;

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

    public static ModelPaginator<Shop> query(Shop shopCondition, int pageNumber, int pageSize) {
        StringBuilder search = new StringBuilder();
        search.append("companyId=? and deleted=?");
        ArrayList queryParams = new ArrayList();
        queryParams.add(shopCondition.companyId);
        queryParams.add(DeletedStatus.UN_DELETED);

        if (!StringUtils.isBlank(shopCondition.name)) {
            search.append(" and name like ?");
            queryParams.add("%" + shopCondition.name.trim() + "%");
        }

        if (!StringUtils.isBlank(shopCondition.address)) {
            search.append(" and address like ?");
            queryParams.add("%" + shopCondition.address.trim() + "%");
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

}
