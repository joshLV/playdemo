package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.order.Order;
import models.order.OrderItems;
import models.supplier.Supplier;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    public Date createdAt = new Date();

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
    public DeletedStatus deleted = DeletedStatus.UN_DELETED;

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
     * @param takeoutSkuMap 待出库sku及数量
     * @param stockOrderMap 因缺货而无法发货的待发货订单
     * @return
     */
    public static Map<Sku, Long> statisticOutCount(Map<Sku, Long> takeoutSkuMap, Map<Sku, List<Order>> stockOrderMap) {
        for (List<Order> orders : stockOrderMap.values()) {
            for (Order order : orders) {
                //减少出库数量
                for (OrderItems orderItem : order.orderItems) {
                    Long count = takeoutSkuMap.get(orderItem.goods.sku);
                    takeoutSkuMap.put(orderItem.goods.sku, count - orderItem.buyNumber);
                }

            }
        }

        return takeoutSkuMap;
    }

    /**
     * 根据缺货商品数，返回相应的因缺货而无法发货的待发货订单.
     *
     * @param takeoutSkuMap 待出库sku及数量
     * @param toDate        截止时间
     * @return
     */
    public static Map<Sku, List<Order>> getDeficientOrders(Map<Sku, Long> takeoutSkuMap, Date toDate) {
        Map<Sku, List<Order>> stockOrderMap = new HashMap<>(); //因缺货而无法发货的待发货订单
        Map<Sku, Long> stockout = new HashMap<>(); //缺货的货品及数量
        //检查缺货情况
        for (Sku sku : takeoutSkuMap.keySet()) {
            Long outCount = takeoutSkuMap.get(sku);  //待出库数量
            if (outCount != null && outCount > 0 && sku.stock < outCount) {
                stockout.put(sku, outCount - sku.stock);
            }
        }
        if (stockout.size() == 0) {
            return stockOrderMap;
        }
        //提取缺货货品对应的所有需发货的订单项
        List<OrderItems> orderItems = OrderItems.findPaid(stockout, toDate);
        //提取因缺货而无法发货的订单
        for (OrderItems orderItem : orderItems) {
            List<Order> orderList = stockOrderMap.get(orderItem.goods.sku);
            if (orderList == null) {
                List<Order> orders = new ArrayList<>();
                orders.add(orderItem.order);
                stockOrderMap.put(orderItem.goods.sku, orders);
            } else {
                orderList.add(orderItem.order);
            }
        }
        return stockOrderMap;
    }

    /**
     * 根据缺货商品数，返回相应的因缺货而无法发货的待发货订单.
     *
     * @param toDate 截止时间
     * @return
     */
    public static List<Order> getDeficientOrderList(Date toDate) {
        List<Order> orderList = new ArrayList<>();

        //统计总的待出库货品及数量
        Map<Sku, Long> takeoutSkuMap = OrderItems.findTakeout(toDate);
        Map<Sku, Long> stockout = new HashMap<>(); //缺货的货品及数量
        //检查缺货情况
        for (Sku sku : takeoutSkuMap.keySet()) {
            Long outCount = takeoutSkuMap.get(sku);  //待出库数量
            if (outCount != null && outCount > 0 && sku.stock < outCount) {
                stockout.put(sku, outCount - sku.stock);
            }
        }
        if (stockout.size() == 0) {
            return new ArrayList<>();
        }
        //提取缺货货品对应的所有需发货的订单项
        List<OrderItems> orderItems = OrderItems.findPaid(stockout, toDate);
        //提取因缺货而无法发货的订单
        Map<Long, Order> orderMap = new HashMap<>();
        for (OrderItems orderItem : orderItems) {
            if (orderMap.containsKey(orderItem.order.id)) {
                continue;
            }
            orderMap.put(orderItem.order.id, orderItem.order);
            orderList.add(orderItem.order);
        }
        return orderList;
    }

    private static final String SYSTEM_USER = "system"; //系统自动创建的用户

    public static InventoryStock createInventoryStock(Supplier supplier, String storekeeper) {
        InventoryStock stock = new InventoryStock();
        stock.actionType = StockActionType.OUT;
        stock.storekeeper = storekeeper;
        stock.supplier = supplier;
        stock.createdBy = SYSTEM_USER;
        stock.create();
        return stock;
    }

    public static void createInventoryStockItem(Sku sku, Long count, InventoryStock stock) {
        InventoryStockItem stockItem = new InventoryStockItem(stock);
        stockItem.changeCount = count;
        stockItem.inventoryStock = stock;
        stockItem.sku = sku;
        stockItem.create();
    }

    public static void updateInventoryStockRemainCount(Sku sku, Long aLong) {

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
        this.dateOfSerialNo = com.uhuila.common.util.DateUtil.dateToString(new Date(), SERIAL_NO_DATE_FORMAT);

        InventoryStock stock = InventoryStock.find("dateOfSerialNo=? and actionType =? order by cast(sequenceCode as int) desc", this.dateOfSerialNo, this.actionType).first();
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
        this.serialNo += this.dateOfSerialNo + this.sequenceCode;
    }


}
