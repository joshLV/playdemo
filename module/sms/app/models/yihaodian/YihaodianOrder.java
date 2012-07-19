package models.yihaodian;

import org.hibernate.annotations.Index;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 */

@Table(name = "yihaodian_order")
public class YihaodianOrder extends Model {
    @Column(name = "order_number")
    public String orderNumber;

    @Column(name = "goods_number")
    public String goodsNumber;

    public int count;

    public BigDecimal amount;

    public String phone;

    public String sender;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "user_name")
    public String userName;

    public String message;

    public String status;

    @Column(name = "pending_actions")
    public String pendingActions;

    @Column(name = "retry_times")
    public int retryTimes;

    //对应我们系统中的属性
    @Index(name = "seewi_goods_id")
    @Column(name = "seewi_goods_id")
    public Long seewiGoodsId;

    @Column(name = "seewi_order_id")
    public Long seewiOrderId;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    public YihaodianOrder(){
        this.createdAt = new Date();
        this.pendingActions = "";
        retryTimes = 0;
    }
}
