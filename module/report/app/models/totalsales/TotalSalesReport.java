package models.totalsales;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Query;
import models.order.ECoupon;
import models.supplier.Supplier;
import play.db.jpa.JPA;

public class TotalSalesReport {

    public Long keyId;
    
    /**
     * 发生日期.
     */
    public String checkedOn;
    
    /**
     * 汇总关键字.
     */
    public String key;
    
    /**
     * 验证笔数.
     */
    public Long checkedCount;
    
    /**
     * 总面值.
     */
    public BigDecimal sumFaceValue;
    
    /**
     * 总应收款.
     */
    public BigDecimal sumAmount;

    public TotalSalesReport() {
        this("", 0l, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }
    public TotalSalesReport(String checkedOn, String key, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount) {
        this(key, checkedCount, sumFaceValue, sumAmount, null);
        this.checkedOn = checkedOn;
    }

    public TotalSalesReport(String checkedOn, Long supplierId, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount) {
        this(supplierId, checkedCount, sumFaceValue, sumAmount, null);
        this.checkedOn = checkedOn;
    }    

    public TotalSalesReport(String key, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount, Long keyId) {
        this.key = key;
        this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumAmount = sumAmount;
        this.keyId = keyId;
    }

    public TotalSalesReport(Long supplierId, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount, Long keyId) {
        this.keyId = keyId;
        Supplier supplier = Supplier.findById(supplierId);
        this.key = (supplier == null) ? "未知" : supplier.fullName; 
            this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumAmount = sumAmount;
    }
    
    /**
     * 按日期汇总.
     * @param condition
     * @return
     */
    public static List<TotalSalesReport> queryTrends(TotalSalesCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.totalsales.TotalSalesReport(str(year(e.consumedAt))||'-'||str(month(e.consumedAt))||'-'||str(day(e.consumedAt)), "
                                + condition.getKeyColumn()
                                + ", count(e.id), sum(e.faceValue), sum(e.salePrice))"
                                + " from ECoupon e where "
                                + condition.getFilter() + " group by str(year(e.consumedAt))||'-'||str(month(e.consumedAt))||'-'||str(day(e.consumedAt)), " 
                                + condition.getGroupBy() + " order by e.consumedAt"); 
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();        
    }

    /**
     * 按类别汇总
     * @param condition
     * @return
     */
    public static List<TotalSalesReport> queryRatios(
            TotalSalesCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.totalsales.TotalSalesReport("
                                + condition.getKeyColumn()
                                + ", count(e.id), sum(e.faceValue), sum(e.salePrice), " + condition.getKeyIdColumn() + ") "
                                + " from ECoupon e where "
                                + condition.getFilter() + " group by " 
                                + condition.getGroupBy() + "," + condition.getKeyIdColumn() + " order by e.consumedAt"); 
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();    
    }
    

    public static TotalSalesReport summary(List<TotalSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new TotalSalesReport(
                    "", 0l, BigDecimal.ZERO, BigDecimal.ZERO, null);
        }

        TotalSalesReport sum = new TotalSalesReport("", 0l, BigDecimal.ZERO, BigDecimal.ZERO, null);

        for (TotalSalesReport report : resultList) {
            if (report.checkedCount != null) sum.checkedCount += report.checkedCount;
            if (report.sumFaceValue != null) sum.sumFaceValue = sum.sumFaceValue.add(report.sumFaceValue);
            if (report.sumFaceValue != null) sum.sumAmount = sum.sumAmount.add(report.sumAmount);
        }

        return sum;
    }

    public static List<ECoupon> queryList(TotalSalesCondition condition) {
        Query query = JPA.em()
                .createQuery("from ECoupon e where "
                                + condition.getFilter() + " order by e.consumedAt"); 
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();     
    }

    public static TotalSalesReport summaryList(List<ECoupon> ecoupons) {
        if (ecoupons == null || ecoupons.size() == 0) {
            return new TotalSalesReport();
        }

        TotalSalesReport sum = new TotalSalesReport();

        for (ECoupon ecoupon : ecoupons) {
            sum.checkedCount += 1;
            if (ecoupon.faceValue != null) sum.sumFaceValue = sum.sumFaceValue.add(ecoupon.faceValue);
            if (ecoupon.salePrice != null) sum.sumAmount = sum.sumAmount.add(ecoupon.salePrice);
        }

        return sum;
    }

    
}
