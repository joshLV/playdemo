package models.yihaodian.groupbuy;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-12
 */
@Entity
@Table(name = "yhd_group_buy_order")
public class YHDGroupBuyOrder extends Model {
    @Column(name = "ybq_order_id", unique = true)
    public Long ybqOrderId;

    @Column(name = "order_code", unique = true)
    public String orderCode;

    @Column(name = "product_id")
    public Long productId;

    @Column(name = "product_num")
    public Integer productNum;

    @Column(name = "order_amount")
    public BigDecimal orderAmount;

    @Column(name = "create_time")
    public Date createTime;

    @Column(name = "paid_time")
    public Date paidTime;

    @Column(name = "user_phone")
    public String userPhone;

    @Column(name = "product_price")
    public BigDecimal productPrice;

    @Column(name = "group_id")
    public Long groupId;

    @Column(name = "outer_group_id")
    public String outerGroupId;

    @Column(name = "status")
    public YHDGroupBuyOrderStatus status;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;

    public YHDGroupBuyOrder(){
        this.status = YHDGroupBuyOrderStatus.ORDER_COPY;
        this.lockVersion = 0;
        this.createdAt = new Date();
    }
}
