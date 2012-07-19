package models.webop;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Query;
import play.db.jpa.JPA;

public class WebTrackReferCodeReport {
    /**
     * 推荐码
     */
    public String referCode;

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
     * 注册用户数
     */
    public Long registerCount;

    /**
     * 下单总金额
     */
    public BigDecimal payAmount;


    public WebTrackReferCodeReport(String referCode, Long visitCount,
            Long registerCount, Long cartCount, Long orderCount, BigDecimal payAmount) {
        this.referCode = referCode;
        this.visitCount = visitCount;
        this.cartCount = cartCount;
        this.orderCount = orderCount;
        this.registerCount = registerCount;
        this.payAmount = payAmount;
    }

    public WebTrackReferCodeReport() {
        this.visitCount = 0;
        this.cartCount = 0;
        this.orderCount = 0;
        this.registerCount = 0;
        this.payAmount = BigDecimal.ZERO;
    }

    /**
     * 查询推荐码汇总报表.
     * @param condition
     * @return
     */
    public static List<WebTrackReferCodeReport> queryReferCodeReport(WebTrackReferCodeCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.webop.WebTrackReferCodeReport(w.referCode, count(w.id), sum(w.registerCount), sum(w.cartCount), sum(w.orderCount), sum(w.payAmount)) "
                                + " from UserWebIdentification w where "
                                + condition.getFilter() + " group by w.referCode order by sum(w.orderCount) DESC, count(w.id) DESC");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();
    }

    public static WebTrackReferCodeReport summary(List<WebTrackReferCodeReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new WebTrackReferCodeReport();
        }

        WebTrackReferCodeReport sum = new WebTrackReferCodeReport();

        for (WebTrackReferCodeReport report : resultList) {
            if (report.visitCount != null) sum.visitCount += report.visitCount;
            if (report.cartCount != null) sum.cartCount += report.cartCount;
            if (report.orderCount != null) sum.orderCount += report.orderCount;
            if (report.registerCount != null) sum.registerCount += report.registerCount;
            if (report.payAmount != null) sum.payAmount = sum.payAmount.add(report.payAmount);
        }

        return sum;
    }
}
