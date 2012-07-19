package models;

import models.order.ECoupon;
import models.order.Order;
import models.resale.Resaler;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
public class ResaleSalesReport extends Model {

    public Resaler resaler;

    public Order order;

    public ECoupon coupon;

    public long buyNumber;

    public BigDecimal salePrice;

    public ResaleSalesReport(Order order, BigDecimal salePrice,Long buyNumber) {
        this.order = order;
        this.salePrice = salePrice;
        this.buyNumber=buyNumber;
    }

    public static List<ResaleSalesReport> query(
            ResaleSalesReportCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.ResaleSalesReport(e.order,sum(e.salePrice),sum(e.orderItems.buyNumber))"
                                + " from ECoupon e "
                                + condition.getFilter() + " group by e.order order by e.createdAt desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        return query.getResultList();
    }

}
