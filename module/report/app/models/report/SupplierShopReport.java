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
public class SupplierShopReport {
    public String shopName;
    public Long count = 0l;
//    public BigDecimal amount = BigDecimal.ZERO;

    public SupplierShopReport(String shopName, Long count) {
        this.shopName = shopName;
        this.count = count;
//        this.amount = amount;
    }

    public static List<SupplierShopReport> getChartList(Long supplierId, Long goodsId, Date fromDate, Date toDate) {
        EntityManager entityManager = JPA.em();
        StringBuilder sql = new StringBuilder("select new models.report.SupplierShopReport(shop.name, count(*)) " +
                "from ECoupon where status=models.order.ECouponStatus.CONSUMED and goods.supplierId=" + supplierId);
        if (goodsId != null && goodsId != 0) {
            sql.append(" and goods.id=" + goodsId);
        }
        if (fromDate != null) {
            sql.append(" and consumedAt>=:fromDate");
        }
        if (toDate != null) {
            sql.append(" and consumedAt<=:toDate");
        }
        sql.append(" group by shop order by count(*) desc");

        Query q = entityManager.createQuery(sql.toString());
        if (fromDate != null) {
            q.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            q.setParameter("toDate", toDate);
        }
        return (List<SupplierShopReport>) q.getResultList();
    }

    public static Map<String, SupplierShopReport> getChartMap(List<SupplierShopReport> reports) {
        Map<String, SupplierShopReport> chartMap = new HashMap<>();
        for (SupplierShopReport report : reports) {
            chartMap.put(report.shopName, report);
        }
        return chartMap;
    }
}
