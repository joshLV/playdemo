package models.sales;

import play.data.validation.Required;
import play.db.jpa.Model;
import models.supplier.Supplier;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
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
     * 统计到指定时间之前的所有未出库实物订单的Sku出库数量.
     *
     * @param toDate
     * @return
     */
    public static Map<Sku, Long> statisticOutCount(Date toDate) {
        //todo
        return null;
    }
}
