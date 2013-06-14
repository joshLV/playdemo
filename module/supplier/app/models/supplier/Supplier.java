package models.supplier;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.PathUtil;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.admin.SupplierUser;
import models.operator.OperateUser;
import models.operator.Operator;
import models.order.Prepayment;
import models.resale.Resaler;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.Shop;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static final String CAN_SALE_REAL = "canSaleReal"; //是否销售实物
    public static final String SELL_ECOUPON = "sellECoupon"; //是否销售电子券
    public static final String KTV_SUPPLIER = "ktvSupplier"; //是否KTV商户
    public static final String KTV_SKU_PUSH_END_HOUR = "ktvSkuPushEndHour";//ktv sku推送时间

    private static Supplier SHIHUI = null;
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
//    @Mobile
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

    /*
     * TODO 待废弃
     * 是否销售实体商品
     */
    @Column(name = "can_sale_real")
    public Boolean canSaleReal = false;


    /**
     * TODO 待废弃
     * 是否销售电子券
     */
    @Column(name = "sell_ecoupon")
    public Boolean sellECoupon = true;

    @Column(name = "weibo_id")
    public String weiboId;

//    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
//    @JoinTable(name = "suppliers_resalers",
//            inverseJoinColumns = @JoinColumn(name = "resaler_id"),
//            joinColumns = @JoinColumn(name = "supplier_id"))
//    public List<Resaler> resalers;

    /**
     * 商户类别
     */

    @ManyToOne
    @JoinColumn(name = "supplier_category_id")
    public SupplierCategory supplierCategory;

    @OneToMany(mappedBy = "supplierId")
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

    /**
     * 是否可查看销售量和退款量
     */
    @Column(name = "show_selling_state")
    public Boolean showSellingState = false;

    /**
     * 商户默认分销商
     */
    @Column(name = "default_resaler_id")
    public Long defaultResalerId;

    //=================================================以上是全部数据库相关属性================================================

    @javax.persistence.Transient
    public String statusName;

    @javax.persistence.Transient
    public String whetherToShowSellingState;

    @javax.persistence.Transient
    public Integer shopsCount;

    @javax.persistence.Transient
    public Integer brandsCount;

    @javax.persistence.Transient
    public Integer goodsCount;

    @Transient
    public String getName() {
        return StringUtils.isBlank(otherName) ? fullName : otherName;
    }

    //销售专员姓名
    @Transient
    public String getSalesName() {
        OperateUser operateUser = OperateUser.findById(this.salesId);
        if (operateUser != null) {
            return operateUser.userName;
        } else {
            return "";
        }
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

    @Transient
    public boolean isKtvSupplier() {
        return "1".equals(this.getProperty(Supplier.KTV_SUPPLIER));
    }

    @Transient
    public String getProperty(String propertyName) {
        return getProperty(propertyName, "0");
    }

    @Transient
    public String getProperty(String propertyName, String defaultStr) {
        if (this.id == null) {
            return defaultStr;
        }
        SupplierProperty supplierProperty = SupplierProperty.find("supplier=? and name=?", this, propertyName).first();
        if (supplierProperty == null || StringUtils.isBlank(supplierProperty.value)) {
            return defaultStr;
        }
        return supplierProperty.value;
    }

    @Transient
    public void setProperty(String propertyName, String value) {
        SupplierProperty property = SupplierProperty.findProperty(this, propertyName);
        if (property == null) {
            new SupplierProperty(this, propertyName, value).save();
        } else {
            property.value = value;
            property.save();
        }

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
        this.resetCode(this.supplierCategory);
        return super.create();
    }

    public static String calculateFormattedCode(String originalCode) {
        int seqCode = Integer.parseInt(originalCode) + 1;
        int digits = String.valueOf(seqCode).length();
        if (digits < 2) {
            digits = 2;
        }
        /*
            用于 Supplier 设置sequenceCode
         */
        if (originalCode.length() == 4 && digits != 5) {
            return String.format("%04d", seqCode);
        }
        return String.format("%0" + digits + "d", seqCode);
    }

    public void resetCode(SupplierCategory supplierCategory) {
        Supplier supplier = null;
        if (supplierCategory != null) {
            supplier = Supplier.find("supplierCategory.id=? and sequenceCode is not null order by sequenceCode desc", supplierCategory.id).first();
        }
        if (supplier == null || supplier.sequenceCode == null) {
            this.sequenceCode = "0001";
        } else {
            this.sequenceCode = calculateFormattedCode(supplier.sequenceCode);
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
        sp.weiboId = supplier.weiboId;
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
//        sp.canSaleReal = supplier.canSaleReal;
//        sp.sellECoupon = supplier.sellECoupon;
        sp.salesId = supplier.salesId;
        sp.shopEndHour = supplier.shopEndHour;
        sp.updatedAt = new Date();
//        sp.resalers = supplier.resalers;
        sp.defaultResalerId = supplier.defaultResalerId;
        sp.showSellingState = supplier.showSellingState == null ? false : supplier.showSellingState;
        if (sp.supplierCategory == null || (sp.supplierCategory != null && supplier.supplierCategory != null && supplier.supplierCategory.id != sp.supplierCategory.id)) {
            sp.resetCode(supplier.supplierCategory);
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

    public static List<Supplier> findByCondition(Long supplierId, String code, String domainName, String keyword) {
        StringBuilder sql = new StringBuilder("deleted= :deleted ");
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);
        if (supplierId != null && supplierId > 0) {
            sql.append("and id = :supplierId ");
            paramsMap.put("supplierId", supplierId);
        }

        if (StringUtils.isNotBlank(code)) {
            sql.append("and code like :code ");
            paramsMap.put("code", code + "%");
        }

        if (StringUtils.isNotBlank(domainName)) {
            sql.append("and domainName like :domainName ");
            paramsMap.put("domainName", "%" + domainName + "%");
        }
        if (StringUtils.isNotBlank(keyword)) {
            sql.append(" and (");
            //商户名称 or 商户短名称
            sql.append("fullName like :fullName or otherName like :otherName ");
            paramsMap.put("fullName", "%" + keyword + "%");
            paramsMap.put("otherName", "%" + keyword + "%");

            //销售专员姓名
            List<OperateUser> operateUserList = OperateUser.find("userName like ?", "%" + keyword + "%").fetch();
            if (operateUserList.size() > 0) {
                List<Long> salesIds = new ArrayList<>();
                for (OperateUser o : operateUserList) {
                    salesIds.add(o.id);
                }
                sql.append("or salesId in (:salesIds) ");
                paramsMap.put("salesIds", salesIds);
            }

            //门店
            List<Shop> shopList = Shop.find("phone like ? or address like ? or name like ?",
                    "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"
            ).fetch();
            if (shopList.size() > 0) {
                List<Long> shopSupplierIds = new ArrayList<>();
                for (Shop s : shopList) {
                    shopSupplierIds.add(s.supplierId);
                }
                sql.append("or id in (:shopSupplierIds) ");
                paramsMap.put("shopSupplierIds", shopSupplierIds);

            }

            //品牌
            List<Brand> brandList = Brand.find("name like ?", "%" + keyword + "%").fetch();
            if (brandList.size() > 0) {
                List<Long> brandSupplierIds = new ArrayList<>();
                for (Brand b : brandList) {
                    brandSupplierIds.add(b.supplier.id);
                }
                sql.append("or id in (:brandSupplierIds) ");
                paramsMap.put("brandSupplierIds", brandSupplierIds);
            }

            sql.append(")");
        }

        sql.append(" order by createdAt DESC");
        return find(sql.toString(), paramsMap).fetch();
    }

    public static List<Supplier> findSuppliersByCanSaleReal() {
        return find("deleted=? and canSaleReal=? order by createdAt DESC", DeletedStatus.UN_DELETED, true).fetch();
    }

    public static List<Supplier> findUnDeleted() {
        return find("deleted=? order by createdAt DESC", DeletedStatus.UN_DELETED).fetch();
    }

    public static List<Supplier> findUnDeletedAndKtvSupplier() {
        return find("deleted=? and id in (select supplier.id from SupplierProperty where name=? and value=? )  order by createdAt DESC", DeletedStatus.UN_DELETED, KTV_SUPPLIER, "1").fetch();
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

    public List<Shop> getShops() {
        return Shop.find("supplierId=? and deleted=?", this.id, DeletedStatus.UN_DELETED
        ).fetch();
    }

    public List<Brand> getBrands() {
        return Brand.find("supplier.id=? and deleted=?", this.id, DeletedStatus.UN_DELETED
        ).fetch();
    }


    public List<Goods> getGoods() {
        return Goods.find("supplierId=?", this.id).fetch();
    }

    /**
     * 可提现金额.
     *
     * @param lastPrepayment 预付款记录
     * @param withdrawAmount 可结算余额
     * @param date
     * @return
     */
    public static BigDecimal getWithdrawAmount(Account supplierAccount, Prepayment lastPrepayment, BigDecimal
            withdrawAmount, Date date) {
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

    public List<SupplierContract> getContract() {
        return SupplierContract.find("supplierId=?", this.id).fetch();
    }


    public static void clearShihuiSupplier() {
        SHIHUI = null;
    }

    /**
     * 获取视惠商户对象.
     *
     * @return
     */
    public static Supplier getShihui() {
        if (SHIHUI == null) {
            SHIHUI = find("fullName='上海视惠信息科技有限公司' and deleted=?", DeletedStatus.UN_DELETED).first();
        }
        return SHIHUI;
    }


    /**
     * 默认运营商。
     *
     * @return
     */
    @Transient
    public Operator defaultOperator() {
        Operator operator = Operator.defaultOperator();
        // 是否有默认分销商，有则默认使用对应的Operator账户
        if (this.defaultResalerId == null) {
            return operator;
        }
        Resaler defaultResaler = Resaler.findById(this.defaultResalerId);
        if (defaultResaler != null) {
            operator = defaultResaler.operator;
        }
        return operator;
    }
}
