package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.data.validation.*;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 库存变动单.
 * <p/>
 * User: sujie
 * Date: 3/4/13
 * Time: 3:55 PM
 */
@Entity
@Table(name = "inventory_stock")
public class InventoryStock extends Model {
    public static final String SERIAL_NO_DATE_FORMAT = "yyyyMMdd";
    public static final String[] CODE_VALUE = {"99", "999", "9999", "99999", "999999"};


    /**
     * 单号
     * 入库时表示入库单号，出库时表示出库单号
     * 生成规则：库存变动行为类型识别码（1位字母）+日期（8位）+流水号（2位）
     * 例：入库单号：J2013022601
     */
    @Column(name = "serial_no")
    public String serialNo;

    /**
     * 库存变动行为类型
     */
    @Required
    @Column(name = "action_type")
    public StockActionType actionType;

    /**
     * 商户
     */
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    public Supplier supplier;

    /**
     * 创建时间
     * 制表时间
     * 出入库时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 创建人
     * 制表人
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 业务员
     * 销售
     */
    public String saler;

    /**
     * 保管员
     */
    public String storekeeper;

    /**
     * 备注
     */
    @MaxSize(50)
    public String remark;

    /**
     * stock流水码（2位）
     */
    @Column(name = "sequence_code")
    public String sequenceCode;

    /**
     * stock进货单号中的组成：日期(8位)
     */
    @Column(name = "date_of_serial_no")
    public String dateOfSerialNo;

    @OneToMany(mappedBy = "inventoryStock")
    public List<InventoryStockItem> inventoryStockItemList;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Transient
    @Min(0)
    @Match(value = "^[0-9]*$", message = "入库数量格式不对!(纯数字)")
    public Long stockInCount;

    @Transient
    @Min(0)
    @Money
    public BigDecimal originalPrice;

    @Transient
    @Min(0)
    @Match(value = "^[0-9]*$", message = "出库数量格式不对!(纯数字)")
    public Long stockOutCount;

    @Transient
    @Min(0)
    @Money
    public BigDecimal salePrice;

    @Transient
    public Date effectiveAt;

    @Transient
    public Date expireAt;

    @Transient
    public Sku sku;

    @Transient
    public Brand brand;

    /**
     * 统计到指定时间之前的所有未出库实物订单的Sku出库数量.
     *
     * @param toDate
     * @return
     */
    public static Map<Sku, Long> statisticOutCount(Date toDate) {
        //todo
        return null;
    }

    @Override
    public boolean create() {
        this.createdAt = new Date();
        this.deleted = DeletedStatus.UN_DELETED;
        if (this.serialNo == null) {
            setStockSerialNo();
        }
        return super.create();
    }

    private String calculateFormattedCode(String originalCode, String digits) {
        return String.format("%0" + digits + "d", Integer.valueOf(originalCode) + 1);
    }

    public void setStockSerialNo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SERIAL_NO_DATE_FORMAT);
        InventoryStock stock;
        stock = InventoryStock.find("dateOfSerialNo=? and actionType =? order by cast(sequenceCode as int) desc", dateFormat.format(new Date()), this.actionType).first();
        this.serialNo = this.actionType.getCode();
        if (stock == null) {
            this.sequenceCode = "01";
        } else {
            if (stock.sequenceCode.equals(CODE_VALUE[stock.serialNo.length() - 9])) {
                this.sequenceCode = calculateFormattedCode(stock.sequenceCode, String.valueOf(stock.serialNo.length() - 8));
            } else {
                this.sequenceCode = calculateFormattedCode(stock.sequenceCode, String.valueOf(stock.serialNo.length() - 9));
            }
        }

        this.dateOfSerialNo = com.uhuila.common.util.DateUtil.dateToString(new Date(), SERIAL_NO_DATE_FORMAT);
        this.serialNo += com.uhuila.common.util.DateUtil.dateToString(new Date(), SERIAL_NO_DATE_FORMAT) + this.sequenceCode;
    }
}
