package models.sales;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "shops")
@SolrSearchable
public class Shop extends Model {

    private static final long serialVersionUID = 36632320609113062L;

    @Column(name = "supplier_id")
    public Long supplierId;

    @Column(name = "area_id")
    @SolrField
    public String areaId;

    public String no;

    @Required
    @SolrField
    public String name;

    @Required
    @SolrField
    public String address;

    @SolrField
    public String phone;

    @SolrField
    public String transport;

    @Column(name = "is_close")
    public String isClose;

    @Column(name = "lat")
    public String latitude = "0";  //纬度

    @Column(name = "lng")
    public String longitude = "0"; //经度

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

    @Transient
    public String supplierName;

    @Transient
    @SolrField
    public String cityId = Area.SHANGHAI;

    /**
     * 是否独立结算.
     */
    @Column(name = "independent_clearing")
    public Boolean independentClearing = false;

    private String districtId;

    public Shop() {
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
        this.lockVersion = 0;
        this.updatedAt = new Date();
    }

    @Transient
    @SolrField
    public String getDistrictId() {
        if (districtId != null) {
            return districtId;
        }
        if (StringUtils.isNotBlank(areaId) && areaId.length() >= 5) {
            districtId = areaId.substring(0, 5);
        }
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

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


    public boolean hasMap() {
        return !(StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude) || latitude.equals("0")
                || longitude.equals("0") || latitude.equals("0.0") || longitude.equals("0.0"));
    }

    /**
     * 读取某商户的全部门店记录
     *
     * @param supplierId 商户标识
     * @return 门店列表
     */
    public static List<Shop> findShopBySupplier(Long supplierId) {
        return Shop.find("bySupplierIdAndDeleted", supplierId, DeletedStatus.UN_DELETED).fetch();
    }

    /**
     * 获取指定商户的所有的独立提现结算的门店.
     *
     * @param supplierId 商户标识
     * @return 门店列表
     */
    public static List<Shop> findIndependentList(Long supplierId) {
        return find("bySupplierIdAndIndependentClearing", supplierId, true).fetch();
    }

    public static boolean containsShop(Long supplierId) {
        return Shop.count("bySupplierIdAndDeleted", supplierId, DeletedStatus.UN_DELETED) > 0;
    }


    public static ModelPaginator<Shop> query(Shop shopCondition, int pageNumber, int pageSize) {
        StringBuilder search = new StringBuilder();
        search.append("deleted=?");
        ArrayList queryParams = new ArrayList();
        queryParams.add(DeletedStatus.UN_DELETED);
        if (shopCondition.supplierId != null && shopCondition.supplierId > 0) {
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
            AccountUtil.cancelAccount(shop.id, AccountType.SHOP);
            return true;
        }
        return false;
    }

    /**
     * 对于需要独立提现的门店，需要建立商户门店的帐号
     * 但如果门店帐号已经存在则不创建
     */
    public void createAccountsIfNeeded() {
        if (!AccountUtil.accountExist(id, AccountType.SHOP)) {
            Account account = new Account(id, AccountType.SHOP);
            account.supplierId = supplierId;
            account.save();
        } else {
            Account account = AccountUtil.getShopAccount(id);
            //sujie: 之前由于程序问题存在门店创建出的帐号中的supplier为空的情况，所以才加了下面这段代码，以便修复历史数据
            if (account.supplierId == null) {
                account.supplierId = supplierId;
                account.save();
            }
        }
    }

    @Transient
    @SolrField
    public String getAreaName() {
        String areaName;
        if (StringUtils.isEmpty(areaId)) {
            return "";
        }
        Area area = Area.findById(areaId);
        if (area == null) {
            return "";
        }
        areaName = area.name;
        return areaName;
    }

    public String getAreaName(int flag) {

        if (StringUtils.isEmpty(areaId)) {
            return "";
        }
        Area district = Area.findParent(areaId);
        if (district == null) {
            Area city = Area.findById(areaId);
            if (city != null && flag == 2) {
                return city.name;
            }
            return "";
        }
        //区域名称
        if (flag == 1) {
            return district.name;
        }

        Area area = Area.findById(areaId);
        // 商圈名称
        if (area != null && flag == 0) {
            return area.name;
        }

        Area city = Area.findParent(district.id);
        if (city != null && flag == 2) {
            return city.name;
        }
        return "";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Shop shop = (Shop) o;

        if (address != null ? !address.equals(shop.address) : shop.address != null) return false;
        if (id != null ? !id.equals(shop.id) : shop.id != null) return false;
        if (name != null ? !name.equals(shop.name) : shop.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /**
     * 获取指定商户独立核算的所有门店.
     *
     * @param supplierId
     * @return
     */
    public static List<Shop> getIndependentShops(Long supplierId) {
        return find("supplierId=? and independentClearing=?", supplierId, true).fetch();
    }


}
