package models.totalsales;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Query;
import models.order.ECoupon;
import models.supplier.Supplier;
import play.db.jpa.JPA;

public class TotalSalesReport {

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

    public TotalSalesReport(String checkedOn, String key, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount) {
        this.checkedOn = checkedOn;
        this.key = key;
        this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumAmount = sumAmount;
    }

    public TotalSalesReport(String checkedOn, Long supplierId, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount) {
        this.checkedOn = checkedOn;
        Supplier supplier = Supplier.findById(supplierId);
        this.key = (supplier == null) ? "未知" : supplier.fullName; 
            this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumAmount = sumAmount;
    }    

    public TotalSalesReport(String key, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumAmount) {
        this.key = key;
        this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumAmount = sumAmount;
    }


    public static List<TotalSalesReport> query(TotalSalesCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.totalsales.TotalSalesReport(str(year(e.consumedAt))||'-'||str(month(e.consumedAt))||'-'||str(day(e.consumedAt)), "
                                + condition.getKeyColumn()
                                + ", count(e.id), sum(e.faceValue), sum(e.originalPrice)) "
                                + " from ECoupon e where "
                                + condition.getFilter() + " group by str(year(e.consumedAt))||'-'||str(month(e.consumedAt))||'-'||str(day(e.consumedAt)), " 
                                + condition.getGroupBy() + " order by e.consumedAt"); 
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();        
    }


    public static TotalSalesReport summary(List<TotalSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new TotalSalesReport(null, 0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        TotalSalesReport sum = new TotalSalesReport(null, 0l, BigDecimal.ZERO, BigDecimal.ZERO);

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
            return new TotalSalesReport(null, 0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        TotalSalesReport sum = new TotalSalesReport(null, 0l, BigDecimal.ZERO, BigDecimal.ZERO);

        for (ECoupon ecoupon : ecoupons) {
            sum.checkedCount += 1;
            if (ecoupon.faceValue != null) sum.sumFaceValue = sum.sumFaceValue.add(ecoupon.faceValue);
            if (ecoupon.originalPrice != null) sum.sumAmount = sum.sumAmount.add(ecoupon.originalPrice);
        }

        return sum;
    }

    
}
