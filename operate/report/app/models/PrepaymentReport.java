package models;

import com.uhuila.common.constants.DeletedStatus;
import models.order.ECoupon;
import models.order.OrderItems;
import models.order.Prepayment;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ModelPaginator;
import play.modules.paginate.SimplePaginator;
import play.modules.paginate.ValuePaginator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * 预付款明细报表.
 * <p/>
 * User: sujie
 * Date: 11/27/12
 * Time: 2:22 PM
 */

public class PrepaymentReport implements Serializable {
    public Prepayment prepayment;
    public String supplierName;
    public BigDecimal amount;  //预付款金额
    public BigDecimal soldAmount;  //已销售金额
    public BigDecimal consumedAmount;        //已消费金额
    public BigDecimal availableBalance;     //可用预付款
    public Date effectiveAt;//预付款开始日
    public Date expireAt;//预付款到期日
    public BigDecimal consumedProcess;//消费进度
    public BigDecimal timeProcess = BigDecimal.ZERO;//时间进度
    public BigDecimal processCompare = BigDecimal.ZERO;//进度比较（消费/时间）

    public boolean isExpired() {
        return expireAt != null && expireAt.before(new Date());
    }

    public PrepaymentReport(Prepayment prepayment) {
        if (prepayment == null || prepayment.supplier == null) {
            return;
        }
        this.prepayment = prepayment;
        this.supplierName = (StringUtils.isNotBlank(prepayment.supplier.otherName)) ?
                prepayment.supplier.fullName + "(" + prepayment.supplier.otherName + ")"
                : prepayment.supplier.fullName;
        this.amount = prepayment.amount;
        this.effectiveAt = prepayment.effectiveAt;
        this.expireAt = prepayment.expireAt;
    }

    private static final String DEFAULT_ORDER_BY = "supplier, createdAt";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static SimplePaginator<PrepaymentReport> getPage(Long supplierId, int isEffective, String orderBy, int pageNumber, int pageSize) {
        if (StringUtils.isBlank(orderBy)) {
            return getPage(supplierId, isEffective, pageNumber, pageSize);
        }

        List<Prepayment> prepayments;
        if (supplierId != null && supplierId > 0) {
            if (isEffective > 0) {
                prepayments = Prepayment.find("deleted = ? and expireAt>? and supplier.id=?",
                        DeletedStatus.UN_DELETED,
                        new Date(),
                        supplierId).fetch();
            } else {
                prepayments = Prepayment.find("deleted = ? and supplier.id=?", DeletedStatus.UN_DELETED,
                        supplierId).fetch();
            }
        } else {
            if (isEffective > 0) {
                prepayments = Prepayment.find("deleted = ? and expireAt>? ",
                        DeletedStatus.UN_DELETED,
                        new Date()).fetch();
            } else {
                prepayments = Prepayment.find("deleted = ? ", DeletedStatus.UN_DELETED).fetch();
            }
        }
        List<PrepaymentReport> reportList = createPrepaymentReportList(prepayments);
        if (orderBy.equals("asc")) {
            Collections.sort(reportList, new Comparator<PrepaymentReport>() {
                @Override
                public int compare(PrepaymentReport o1, PrepaymentReport o2) {
                    return o1.processCompare.compareTo(o2.processCompare);
                }
            });
        } else {
            Collections.sort(reportList, new Comparator<PrepaymentReport>() {
                @Override
                public int compare(PrepaymentReport o1, PrepaymentReport o2) {
                    return o2.processCompare.compareTo(o1.processCompare);
                }
            });
        }
        ValuePaginator<PrepaymentReport> page = new ValuePaginator<>(reportList);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        SimplePaginator<PrepaymentReport> reportPage = new SimplePaginator<>(page.getCurrentPage());
        reportPage.setRowCount(page.getRowCount());
        reportPage.setPageNumber(pageNumber);
        reportPage.setPageSize(pageSize);
        return reportPage;
    }

    public static SimplePaginator<PrepaymentReport> getPage(Long supplierId, int isEffective, int pageNumber, int pageSize) {
        ModelPaginator<Prepayment> page;
        if (supplierId != null && supplierId > 0) {
            if (isEffective > 0) {
                page = new ModelPaginator<>(Prepayment.class, "deleted = ? and expireAt>? and supplier.id=?",
                        DeletedStatus.UN_DELETED,
                        new Date(),
                        supplierId).orderBy(DEFAULT_ORDER_BY);
            } else {
                page = new ModelPaginator<>(Prepayment.class, "deleted = ? and supplier.id=?", DeletedStatus.UN_DELETED,
                        supplierId).orderBy(DEFAULT_ORDER_BY);
            }
        } else {
            if (isEffective > 0) {
                page = new ModelPaginator<>(Prepayment.class, "deleted = ? and expireAt>? ",
                        DeletedStatus.UN_DELETED,
                        new Date()).orderBy(DEFAULT_ORDER_BY);
            } else {
                page = new ModelPaginator<>(Prepayment.class, "deleted = ? ", DeletedStatus.UN_DELETED).orderBy(DEFAULT_ORDER_BY);
            }
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);

        List<PrepaymentReport> currentPage = createPrepaymentReportList(page);

        SimplePaginator<PrepaymentReport> reportPage = new SimplePaginator<>(currentPage);
        reportPage.setRowCount(page.getRowCount());
        reportPage.setPageNumber(pageNumber);
        reportPage.setPageSize(pageSize);

        return reportPage;
    }

    private static List<PrepaymentReport> createPrepaymentReportList(List<Prepayment> prepayments) {
        List<PrepaymentReport> reportList = new ArrayList<>();
        for (Prepayment prepayment : prepayments) {
            PrepaymentReport report = new PrepaymentReport(prepayment);
            report.soldAmount = OrderItems.getSoldAmount(prepayment);
            report.consumedAmount = ECoupon.getConsumedAmount(prepayment);
            report.availableBalance = prepayment.getAvailableBalance(report.consumedAmount);

            if (report.amount.compareTo(BigDecimal.ZERO) > 0) {
                report.consumedProcess = report.consumedAmount.divide(report.amount, 4, RoundingMode.CEILING).multiply(ONE_HUNDRED);
            }
            if (prepayment.expireAt != null && prepayment.effectiveAt != null) {
                final BigDecimal pastTimeLen = new BigDecimal(new Date().getTime() - report.effectiveAt.getTime());
                final BigDecimal effectiveTimeLen = new BigDecimal(report.expireAt.getTime() - report.effectiveAt.getTime());
                if (effectiveTimeLen.compareTo(BigDecimal.ZERO)>0){
                    report.timeProcess = pastTimeLen.divide(effectiveTimeLen, 4, RoundingMode.CEILING).multiply(ONE_HUNDRED);
                }
                if (report.timeProcess.compareTo(ONE_HUNDRED) > 0) {
                    report.timeProcess = ONE_HUNDRED;
                }
            }

            if (report.timeProcess != null && report.timeProcess.compareTo(BigDecimal.ZERO) > 0) {
                report.processCompare = report.consumedProcess.divide(report.timeProcess, 4, RoundingMode.CEILING).multiply(ONE_HUNDRED);
            }
            reportList.add(report);
        }
        return reportList;
    }
}
