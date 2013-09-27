package models.supplier;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商户广告费model
 * <p/>
 * User: yan
 * Date: 13-8-5
 * Time: 下午4:45
 */
@Entity
@Table(name = "supplier_ads_fees")
public class SupplierAdsFee extends Model {
    @ManyToOne
    public Supplier supplier;

    @Required
    @Column(name = "ads_fee")
    @Min(value = 1)
    public BigDecimal adsFee;

    @Required
    @Column(name = "received_at")
    public Date receivedAt;

    @Enumerated(EnumType.STRING)
    public ReceivedType receivedType;//广告费收取类型

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "updated_at")
    public Date updatedAt;

    public String remark;

    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    public static JPAExtPaginator<SupplierAdsFee> getPage(SupplierAdsFeesCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<SupplierAdsFee> adsFeesPage = new JPAExtPaginator<>("SupplierAdsFee s", "s",
                SupplierAdsFee.class, condition.getFitter(), condition.getParamMap()).orderBy("s.id desc");
        adsFeesPage.setPageNumber(pageNumber);
        adsFeesPage.setPageSize(pageSize);
        adsFeesPage.setBoundaryControlsEnabled(false);
        return adsFeesPage;
    }
}
