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
    public String remark;

    /**
     * stock流水码（2位）
     */
    @Column(name = "sequence_code")
    public String sequenceCode;

    @OneToMany(mappedBy = "inventoryStock")
    public List<InventoryStockItem> inventoryStockItemList;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    @Transient
    @Min(0)
    @Match(value = "^[0-9]*$", message = "入库数量格式不对!(i纯数字)")
    public Long stockInCount;

    @Transient
    @Min(0)
    @Money
    public BigDecimal originalPrice;

    @Transient
    public Date effectiveAt;

    @Transient
    public Date expireAt;

    @Transient
    public Sku sku;

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
//        setStockSerialNo();
        System.out.println(this.serialNo + "===this.serialNo>>");
        this.save();
        return super.create();
    }

    private String calculateFormattedCode(String originalCode, String digits) {
        return String.format("%0" + digits + "d", Integer.valueOf(originalCode) + 1);
    }

//    public void setStockSerialNo() {
//        List<InventoryStockItem> stockItemList = InventoryStockItem.find("sku=? ", this.sku).fetch();
//        System.out.println(stockItemList.size() + "===stockItem.size()>>");
//        if (stockItemList.size() == 0) {
//            this.sequenceCode = "01";
//        } else {
//            InventoryStock stock = InventoryStock.find("inventoryStockItemList in ? and sequenceCode is not null order by cast(sequenceCode as int) desc ", stockItemList).first();
//            if (stock == null || StringUtils.isBlank(stock.sequenceCode)) {
//                this.sequenceCode = "01";
//            } else {
//                if (stock.sequenceCode.equals(CODE_VALUE[stock.serialNo.length() - 9])) {
//                    System.out.println(stock.sequenceCode + "===111stock.sequenceCode>>");
//                    this.sequenceCode = calculateFormattedCode(stock.sequenceCode, String.valueOf(stock.serialNo.length() - 6));
//                } else {
//                    System.out.println(stock.sequenceCode + "===2222stock.sequenceCode>>");
//
//                    this.sequenceCode = calculateFormattedCode(stock.sequenceCode, String.valueOf(stock.serialNo.length() - 7));
//                }
//            }
//        }
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat(SERIAL_NO_DATE_FORMAT);
//        this.serialNo = this.actionType.getCode() + dateFormat.format(new Date()) + this.sequenceCode;
//
//        System.out.println(this.serialNo + "===this.serialNo>>");
//    }
}
