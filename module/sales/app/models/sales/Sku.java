package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import org.apache.commons.lang.StringUtils;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 货品
 * <p/>
 * User: yanjy
 * Date: 13-2-26
 * Time: 下午4:34
 */
@Entity
@Table(name = "sku")
public class Sku extends Model {

    /**
     * 货品名称
     */
    @Required
    @MaxSize(value = 500)
    public String name;

    /**
     * 市场价
     */
    @Required
    @Column(name = "market_price")
    public BigDecimal marketPrice;

    /**
     * sku流水码（2位）
     */
    @Column(name = "sequence_code")
    public String sequenceCode;


    /**
     * 货品（SKU）编码 【2位类别编码+4位商户编码+2位品牌编码+2位流水码】
     */
    @Column(name = "code")
    public String code;
    /**
     * 发货商户
     */
    @Required
    @ManyToOne
    public Supplier supplier;

    /**
     * 品牌
     */
    @Required
    @ManyToOne
    public Brand brand;

    /**
     * 类别
     */
    @Required
    @ManyToOne
    @JoinColumn(name = "supplier_category_id")
    public SupplierCategory supplierCategory;

    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    @Override
    public boolean create() {
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
        calcSkuCode();

        return super.create();
    }

    private String calculateFormattedCode(String originalCode) {
        int seqCode = Integer.parseInt(originalCode) + 1;
        int digits = String.valueOf(seqCode).length();
        if (digits < 2) {
            digits = 2;
        }
        return String.format("%0" + digits + "d", seqCode);
    }

    /**
     * 计算SKU编码 【2位类别编码+4位商户编码+2位流水码】
     */
    private void calcSkuCode() {
        Sku sku = Sku.find("supplier.id=? and brand=? and supplierCategory=? and sequenceCode is not null order by cast(sequenceCode as int) desc", this.supplier.id, this.brand, this.supplierCategory).first();
        if (sku == null || StringUtils.isBlank(sku.sequenceCode)) {
            this.sequenceCode = "01";
        } else {
            this.sequenceCode = calculateFormattedCode(sku.sequenceCode);
        }

        Supplier supplier = Supplier.findUnDeletedById(this.supplier.id);
        if (supplier != null && StringUtils.isNotBlank(supplier.code)) {
            this.code = "S" + supplierCategory.code + supplier.sequenceCode + this.sequenceCode;
        }
    }

    /**
     * 货品更新
     *
     * @param id
     * @param sku
     */
    public static void update(Long id, Sku sku) {
        Sku updSku = findById(id);
        updSku.marketPrice = sku.marketPrice;
        updSku.name = sku.name;
        updSku.save();
    }

    public static Sku findUnDeletedById(Long id) {
        return Sku.find("id=? and deleted=?",id,DeletedStatus.UN_DELETED).first();

    }
    public static List<Sku> findUnDeleted() {
        return Sku.find("deleted=?", DeletedStatus.UN_DELETED).fetch();

    }

    public static List<Sku> findShiHuiUnDeleted() {
        return Sku.find("deleted=? and supplier=?", DeletedStatus.UN_DELETED, Supplier.getShihui()).fetch();
    }

    public static void delete(Long id) {
        Sku sku = Sku.findById(id);
        if (sku == null) {
            return;
        }
        sku.deleted = DeletedStatus.DELETED;
        sku.save();
    }

    /**
     * 货品查询
     *
     * @param condition
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public static JPAExtPaginator<Sku> findByCondition(SkuCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<Sku> skuPage = new JPAExtPaginator<>("Sku s", "s", Sku.class, condition.getFilter(), condition.getParamMap());
        skuPage.setPageNumber(pageNumber);
        skuPage.setPageSize(pageSize);
        skuPage.setBoundaryControlsEnabled(false);
        return skuPage;
    }

    public static List<Sku> findByBrand(Long brandId) {
        return Sku.find("deleted=? and brand.id=?", DeletedStatus.UN_DELETED, brandId).fetch();
    }

    /**
     * 获取货品的剩余数量.
     *
     * @return
     */
    @Transient
    public long getRemainCount() {
        Query query = JPA.em().createQuery("SELECT SUM(st.remainCount) FROM InventoryStockItem st where st.sku.id= :skuId and st.deleted != :deleted");
        query.setParameter("skuId", id);
        query.setParameter("deleted", DeletedStatus.DELETED);
        Long count = (Long) query.getSingleResult();
        return count == null ? 0L : count;
    }
}
