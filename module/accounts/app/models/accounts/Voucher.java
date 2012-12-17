package models.accounts;


import com.uhuila.common.constants.DeletedStatus;
import models.accounts.Account;
import models.order.Order;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 * Date: 12-12-12
 */
@Entity
@Table(name = "vouchers")
public class Voucher extends Model {

    /**
     * 序列号.
     * 保存为英文大写和数字。
     *
     * 录入时，编辑可以指定一个前缀，然后生成时用前缀加数字序号即可.
     * 可以这样做：
     *      DecimalFormat myFormatter = new DecimalFormat("00000");
     *      String serialNo = prefix + myFormatter.format(22);
     */
    @Column(name="serial_no", unique = true)
    public String serialNo;

    @Column(name = "prefix")
    public String prefix;

    /**
     * 充值码.
     * 伪随机生成的15位数字
     */
    @Column(name="charge_code", unique = true)
    public String chargeCode;

    /**
     * 关联的账户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = true)
    public Account account;

    /**
     * 价值
     */
    @Column(name = "value")
    public BigDecimal value;

    /**
     * 关联的订单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;

    @Version
    @Column(name="lock_version")
    public int lockVersion;

    /**
     * 卡片名称
     */
    @Column(name = "name")
    public String name;

    /**
     * 创建者ID，为运营后台登录ID.
     */
    @Column(name = "operator_id")
    public Long operatorId;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 分配时间
     */
    @Column(name = "assigned_at")
    public Date assignedAt;

    /**
     * 使用时间
     */
    @Column(name = "used_at")
    public Date usedAt;

    /**
     * 过期时间
     */
    @Column(name = "expired_at")
    public Date expiredAt;

    /**
     * 删除状态。
     * 没有被充值的记录才可被删除，删除后不可再被使用.
     */
    @Enumerated
    public DeletedStatus deleted;

    public Voucher() {
        createdAt = new Date();
        lockVersion = 0;
    }

    public static JPAExtPaginator<Voucher> findByCondition(
            VoucherCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<Voucher> page = new JPAExtPaginator<>(
                null, null, Voucher.class, condition.getFilter(), condition.getParams());

        page.orderBy("createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
