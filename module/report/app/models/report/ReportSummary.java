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
    long goodsCount;
    long kindCount;
    long orderCount;

    BigDecimal saleAmount;
    BigDecimal resaleAmount;
    BigDecimal originalAmount;
}
