package models.totalsales;

import models.order.ECoupon;
import models.supplier.Supplier;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public BigDecimal sumOriginValue;
    
    /**
     * 总应收款.
     */
    public BigDecimal sumSalesAmount;

    public TotalSalesReport() {
        this("", 0l, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }
    public TotalSalesReport(String checkedOn, String key, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumOriginValue, BigDecimal sumSalesAmount) {
        this(key, checkedCount, sumFaceValue, sumOriginValue, sumSalesAmount, null);
        this.checkedOn = checkedOn;
    }

    public TotalSalesReport(String checkedOn, Long supplierId, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumOriginValue, BigDecimal sumSalesAmount) {
        this(supplierId, checkedCount, sumFaceValue, sumOriginValue, sumSalesAmount, null);
        this.checkedOn = checkedOn;
    }    

    public TotalSalesReport(String key, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumOriginValue, BigDecimal sumSalesAmount, Long keyId) {
        this.key = key;
        this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumOriginValue = sumOriginValue;
        this.sumSalesAmount = sumSalesAmount;
        this.keyId = keyId;
    }

    public TotalSalesReport(Long supplierId, Long checkedCount,
            BigDecimal sumFaceValue, BigDecimal sumOriginValue, BigDecimal sumSalesAmount, Long keyId) {
        this.keyId = keyId;
        this.keyId = supplierId;
        Supplier supplier = Supplier.findById(supplierId);
        this.key = (supplier == null) ? "未知" : supplier.fullName; 
            this.checkedCount = checkedCount;
        this.sumFaceValue = sumFaceValue;
        this.sumOriginValue = sumOriginValue;
        this.sumSalesAmount = sumSalesAmount;
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
                                + ", count(e.id), sum(e.faceValue), sum(e.originalPrice), sum(e.salePrice))"
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
                                + ", count(e.id), sum(e.faceValue), sum(e.originalPrice), sum(e.salePrice), " + condition.getKeyIdColumn() + ") "
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
            return new TotalSalesReport();
        }

        TotalSalesReport sum = new TotalSalesReport();

        for (TotalSalesReport report : resultList) {
            if (report.checkedCount != null) sum.checkedCount += report.checkedCount;
            if (report.sumFaceValue != null) sum.sumFaceValue = sum.sumFaceValue.add(report.sumFaceValue);
            if (report.sumOriginValue != null) sum.sumOriginValue = sum.sumOriginValue.add(report.sumOriginValue);
            if (report.sumSalesAmount != null) sum.sumSalesAmount = sum.sumSalesAmount.add(report.sumSalesAmount);
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
            if (ecoupon.salePrice != null) sum.sumSalesAmount = sum.sumSalesAmount.add(ecoupon.salePrice);
            if (ecoupon.originalPrice != null) sum.sumOriginValue = sum.sumOriginValue.add(ecoupon.originalPrice);
        }

        return sum;
    }
    
    /**
     * 生成走势图数据
     * @param totalSales
     * @return
     */
    public static Map<String, List<TotalSalesReport>> mapTrendsCharts(
            List<TotalSalesReport> totalSales, List<String> dateList) {
        Map<String, TotalSalesReport> map = new HashMap<>();
        Set<String> keySet = new HashSet<>();
        for (TotalSalesReport report : totalSales) {
            map.put(report.key + "." + report.checkedOn, report);
            keySet.add(report.key);
        }

        Map<String, List<TotalSalesReport>> mapedResult = new HashMap<>();
        for (String key : keySet) {
            List<TotalSalesReport> list = new ArrayList<>();
            for (String date : dateList) {
                TotalSalesReport report = map.get(key + "." + date);
                if (report == null) {
                    report  = new TotalSalesReport();
                    report.checkedOn = date;
                    report.key = key;
                }
                list.add(report);
            }
            mapedResult.put(key, list);
        }
        
        return mapedResult;
    }
    
    public static List<String> generateDateList(TotalSalesCondition condition) {
        Date date = condition.beginAt;
        System.out.println(date+"-----------");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
        List<String> dateList = new ArrayList<>();
        long oneDay = 1000L * 60 * 60 * 24;
        do {
            dateList.add(df.format(date));
            date = new Date(date.getTime() + oneDay);
        } while(date.compareTo(condition.endAt) <= 0);
        return dateList;
    }

    
}
