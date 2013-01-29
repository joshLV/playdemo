package models.report;

import org.apache.commons.collections.CollectionUtils;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/22/13
 * Time: 4:51 PM
 */
public class SupplierGoodsReport {
    public String goodsName;
    public Long count = 0l;
    public BigDecimal amount = BigDecimal.ZERO;

    public SupplierGoodsReport(String goodsName, Long count, BigDecimal amount) {
        this.goodsName = goodsName;
        this.count = count;
        this.amount = amount;
    }

    public static List<SupplierGoodsReport> getSupplierGoodsReport(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate) {
        EntityManager entityManager = JPA.em();
        StringBuilder sql = new StringBuilder("select new models.report.SupplierGoodsReport(goods.shortName, count(*), sum(faceValue)) " +
                "from ECoupon where status=models.order.ECouponStatus.CONSUMED and goods.supplierId=" + supplierId);
        if (goodsId != null && goodsId != 0) {
            sql.append(" and goods.id=" + goodsId);
        }
        if (shopId != null && shopId != 0) {
            sql.append(" and shop.id=" + shopId);
        }
        if (fromDate != null) {
            sql.append(" and consumedAt>=:fromDate");
        }
        if (toDate != null) {
            sql.append(" and consumedAt<=:toDate");
        }
        sql.append(" group by goods order by count(*) desc");
        Query q = entityManager.createQuery(sql.toString());
        if (fromDate != null) {
            q.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            q.setParameter("toDate", toDate);
        }
        return (List<SupplierGoodsReport>) q.getResultList();
    }

    public static Map<String, SupplierGoodsReport> getChartMap(List<SupplierGoodsReport> goodsReportList) {
        Map<String, SupplierGoodsReport> chartMap = new HashMap<>();
        if (CollectionUtils.isEmpty(goodsReportList)) {
            return chartMap;
        }
        for (SupplierGoodsReport supplierGoodsReport : goodsReportList) {
            chartMap.put(supplierGoodsReport.goodsName, supplierGoodsReport);
        }
        return chartMap;
    }
}
