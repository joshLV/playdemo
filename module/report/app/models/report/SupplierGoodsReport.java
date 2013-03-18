package models.report;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户看的商品销售情况报表.
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

    public static List<SupplierGoodsReport> getGoodsSellingReport(Long supplierId, String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        List<SupplierGoodsReport> consumedCountList = getConsumedCountList(supplierId, goodsShortName, shopId, fromDate, toDate);
        List<SupplierGoodsReport> soldCountList = getSoldCountList(supplierId, goodsShortName, shopId, fromDate, toDate);
        boolean found = false;
        for (SupplierGoodsReport soldCountReport : soldCountList) {

            for (SupplierGoodsReport consumedCountReport : consumedCountList) {
                if (!found && consumedCountReport.goodsName != null && consumedCountReport.goodsName.equals(soldCountReport.goodsName)) {
                    found = true;
                    consumedCountReport.soldCount = soldCountReport.soldCount;
                    break;
                }
            }
            if (!found) {
                consumedCountList.add(soldCountReport);
            }
            found = false;
        }
        List<SupplierGoodsReport> refundCountList = getRefundCountList(supplierId, goodsShortName, shopId, fromDate, toDate);
        found = false;
        for (SupplierGoodsReport refundCountReport : refundCountList) {
            for (SupplierGoodsReport consumedCountReport : consumedCountList) {
                if (!found && consumedCountReport.goodsName != null && consumedCountReport.goodsName.equals(refundCountReport.goodsName)) {
                    found = true;
                    consumedCountReport.refundCount = refundCountReport.refundCount;
                    break;
                }
            }
            if (!found) {
                consumedCountList.add(refundCountReport);
            }
            found = false;
        }
        return consumedCountList;
    }

    public static List<SupplierGoodsReport> getConsumedCountList(Long supplierId, String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        return getGoodsCountList(supplierId, goodsShortName, shopId, fromDate, toDate, "consumedAt", "select new models.report.SupplierGoodsReport(goods.id, goods.shortName, count(*), sum(faceValue)) " +
                "from ECoupon where status=models.order.ECouponStatus.CONSUMED");
    }

    public static List<SupplierGoodsReport> getSoldCountList(Long supplierId, String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        return getGoodsCountList(supplierId, goodsShortName, shopId, fromDate, toDate, "createdAt", "select new models.report.SupplierGoodsReport(goods.id, goods.shortName, count(*)) " +
                "from ECoupon where status!=models.order.ECouponStatus.REFUND");
    }

    private static List<SupplierGoodsReport> getRefundCountList(Long supplierId, String goodsShortName, Long shopId, Date fromDate, Date toDate) {
        return getGoodsCountList(supplierId, goodsShortName, shopId, fromDate, toDate, "refundAt", "select new models.report.SupplierGoodsReport(goods.id, goods.shortName, 0, count(*)) " +
                "from ECoupon where status=models.order.ECouponStatus.REFUND");
    }

    private static List<SupplierGoodsReport> getGoodsCountList(Long supplierId, String goodsShortName, Long shopId, Date fromDate, Date toDate, String dateColumn, String initSql) {
        EntityManager entityManager = JPA.em();
        StringBuilder sql = new StringBuilder(initSql + " and goods.supplierId=" + supplierId);
        if (StringUtils.isNotBlank(goodsShortName)) {
            sql.append(" and goods.shortName='" + goodsShortName + "'");
            System.out.println("goodsShortName:" + goodsShortName);
        }
        if (shopId != null && shopId != 0) {
            sql.append(" and shop.id=" + shopId);
        }
        if (fromDate != null) {
            sql.append(" and " + dateColumn + ">=:fromDate");
        }
        if (toDate != null) {
            sql.append(" and " + dateColumn + "<=:toDate");
        }
        sql.append(" group by goods.shortName order by count(*) desc");
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
