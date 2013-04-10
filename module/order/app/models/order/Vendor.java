package models.order;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供货商
 * <p/>
 * User: wangjia
 * Date: 13-3-28
 * Time: 下午5:46
 */
@Entity
@Table(name = "vendor")
public class Vendor extends Model {

    /*
        公司名称
     */
    @Required
    public String name;

    /*
       公司地址
     */
    @Required
    public String address;

    /*
       授权代表
     */
    @Column(name = "authorized_representative")
    public String authorizedRepresentative;

    /*
       电话
     */
    @Phone
    public String phone;

    /*
      传真
    */
    public String fax;

    /*
       开户行
     */
    @Column(name = "bank_name")
    public String bankName;

    /*
       银行帐号
     */
    @Column(name = "card_number")
    public String cardNumber;

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

    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;
    /**
     * 修改人
     */
    @Column(name = "updated_by")
    public String updatedBy;

    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public com.uhuila.common.constants.DeletedStatus deleted;

    @Override
    public boolean create() {
        deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        createdAt = new Date();
        return super.create();
    }

    public static void update(Long id, Vendor vendor) {
        Vendor updatedVendor = Vendor.findById(id);
        updatedVendor.refresh();
        updatedVendor.name = vendor.name;
        updatedVendor.address = vendor.address;
        updatedVendor.authorizedRepresentative = vendor.authorizedRepresentative;
        updatedVendor.phone = vendor.phone;
        updatedVendor.fax = vendor.fax;
        updatedVendor.bankName = vendor.bankName;
        updatedVendor.cardNumber = vendor.cardNumber;
        updatedVendor.save();
    }


    public static void delete(long id) {
        Vendor vendor = Vendor.findById(id);
        if (vendor == null) {
            return;
        }
        if (!com.uhuila.common.constants.DeletedStatus.DELETED.equals(vendor.deleted)) {
            vendor.deleted = com.uhuila.common.constants.DeletedStatus.DELETED;
            vendor.save();
        }
    }

    public static List<Vendor> findByCondition(String keyword) {
        StringBuilder sql = new StringBuilder("deleted= :deleted");
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deleted", com.uhuila.common.constants.DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(keyword)) {
            sql.append(" and (name like :vendorName or address like :address or phone like :phone)");
            paramsMap.put("vendorName", "%" + keyword + "%");
            paramsMap.put("address", "%" + keyword + "%");
            paramsMap.put("phone", "%" + keyword + "%");
        }
        return find(sql.toString(), paramsMap).fetch();
    }

    public static List<Vendor> findUnDeleted() {
        return find("deleted=? order by createdAt DESC", com.uhuila.common.constants.DeletedStatus.UN_DELETED).fetch();
    }


}
