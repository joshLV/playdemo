package models.report;

import play.db.jpa.Model;

import java.math.BigDecimal;

/**
 * 报表汇总信息.
 * <p/>
 * User: sujie
 * Date: 5/16/12
 * Time: 6:02 PM
 */
public class ReportSummary extends Model {
    public long goodsCount;
    public long kindCount;
    public long orderCount;

    public BigDecimal saleAmount;
    public BigDecimal resaleAmount;
    public BigDecimal originalAmount;

    public BigDecimal tax;
    public BigDecimal noTaxAmount;


    public ReportSummary() {

    }

    public ReportSummary(long goodsCount,long orderCount,BigDecimal originalAmount) {
        this.goodsCount = goodsCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
    }

    public ReportSummary(long goodsCount,long orderCount,BigDecimal originalAmount,BigDecimal tax,
                         BigDecimal noTaxAmount) {
        this.goodsCount = goodsCount;
        this.orderCount = orderCount;
        this.originalAmount = originalAmount;
        this.tax = tax;
        this.noTaxAmount = noTaxAmount;
    }
}
