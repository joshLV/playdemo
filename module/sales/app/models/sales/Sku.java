package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import org.apache.commons.lang.StringUtils;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-26
 * Time: 下午4:34
 */
@Entity
@Table(name = "sku")
public class Sku extends Model {
    public static final String[] CODE_VALUE = {"99", "999", "9999", "99999", "999999"};

    @ManyToOne
    public Goods goods;

    //货品名称
    @Required
    @MaxSize(value = 500)
    public String name;
    //市场价
    @Required
    @Column(name = "market_price")
    public BigDecimal marketPrice;

    // 库存（初始0）
    public Long stock = 0L;

    /**
     * sku流水码（2位）
     */
    @Column(name = "sequence_code")
    public String sequenceCode;


    // 货品（SKU）编码 【2位类别编码+4位商户编码+2位品牌编码+2位流水码】
    @Column(name = "code")
    public String code;
    // 发货商户
    @Required
    @Column(name = "supplier_id")
    public Long supplierId;

    // 品牌
    @Required
    @ManyToOne
    public Brand brand;

    // 类别
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
     * 获取商品所属的商户信息.
     *
     * @return
     */
    @Transient
    public Supplier getSupplier() {
        if (supplierId == null) {
            return null;
        }
        return Supplier.findUnDeletedById(supplierId);
    }

    @Override
    public boolean create() {
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
        setSkuCode();
        return super.create();
    }

    private String calculateFormattedCode(String originalCode, String digits) {
        return String.format("%0" + digits + "d", Integer.valueOf(originalCode) + 1);
    }

    // SKU编码 【2位类别编码+4位商户编码+2位流水码】
    public void setSkuCode() {
        Sku sku = Sku.find("supplierId=? and brand=? and supplierCategory=? and sequenceCode is not null order by cast(sequenceCode as int) desc", this.supplierId, this.brand, this.supplierCategory).first();
        if (sku == null || StringUtils.isBlank(sku.sequenceCode)) {
            this.sequenceCode = "01";
        } else {
            if (sku.sequenceCode.equals(CODE_VALUE[sku.code.length() - 9])) {
                this.sequenceCode = calculateFormattedCode(sku.sequenceCode, String.valueOf(sku.code.length() - 6));
            } else {
                this.sequenceCode = calculateFormattedCode(sku.sequenceCode, String.valueOf(sku.code.length() - 7));
            }
        }

        Supplier supplier = Supplier.findUnDeletedById(this.supplierId);
        if (supplier != null && StringUtils.isNotBlank(supplier.code)) {
            this.code = "S" + supplierCategory.code + supplier.sequenceCode + this.sequenceCode;
        }
    }

    public static void update(Long id, Sku sku) {
        Sku updSku = findById(id);
        updSku.marketPrice = sku.marketPrice;
        updSku.stock = sku.stock;
        updSku.name = sku.name;
        updSku.save();
    }

    public static List<Sku> findUnDeleted() {
        return Sku.find("deleted=?", DeletedStatus.UN_DELETED).fetch();

    }

    public static void delete(Long id) {
        Sku sku = Sku.findById(id);
        if (sku == null) {
            return;
        }
        sku.deleted = DeletedStatus.DELETED;
        sku.save();
    }


    public static JPAExtPaginator<Sku> findByCondition(SkuCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<Sku> skuPage = new JPAExtPaginator<>("Sku s", "s", Sku.class, condition.getFilter(), condition.getParamMap());
        skuPage.setPageNumber(pageNumber);
        skuPage.setPageSize(pageSize);
        skuPage.setBoundaryControlsEnabled(false);
        return skuPage;
    }
}