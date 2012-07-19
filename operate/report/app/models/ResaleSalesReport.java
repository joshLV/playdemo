package models;

import models.order.ECoupon;
import models.order.Order;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
public class ResaleSalesReport extends Model {

    public Order order;
    public ECoupon coupon;
    public long buyNumber;

    public long orderCount;

    public BigDecimal salePrice;

    public static List<ResaleSalesReport> query(
            ResaleSalesReportCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.ResaleSalesReport(e.status, oi.salePrice, sum(oi.buyNumber),sum(oi.salePrice*oi.buyNumber))"
                                + " from ECoupon e where "
                                + condition.getFilter() + " group by r.order, r.salePrice order by r.goods",
                        ResaleSalesReport.class);

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();
    }

}
