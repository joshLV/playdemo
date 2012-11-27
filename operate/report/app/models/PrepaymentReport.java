package models;

import com.uhuila.common.constants.DeletedStatus;
import models.order.ECoupon;
import models.order.OrderItems;
import models.order.Prepayment;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import play.modules.paginate.SimplePaginator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 预付款明细报表.
 * <p/>
 * User: sujie
 * Date: 11/27/12
 * Time: 2:22 PM
 */
public class PrepaymentReport extends Model {
    public String supplierName;
    public BigDecimal amount;  //预付款金额
    public BigDecimal soldAmount;  //已销售金额
    public BigDecimal consumedAmount;        //已消费金额
    public BigDecimal balance;     //预付款余额
    public Date effectiveAt;//预付款开始日
    public Date expireAt;//预付款到期日
    public BigDecimal consumedProcess;//消费进度
    public BigDecimal timeProcess;//时间进度
    public BigDecimal processCompare;//进度比较（消费/时间）

    public PrepaymentReport(Prepayment prepayment) {
        if (prepayment == null || prepayment.supplier == null) {
            return;
        }
        this.supplierName = (StringUtils.isNotBlank(prepayment.supplier.otherName)) ?
                prepayment.supplier.fullName + "(" + prepayment.supplier.otherName + ")"
                : prepayment.supplier.fullName;
        this.amount = prepayment.amount;
        this.balance = prepayment.getBalance();
        this.effectiveAt = prepayment.effectiveAt;
        this.expireAt = prepayment.expireAt;
    }

    private static final String DEFAULT_ORDER_BY = "supplier,createdAt";

    public static SimplePaginator<PrepaymentReport> getPage(Long supplierId, String orderBy, int pageNumber, int pageSize) {
        ModelPaginator<Prepayment> page;
        if (supplierId != null && supplierId > 0) {
            page = new ModelPaginator<>(Prepayment.class, "deleted = ? and supplier.id=?", DeletedStatus.UN_DELETED,
                    supplierId).orderBy(DEFAULT_ORDER_BY);
        } else {
            page = new ModelPaginator<>(Prepayment.class, "deleted = ? ", DeletedStatus.UN_DELETED).orderBy(DEFAULT_ORDER_BY);
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);

        List<PrepaymentReport> currentPage = new ArrayList<>();
        for (Prepayment prepayment : page) {
            PrepaymentReport report = new PrepaymentReport(prepayment);
            report.soldAmount = OrderItems.getSoldAmount(prepayment);
            report.consumedAmount = ECoupon.getConsumedAmount(prepayment);
            if (report.amount.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("report.consumedAmount:" + report.consumedAmount);
                System.out.println("report.amount:" + report.amount);
                report.consumedProcess = report.consumedAmount.divide(report.amount, 4, RoundingMode.CEILING).multiply(new BigDecimal(100));
            }
            if (prepayment.expireAt != null && prepayment.effectiveAt != null) {
                report.timeProcess = new BigDecimal((new Date().getTime() - report.effectiveAt.getTime())
                        / (report.effectiveAt.getTime() - report.expireAt.getTime()));
            }

            if (report.timeProcess != null && report.timeProcess.compareTo(BigDecimal.ZERO) > 0) {
                report.processCompare = report.consumedProcess.divide(report.timeProcess, 4, RoundingMode.CEILING).multiply(new BigDecimal(100));
            }
            currentPage.add(report);
        }

        SimplePaginator<PrepaymentReport> reportPage = new SimplePaginator<>(currentPage);
        reportPage.setRowCount(page.getRowCount());
        reportPage.setPageNumber(pageNumber);
        reportPage.setPageSize(pageSize);

        return reportPage;
    }
}
