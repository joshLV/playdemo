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
    public Long goodsId;
    public String goodsName;
    public Long count = 0l;
    public Long soldCount = 0l;
    public Long refundCount = 0l;
    public BigDecimal amount = BigDecimal.ZERO;

    public SupplierGoodsReport(Long goodsId, String goodsName, Long count, BigDecimal amount) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.count = count;
        this.amount = amount;
    }

    public SupplierGoodsReport(Long goodsId, String goodsName, Long soldCount) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.soldCount = soldCount;
    }

    public SupplierGoodsReport(Long goodsId, String goodsName, Integer soldCount, Long refundCount) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.refundCount = refundCount;
    }

    public static List<SupplierGoodsReport> getGoodsSellingReport(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate) {
        List<SupplierGoodsReport> consumedCountList = getSupplierGoodsReport(supplierId, goodsId, shopId, fromDate, toDate);
        List<SupplierGoodsReport> soldCountList = getSoldCountList(supplierId, goodsId, shopId, fromDate, toDate);
        boolean found = false;
        for (SupplierGoodsReport soldCountReport : soldCountList) {
            for (SupplierGoodsReport consumedCountReport : consumedCountList) {
                if (!found && consumedCountReport.goodsId != null && consumedCountReport.goodsId.equals(soldCountReport.goodsId)) {
                    found = true;
                    consumedCountReport.soldCount = soldCountReport.soldCount;
                    break;
                }
            }
            if (!found) {
                consumedCountList.add(soldCountReport);
            }
        }
        List<SupplierGoodsReport> refundCountList = getRefundCountList(supplierId, goodsId, shopId, fromDate, toDate);
        for (SupplierGoodsReport refundCountReport : refundCountList) {
            for (SupplierGoodsReport consumedCountReport : consumedCountList) {
                if (!found && consumedCountReport.goodsId != null && consumedCountReport.goodsId.equals(refundCountReport.goodsId)) {
                    found = true;
                    consumedCountReport.refundCount = refundCountReport.refundCount;
                    break;
                }
            }
            if (!found) {
                consumedCountList.add(refundCountReport);
            }
        }
        return consumedCountList;
    }

    public static List<SupplierGoodsReport> getSupplierGoodsReport(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate) {
        return getGoodsCountList(supplierId, goodsId, shopId, fromDate, toDate, "select new models.report.SupplierGoodsReport(goods.id, goods.shortName, count(*), sum(faceValue)) " +
                "from ECoupon where status=models.order.ECouponStatus.CONSUMED");
    }

    public static List<SupplierGoodsReport> getSoldCountList(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate) {
        return getGoodsCountList(supplierId, goodsId, shopId, fromDate, toDate, "select new models.report.SupplierGoodsReport(goods.id, goods.shortName, count(*)) " +
                "from ECoupon where status!=models.order.ECouponStatus.REFUND");
    }

    public static List<SupplierGoodsReport> getRefundCountList(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate) {
        return getGoodsCountList(supplierId, goodsId, shopId, fromDate, toDate, "select new models.report.SupplierGoodsReport(goods.id, goods.shortName, 0, count(*)) " +
                "from ECoupon where status=models.order.ECouponStatus.REFUND");
    }

    private static List<SupplierGoodsReport> getGoodsCountList(Long supplierId, Long goodsId, Long shopId, Date fromDate, Date toDate, String initSql) {
        EntityManager entityManager = JPA.em();
        StringBuilder sql = new StringBuilder(initSql + " and goods.supplierId=" + supplierId);
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
