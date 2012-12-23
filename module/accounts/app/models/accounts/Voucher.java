package models.accounts;


import com.uhuila.common.constants.DeletedStatus;
import models.consumer.User;
import models.order.Order;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author likang
 *         Date: 12-12-12
 */
@Entity
@Table(name = "vouchers")
public class Voucher extends Model {

    private static final String DECIMAL_FORMAT = "00000";
    /**
     * 序列号.
     * 保存为英文大写和数字。
     * <p/>
     * 录入时，编辑可以指定一个前缀，然后生成时用前缀加数字序号即可.
     * 可以这样做：
     * DecimalFormat myFormatter = new DecimalFormat("00000");
     * String serialNo = prefix + myFormatter.format(22);
     */
    @Column(name = "serial_no", unique = true)
    public String serialNo;

    @Column(name = "prefix")
    public String prefix;

    /**
     * 充值码.
     * 伪随机生成的15位数字
     */
    @Column(name = "charge_code", unique = true)
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
    @Column(name = "lock_version")
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

    @Transient
    public String operatorName;

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

    @Column(name = "voucher_type")
    @Enumerated(EnumType.STRING)
    public VoucherType voucherType;
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


    public static void generate(int count, BigDecimal faceValue, String name, String prefix, Account account, Long operatorId, VoucherType type, Date expiredAt) {
        Random random = new Random();
        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);

        for (int i = 0; i < count; i++) {
            String chargeCode = decimalFormat.format(random.nextInt(100000)) +
                    decimalFormat.format(random.nextInt(100000)) +
                    decimalFormat.format(random.nextInt(100000));

            Voucher voucher = new Voucher();
            voucher.chargeCode = chargeCode;
            voucher.value = faceValue;
            voucher.name = name;
            voucher.account = account;
            if (voucher.account != null) {
                voucher.assignedAt = new Date();
            }
            voucher.prefix = prefix;
            voucher.serialNo = prefix + decimalFormat.format(i + 1);
            voucher.operatorId = operatorId;
            voucher.voucherType = type;
            voucher.expiredAt = expiredAt;
            voucher.save();
        }
    }


    /**
     * 删除状态。
     * 没有被充值的记录才可被删除，删除后不可再被使用.
     */
    @Enumerated
    public DeletedStatus deleted;

    public Voucher() {
        createdAt = new Date();
        deleted = DeletedStatus.UN_DELETED;
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

    public static List<Voucher> validVouchers(Account account) {
        return Voucher.find("account = ? and expiredAt > ? and deleted = ? and order = null order by expiredAt ",
                account, new Date(), DeletedStatus.UN_DELETED).fetch();
    }

    public static long validVoucherCount(Account account) {
        return Voucher.count("account = ? and expiredAt > ? and deleted = ? and order = null",
                account, new Date(), DeletedStatus.UN_DELETED);
    }

    public static List<Voucher> usedInOrder(Long orderId) {
        return Voucher.find("order != null and order.id = ?", orderId).fetch();
    }

    public static boolean canAssign(Account account, Voucher voucher) {
        if (!voucher.account.getId().equals(account.getId())) {
            return false;
        }
        if (voucher.expiredAt.before(new Date())) {
            return false;
        }
        if (voucher.deleted == DeletedStatus.DELETED) {
            return false;
        }
        if (voucher.order != null) {
            return false;
        }
        return true;
    }
}
