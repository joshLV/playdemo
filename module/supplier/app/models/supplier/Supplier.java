package models.supplier;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.PathUtil;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.admin.SupplierUser;
import models.order.Prepayment;
import models.sales.Brand;
import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.solr.SolrField;
import play.modules.solr.SolrSearchable;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.beans.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 供应商（商户）
 * <p/>
 * author:sujie@uhuila.com
 */
@Entity
@Table(name = "suppliers")
@SolrSearchable
public class Supplier extends Model {

    private static final long serialVersionUID = 7122320609113062L;
    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";
    public static final String BEGIN_TIME = " 00:00";
    public static final String END_TIME = " 23:59";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    /**
     * 域名
     */
    @Required
    @MaxSize(100)
    @Match("^[a-zA-Z0-9\\-]{3,20}$")
    @Column(name = "domain_name")
    @SolrField
    public String domainName;
    /**
     * 公司名称
     */
    @Required
    @MaxSize(50)
    @Column(name = "full_name")
    @SolrField
    public String fullName;

    /**
     * 公司别名称
     */
    @Required
    @MaxSize(50)
    @Column(name = "other_name")
    @SolrField
    public String otherName;

    /**
     * 职务
     */
    @MaxSize(100)
    public String position;

    /**
     * 负责人手机号
     */
    @Mobile
    public String mobile;

    /**
     * 财务负责人手机号
     */
    @Mobile
    public String accountLeaderMobile;

    /**
     * 负责人联系电话
     */
    @Phone
    public String phone;

    /**
     * 负责人姓名
     */
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
    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    public SupplierStatus status;
    /**
     * logo图片路径
     */
    public String logo;
    /**
     * 描述
     */
    @SolrField
    public String remark;
    @Email
    public String email;
    @Email
    @Column(name = "sales_email")
    public String salesEmail;

    /**
     * 所属操作员
     */
    @Column(name = "sales_id")
    public Long salesId;

    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    /**
     * 商户流水码（4位）
     */
    @Column(name = "sequence_code")
    public String sequenceCode;

    /**
     * 商户编码 【商户类别编码（2位）+商户流水码（4位）】
     */
    public String code;

    /**
     * 商户类别
     */

    @ManyToOne
    @JoinColumn(name = "supplier_category_id")
    public SupplierCategory supplierCategory;

    @OneToMany(mappedBy = "supplier")
    public List<SupplierContract> supplierContractList;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn(name = "`display_order`")
    @JoinColumn(name = "supplier_id")
    public List<Brand> brands;

    public Supplier(Long id) {
        this.id = id;
    }

    /**
     * 格式:HH:mm
     */
    @Column(name = "shop_end_hour")
    public String shopEndHour;

    @Transient
    public String getName() {
        return StringUtils.isBlank(otherName) ? fullName : otherName;
    }

    public Supplier() {
    }


