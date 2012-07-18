package models.webop;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.TypedQuery;
import play.db.jpa.JPA;

public class WebTrackRefererReport {
    /**
     * 分组名称
     */
    public String subject;
    
    /**
     * 访问数
     */
    public Long visitCount;
    
    /**
     * 加入购物车数量
     */
    public Long cartCount;
    
    /**
     * 订单数
     */
    public Long orderCount;
    
    /**
     * 下单总金额
     */
    public BigDecimal payAmount;
    
    
    public WebTrackRefererReport(String subject, Long visitCount,
            Long cartCount, Long orderCount, BigDecimal payAmount) {
        this.subject = subject;
        this.visitCount = visitCount;
        this.cartCount = cartCount;
        this.orderCount = orderCount;
        this.payAmount = payAmount;
    }

    public WebTrackRefererReport() {
        this.visitCount = 0l;
        this.cartCount = 0l;
        this.orderCount = 0l;
        this.payAmount = BigDecimal.ZERO; 
    }    
    
    /**
     * 查询外链汇总报表.
     * @param condition
     * @return
     */
    public static List<WebTrackRefererReport> queryRefererReport(WebTrackRefererCondition condition) {
        TypedQuery<WebTrackRefererReport> query = JPA.em()
                .createQuery(
                        "select new models.webop.WebTrackRefererReport(" + condition.getSubjectName() + ", sum(w.id), sum(w.cartCount), sum(w.orderCount), sum(w.payAmount)) "
                                + " from UserWebIdentification w where "
                                + condition.getFilter() + " group by " + condition.getSubjectName() + " order by sum(w.id) DESC",
                        WebTrackRefererReport.class);
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
            if (report.orderCount != null) sum.orderCount += report.orderCount;
            if (report.payAmount != null) sum.payAmount = sum.payAmount.add(report.payAmount);
        }
        
        return sum;
    }
}
