package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import models.order.Order;
import models.order.OrderItems;
import models.supplier.Supplier;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "action")
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


    @OneToMany(mappedBy = "stock")
    public List<InventoryStockItem> inventoryStockItemList;
    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted = DeletedStatus.UN_DELETED;

    /**
     * 乐观锁
     */
    @Column(name = "lock_version")
    @Version
    public int lockVersion;

    /**
     * 统计未出库实物订单的Sku出库数量.
     *
     * @param takeoutSkuMap     待出库sku及数量
     * @param deficientOrderMap 因缺货而无法发货的待发货订单
     * @return
     */
    public static Map<Sku, Long> statisticOutCount(Map<Sku, Long> preparingTakeoutSkuMap, List<Order> deficientOrders) {
        Map<Sku, Long> takeoutSkuMap = new HashMap<>();
        for (Sku sku : preparingTakeoutSkuMap.keySet()) {
            takeoutSkuMap.put(sku, preparingTakeoutSkuMap.get(sku));
        }
        for (Order deficientOrder : deficientOrders) {
            //减少出库数量
            for (OrderItems orderItem : deficientOrder.orderItems) {
                Long count = takeoutSkuMap.get(orderItem.goods.sku);

                if (count == null) {
                    continue;
                }
                takeoutSkuMap.put(orderItem.goods.sku, count - orderItem.getSkuCount());
            }

        }

        return takeoutSkuMap;
    }

    /**
     * 根据缺货商品数，返回相应的因缺货而无法发货的待发货订单.
     *
     * @param takeoutSkuMap          待出库sku及数量
     * @param deficientOrderItemList 缺货货品对应的订单项
     * @param toDate                 截止时间
     * @return
     */
    public static Map<Sku, List<Order>> getDeficientOrders(List<OrderItems> deficientOrderItemList) {
        Map<Sku, List<Order>> deficientOrderMap = new HashMap<>(); //因缺货而无法发货的待发货订单

        //提取因缺货而无法发货的订单
        for (OrderItems orderItem : deficientOrderItemList) {
            List<Order> orderList = deficientOrderMap.get(orderItem.goods.sku);
            if (orderList == null) {
                List<Order> orders = new ArrayList<>();
                orders.add(orderItem.order);
                deficientOrderMap.put(orderItem.goods.sku, orders);
            } else if (!orderList.contains(orderItem.order)) {
                orderList.add(orderItem.order);
            }
        }
        return deficientOrderMap;
    }

    /**
     * 根据缺货商品数，返回相应的因缺货而无法发货的待发货订单.
     *
     * @param toDate 截止时间
     * @return
     */
    public static List<OrderItems> getDeficientOrderItemList(Map<Sku, Long> preparingTakeoutSkuMap, Date toDate) {
        //统计总的待出库货品及数量
        Map<Sku, Long> deficientCountMap = new HashMap<>(); //缺货的货品及数量
        //检查缺货情况
        for (Sku sku : preparingTakeoutSkuMap.keySet()) {
            Long outCount = preparingTakeoutSkuMap.get(sku);  //待出库数量
            long remainCount = sku.getRemainCount();

            if (outCount != null && outCount > 0 && remainCount < outCount) {
                deficientCountMap.put(sku, outCount - remainCount);
            }
        }
        if (deficientCountMap.size() == 0) {
            return new ArrayList<>();
        }
        //提取缺货货品对应的所有需发货的订单项
        return OrderItems.findPaid(deficientCountMap, toDate);
    }

    public static List<Order> getOrderListByItem(List<OrderItems> orderItems) {
        List<Order> orderList = new ArrayList<>();
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

    public static void createInventoryStockItem(Sku sku, Long count, InventoryStock stock, BigDecimal price) {
        InventoryStockItem stockItem = new InventoryStockItem(stock);
        stockItem.changeCount = count;
        stockItem.stock = stock;
        stockItem.price = price;
        stockItem.sku = sku;
        stockItem.create();
    }

    public static void updateInventoryStockRemainCount(Sku sku, Long changeCount) {
        List<InventoryStockItem> stockItemList = InventoryStockItem.find("sku=? and remainCount>0 order by createdAt desc", sku).fetch();
        long reducedCount = changeCount;
        for (InventoryStockItem stockItem : stockItemList) {
            if (stockItem.remainCount >= reducedCount) {
                stockItem.remainCount -= reducedCount;
                stockItem.save();
                return;
            }
            stockItem.remainCount = 0L;
            stockItem.save();
            reducedCount -= stockItem.remainCount;
        }
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
        String dateOfSerialNo = com.uhuila.common.util.DateUtil.dateToString(new Date(), SERIAL_NO_DATE_FORMAT);
        String sequenceCode;
        InventoryStock stock = InventoryStock.find("serialNo like ? and actionType =? order by id desc", "_" + dateOfSerialNo + "__", this.actionType).first();
        if (stock == null) {
            sequenceCode = "01";
        } else {
            if (stock.serialNo.substring(9, stock.serialNo.length()).equals(CODE_VALUE[stock.serialNo.length() - 9])) {
                sequenceCode = calculateFormattedCode(stock.serialNo.substring(9, stock.serialNo.length()), String.valueOf(stock.serialNo.length() - 8));
            } else {
                sequenceCode = calculateFormattedCode(stock.serialNo.substring(9, stock.serialNo.length()), String.valueOf(stock.serialNo.length() - 9));
            }
        }
        this.serialNo = this.actionType.getCode() + dateOfSerialNo + sequenceCode;
    }


}
