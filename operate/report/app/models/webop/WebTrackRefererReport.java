package models.webop;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Query;
import play.db.jpa.JPA;

public class WebTrackRefererReport {
    /**
     * 分组名称
     */
    public String subject;
    
    /**
     * 访问数
     */
    public Integer visitCount;
    
    /**
     * 加入购物车数量
     */
    public Integer cartCount;
    
    /**
     * 订单数
     */
    public Integer orderCount;
    
    /**
     * 注册数
     */
    public Integer registerCount;
    
    /**
     * 下单总金额
     */
    public BigDecimal payAmount;
    
    
    public WebTrackRefererReport(String subject, Integer visitCount,
            Integer registerCount, Integer cartCount, Integer orderCount, BigDecimal payAmount) {
        this.subject = subject;
        this.visitCount = visitCount;
        this.cartCount = cartCount;
        this.orderCount = orderCount;
        this.registerCount = registerCount;
        this.payAmount = payAmount;
    }

    public WebTrackRefererReport() {
        this.visitCount = 0;
        this.cartCount = 0;
        this.registerCount = 0;
        this.orderCount = 0;
        this.payAmount = BigDecimal.ZERO; 
    }
    
    public String getShortSubject() {
        if (this.subject != null && this.subject.length() > 30) {
            return this.subject.substring(0, 30) + "...";
        }
        return this.subject;
    }
    
    /**
     * 查询外链汇总报表.
     * @param condition
     * @return
     */
    public static List<WebTrackRefererReport> queryRefererReport(WebTrackRefererCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.webop.WebTrackRefererReport(" + condition.getSubjectName() + ", count(w.id), sum(w.registerCount), sum(w.cartCount), sum(w.orderCount), sum(w.payAmount)) "
                                + " from UserWebIdentification w where "
                                + condition.getFilter() + " group by " + condition.getSubjectName() + " order by sum(w.orderCount) DESC, count(w.id) DESC");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();
    }
    
    public static WebTrackRefererReport summary(List<WebTrackRefererReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new WebTrackRefererReport();
        }
        
        WebTrackRefererReport sum = new WebTrackRefererReport();
            
        for (WebTrackRefererReport report : resultList) {
            if (report.visitCount != null) sum.visitCount += report.visitCount;
            if (report.cartCount != null) sum.cartCount += report.cartCount;
            if (report.registerCount != null) sum.registerCount += report.registerCount;
            if (report.orderCount != null) sum.orderCount += report.orderCount;
            if (report.payAmount != null) sum.payAmount = sum.payAmount.add(report.payAmount);
        }
        
        return sum;
    }
}
