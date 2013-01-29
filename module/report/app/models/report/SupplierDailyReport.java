package models.report;

import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/23/13
 * Time: 10:22 AM
 */
public class SupplierDailyReport {
    public String day;
    public Long count = 0l;
//    public BigDecimal amount;

    public SupplierDailyReport(String day, Long count) {
        this.day = day;
        this.count = count;
//        this.amount = amount;
    }

    public static List<SupplierDailyReport> getChartList(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate) {
        EntityManager entityManager = JPA.em();
        StringBuilder sql = new StringBuilder("select new models.report.SupplierDailyReport(date_format(date(consumedAt),'%Y-%m-%d'), count(*)) " +
                "from ECoupon where status=models.order.ECouponStatus.CONSUMED and goods.supplierId=" + supplierId);
        if (goodsId != null && goodsId != 0) {
            sql.append(" and goods.id=" + goodsId);
        }
        if (shopId != null && shopId != 0) {
            sql.append(" and shop.id=" + shopId);
        }
        sql.append(" and consumedAt>=:fromDate and consumedAt<=:toDate group by date(consumedAt) order by date(consumedAt)");

        System.out.println("sql.toString():" + sql.toString());
        Query q = entityManager.createQuery(sql.toString());
        q.setParameter("fromDate", fromDate);
        q.setParameter("toDate", toDate);

        return (List<SupplierDailyReport>) q.getResultList();
    }

    public static Map<String, Long> getChartMap(List<SupplierDailyReport> reports) {
        Map<String, Long> chartMap = new HashMap<>();
        for (SupplierDailyReport report : reports) {

            chartMap.put(report.day, report.count);
        }
        return chartMap;
    }
}
