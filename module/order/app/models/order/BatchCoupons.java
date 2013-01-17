package models.order;

import com.uhuila.common.constants.DeletedStatus;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 批量导出券
 * <p/>
 * User: wangjia
 * Date: 12-11-21
 * Time: 下午12:53
 */
@Entity
@Table(name = "batch_coupons")
public class BatchCoupons extends Model {

    public String name;

    @Column(name = "goods_name")
    public String goodsName;

    @Transient
    public String prefix;

    @Column(name = "created_at")
    public Date createdAt;

    public int count;

    /**
     * 创建者ID，为运营后台登录ID.
     */
    @Column(name = "operator_id")
    public Long operatorId;

    @Transient
    public String operatorName;

    @Version
    @Column(name = "lock_version")
    public int lockVersion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "batchCoupons")
    @OrderBy("id")
    public List<ECoupon> coupons;

    public BatchCoupons() {
        lockVersion = 0;
        createdAt = new Date();
    }

    public static JPAExtPaginator<BatchCoupons> findByCondition(
            BatchCouponsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<BatchCoupons> page = new JPAExtPaginator<>(
                null, null, BatchCoupons.class, condition.getFilter(), condition.getParams());

        page.orderBy("createdAt DESC");
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

}
