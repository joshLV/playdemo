package models;

import models.accounts.AccountType;
import models.accounts.TradeType;
import models.order.Order;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
@Entity
@Table(name = "resale_sales_report")
public class ResaleSalesReport extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;
    public String loginName;
    public String userName;
    public long buyNumber;

    public BigDecimal salePrice;

    public BigDecimal refundPrice;

    public Long totalNumber;
    public BigDecimal amount;
    public BigDecimal totalRefundPrice;

    public ResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber) {
        this.order = order;
        if (order.userType == AccountType.CONSUMER) {
            this.loginName = order.getUser().loginName;
        } else {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
        }
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
    }

    public ResaleSalesReport(long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice) {
        this.totalNumber = totalNumber;
        this.amount = amount;
        this.totalRefundPrice = totalRefundPrice;
    }


    public static List<ResaleSalesReport> query(
            ResaleSalesReportCondition condition, AccountType type) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.ResaleSalesReport(e.order,sum(e.salePrice),count(e.orderItems.buyNumber))"
                                + " from ECoupon e "
                                + condition.getFilter(type) + " group by e.order.userId order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        //取得退款的金额
        String sql = "";
        List<ResaleSalesReport> resultList = query.getResultList();
        for (ResaleSalesReport resaleSalesReport : resultList) {
            sql = "select sum(t.amount) from TradeBill t where t.tradeType='" + TradeType.REFUND + "' and t.orderId=" + resaleSalesReport.order.id;
            Object obj = JPA.em().createQuery(sql).getSingleResult();
            BigDecimal price = obj == null ? BigDecimal.ZERO : new BigDecimal(obj.toString());
            resaleSalesReport.refundPrice = price;
        }
        return resultList;
    }

    public static ResaleSalesReport summary(List<ResaleSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ResaleSalesReport(0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        long buyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal refundPrice = BigDecimal.ZERO;
        BigDecimal totRefundPrice = BigDecimal.ZERO;
        for (ResaleSalesReport item : resultList) {
            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice);
            totRefundPrice = item.refundPrice == null ? new BigDecimal(0) : item.refundPrice;
            refundPrice = refundPrice.add(totRefundPrice);

        }
        return new ResaleSalesReport(buyCount, amount, refundPrice);
    }
}