    public static final String CACHEKEY = "SUPPLIER";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }


    @Transient
    public String getSmallLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_SMALL);
    }

    @Transient
    public String getOriginalLogo() {
        return PathUtil.getImageUrl(IMAGE_SERVER, logo, IMAGE_ORIGINAL);
    }

    /**
     * 获取商户完整域名
     *
     * @return
     */
    @Transient
    public String getSupplierHost() {
        return domainName + "." + play.Play.configuration.getProperty("application.supplierDomain", "quanmx.com");
    }

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");

    @Override
    public boolean create() {
        deleted = DeletedStatus.UN_DELETED;
        status = SupplierStatus.NORMAL;
        createdAt = new Date();
        this.setCode(this.supplierCategory);
        return super.create();
    }

    public static String calculateFormattedCode(String originalCode, String digits) {
        return String.format("%0" + digits + "d", Integer.valueOf(originalCode) + 1);
    }

    public void setCode(SupplierCategory supplierCategory) {
        Supplier supplier = null;
        if (supplierCategory != null) {
            supplier = Supplier.find("supplierCategory.id=? and sequenceCode is not null order by sequenceCode desc", supplierCategory.id).first();
        }
        if (supplier == null || supplier.sequenceCode == null) {
            this.sequenceCode = "0001";
        } else {
            this.sequenceCode = calculateFormattedCode(supplier.sequenceCode, "4");
        }
        if (supplierCategory == null || supplierCategory.code == null) {
            return;
        }
        this.code = supplierCategory.code + this.sequenceCode;
        this.supplierCategory = supplierCategory;
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
        sp.otherName = supplier.otherName;
        sp.remark = supplier.remark;
        sp.mobile = supplier.mobile;
        sp.phone = supplier.phone;
        sp.position = supplier.position;
        sp.userName = supplier.userName;
        sp.email = supplier.email;
        sp.accountLeaderMobile = supplier.accountLeaderMobile;
//        sp.salesEmail = supplier.salesEmail;
        sp.salesId = supplier.salesId;
        sp.shopEndHour = supplier.shopEndHour;
        sp.updatedAt = new Date();
        if (sp.supplierCategory == null || (sp.supplierCategory != null && supplier.supplierCategory != null && supplier.supplierCategory.id != sp.supplierCategory.id)) {
            sp.setCode(supplier.supplierCategory);
        }
        sp.save();
        List<Goods> goodsList = Goods.find("supplierId=? and code is not null order by code desc", sp.id).fetch();
        if (goodsList != null && goodsList.size() > 0) {
            for (Goods g : goodsList) {
                g.refresh();
                Supplier tempSupplier = Supplier.findById(g.supplierId);
                g.code = tempSupplier.code + g.sequenceCode;
                g.save();
            }
        } else {
            List<Goods> existedGoodsList = Goods.find("supplierId=? order by createdAt desc", sp.id).fetch();
            for (Goods g : existedGoodsList) {
                g.refresh();
                g.resetCode();
                g.save();
            }
        }

    }

    public static void delete(long id) {
        Supplier supplier = Supplier.findById(id);
        if (supplier == null) {
            return;
        }
        if (!DeletedStatus.DELETED.equals(supplier.deleted)) {
            SupplierUser supplierUser = SupplierUser.findUserByDomainName(supplier.domainName, supplier.loginName);
            if (supplierUser != null) {
                supplierUser.deleted = DeletedStatus.DELETED;
                supplierUser.save();
            }
            supplier.deleted = DeletedStatus.DELETED;
            supplier.save();
        }
    }

    public static List<Supplier> findByCondition(String otherName) {
        return findByCondition(otherName, null);
    }

    public static List<Supplier> findByCondition(String otherName, String code) {
        StringBuilder sql = new StringBuilder("deleted=?");
        List params = new ArrayList();
        params.add(DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(otherName)) {
            sql.append(" and otherName like ?");
            params.add("%" + otherName + "%");
        }

        if (StringUtils.isNotBlank(otherName)) {
            sql.append("or fullName like ?");
            params.add("%" + otherName + "%");
        }

        if (StringUtils.isNotBlank(code)) {
            sql.append("and code like ?");
            params.add(code + "%");
        }

        sql.append(" order by createdAt DESC");
        return find(sql.toString(), params.toArray()).fetch();
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

    public static Supplier findByFullName(String fullName) {
        return Supplier.find("fullName like ?", "%" + fullName + "%").first();
    }

    public static List<Supplier> findListByFullName(String fullName) {
        return find("fullName like ?", "%" + fullName + "%").fetch();
    }

    public static boolean existDomainName(String domainName) {
        return find("domainName=? and deleted=?", domainName, DeletedStatus.UN_DELETED).first() != null;
    }

    /**
     * 检查是否有营业时间
     *
     * @param conditionDate 传入的时间
     * @param shopHour      营业时间
     * @param hourFlag
     * @return
     */
    public static Date getShopHour(Date conditionDate, String shopHour, boolean hourFlag) {
        String time = END_TIME;
        int days = 0;
        if (hourFlag) {
            days = 1;
        }
        String dateStr = DateUtil.dateToString(conditionDate, days) + (StringUtils.isBlank(shopHour) ? time : " " + shopHour);
        return DateUtil.stringToDate(dateStr, DATE_FORMAT);
    }

    public List<Goods> getGoods() {
        return Goods.find("supplierId=?", this.id
        ).fetch();
    }

    /**
     * 可提现金额.
     *
     * @param lastPrepayment 预付款记录
     * @param withdrawAmount 可结算余额
     * @param date
     * @return
     */
    public static BigDecimal getWithdrawAmount(Account supplierAccount, Prepayment lastPrepayment, BigDecimal withdrawAmount, Date date) {
        if (lastPrepayment == null) {
            return withdrawAmount;
        }
        //预付款已过期
        if (lastPrepayment.expireAt != null && lastPrepayment.expireAt.before(date)) {
            BigDecimal prepayConsumedAmount = AccountSequence.getVostroAmountTo(supplierAccount, lastPrepayment.expireAt);
            BigDecimal vostroAmount = AccountSequence.getVostroAmount(supplierAccount, lastPrepayment.expireAt, date);
            if (prepayConsumedAmount.compareTo(lastPrepayment.getBalance()) > 0) {
                return prepayConsumedAmount.subtract(lastPrepayment.getBalance()).add(vostroAmount);
            }
            return vostroAmount;
        }
        if (withdrawAmount.compareTo(lastPrepayment.getBalance()) <= 0) {
            return BigDecimal.ZERO;
        }
        return withdrawAmount.subtract(lastPrepayment.getBalance());
    }

    public static Supplier findUnDeletedById(Long id) {
        return Supplier.find("id=? and deleted=?", id, DeletedStatus.UN_DELETED).first();
    }

    public static Supplier findByDomainName(String domainName) {
        return Supplier.find("domainName=? and deleted=?", domainName, DeletedStatus.UN_DELETED).first();
    }
}
