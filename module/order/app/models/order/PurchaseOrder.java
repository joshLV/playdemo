package models.order;

import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购合同
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 下午2:56
 */
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder extends Model {
    public static final String SERIAL_NO_DATE_FORMAT = "yyyyMMdd";

    /**
     * 合同编号
     * 生成合同编号（打印时用） CG+添加时间+2位流水号
     * 例：合同编号：CG2013032601
     */
    @Column(name = "serial_no")
    public String serialNo;

    /**
     * 出售方
     */
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    public Vendor vendor;

    /**
     * 开票方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type")
    public InvoiceType invoiceType;

    /**
     * 支付方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    public PaymentType paymentType;

    /**
     * 签约日期
     */
    @Column(name = "signed_at")
    public Date signedAt;

    @OneToMany(mappedBy = "purchaseOrder")
    public List<PurchaseItem> purchaseItems;

    /**
     * 创建人
     * 制单人
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 修改时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;


    /**
     * 修改人
     */
    @Column(name = "updated_by")
    public String updatedBy;

    /**
     * 创建时间
     * 制表时间
     */
    @Column(name = "created_at")
    public Date createdAt = new Date();

    /**
     * 删除状态
     */
    @Enumerated(EnumType.ORDINAL)
    public com.uhuila.common.constants.DeletedStatus deleted;


    @Override
    public boolean create() {
//        deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        createdAt = new Date();
        if (this.serialNo == null) {
            resetStockSerialNo();
        }
        return super.create();
    }

    public static void update(Long id, PurchaseOrder purchaseOrder) {
        PurchaseOrder updatedPurchaseOrder = PurchaseOrder.findById(id);
        updatedPurchaseOrder.refresh();

        updatedPurchaseOrder.save();
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


    public static List<PurchaseOrder> findByCondition(String keyword) {
        StringBuilder sql = new StringBuilder("deleted= :deleted");
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("deleted", com.uhuila.common.constants.DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(keyword)) {
            sql.append(" and (vendor.name like :vendorName or serialNo like :serialNo )");
            paramsMap.put("vendorName", "%" + keyword + "%");
            paramsMap.put("serialNo", "%" + keyword + "%");
        }
        return find(sql.toString(), paramsMap).fetch();
    }

    private String calculateFormattedCode(String originalCode) {
        int seqCode = Integer.parseInt(originalCode) + 1;
        int digits = String.valueOf(seqCode).length();
        if (digits < 2) {
            digits = 2;
        }
        return String.format("%0" + digits + "d", seqCode);
    }

    public void resetStockSerialNo() {
        String dateOfSerialNo = com.uhuila.common.util.DateUtil.dateToString(new Date(), SERIAL_NO_DATE_FORMAT);
        String sequenceCode;
        PurchaseOrder purchaseOrder = PurchaseOrder.find("serialNo like ? order by id desc", "__" + dateOfSerialNo + "__").first();
        if (purchaseOrder == null) {
            sequenceCode = "01";
        } else {
            sequenceCode = calculateFormattedCode(purchaseOrder.serialNo.substring(10, purchaseOrder.serialNo.length()));

        }
        this.serialNo = "CG" + dateOfSerialNo + sequenceCode;
    }

}

