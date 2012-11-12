package models;

import models.sales.Goods;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-7
 * Time: 下午4:05
 */
public class RefundReport extends Model {
    public String supplierName;
    public Goods goods;
    public BigDecimal salePrice;
    public Long buyNumber;
    public BigDecimal amount;
    public BigDecimal totalAmount;
    public String reportDate;

    public RefundReport(Goods goods, BigDecimal salePrice, Long buyNumber, BigDecimal amount) {
        this.goods = goods;
        if (goods != null) {
            Supplier supplier = Supplier.findById(goods.supplierId);
            this.supplierName = StringUtils.isNotBlank(supplier.otherName) ? supplier.otherName : supplier.fullName;
        }
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.amount = amount;
    }

    public RefundReport(long buyCount, BigDecimal amount, BigDecimal totAmount) {
        this.amount = amount;
        this.buyNumber = buyCount;
        this.totalAmount = totAmount;
    }

    public RefundReport(String reportDate, BigDecimal refundPrice, Long buyCount) {
        this.amount = refundPrice;
        this.buyNumber = buyCount;
        this.reportDate = reportDate;
    }

    /**
     * 取得按商品统计的退款记录
     *
     * @param condition
     * @return
     */
    public static List<RefundReport> query(RefundReportCondition condition) {

        String sql = "select new models.RefundReport(e.orderItems.goods,e.salePrice,count(e.orderItems.buyNumber),sum(e.refundPrice)) from ECoupon e ";
        String groupBy = " group by e.orderItems.goods";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RefundReport> resultList = query.getResultList();
        return resultList;
    }

    /**
     * 取得每天有多少消费者退款的记录
     *
     * @param condition
     * @return
     */
    public static List<RefundReport> getConsumerRefundData(RefundReportCondition condition) {

        String refundAt = "str(year(e.refundAt))||'-'|| str(month(e.refundAt))||'-'|| str(day(e.refundAt)) ";
        String sql = "select new models.RefundReport( " + refundAt + ",sum(e.refundPrice),count(e.id)) from ECoupon e ";
        String groupBy = " group by " + refundAt;
        Boolean right;
        Long id;

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RefundReport> resultList = query.getResultList();
        return resultList;
    }


    /**
     * 商品退款总计
     *
     * @param resultList
     * @return
     */
    public static RefundReport summary(List<RefundReport> resultList) {
        if (resultList.size() == 0) {
            return new RefundReport(0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        long buyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal totAmount = BigDecimal.ZERO;
        for (RefundReport item : resultList) {
            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice);
            totAmount = totAmount.add(item.amount == null ? BigDecimal.ZERO : item.amount);

        }
        return new RefundReport(buyCount, amount, totAmount);

    }

    /**
     * 消费者退款总计
     *
     * @param resultList
     * @return
     */
    public static RefundReport consumerSummary(List<RefundReport> resultList) {
        if (resultList.size() == 0) {
            return new RefundReport(0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        long buyCount = 0l;
        BigDecimal totAmount = BigDecimal.ZERO;
        for (RefundReport item : resultList) {
            buyCount += item.buyNumber;
            totAmount = totAmount.add(item.amount == null ? BigDecimal.ZERO : item.amount);

        }
        return new RefundReport(buyCount, BigDecimal.ZERO, totAmount);
    }

}

